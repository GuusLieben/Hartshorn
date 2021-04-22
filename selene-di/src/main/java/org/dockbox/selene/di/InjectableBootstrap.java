/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.di;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;

import org.dockbox.selene.api.Selene;
import org.dockbox.selene.api.domain.Exceptional;
import org.dockbox.selene.api.entity.annotations.DoNotEnable;
import org.dockbox.selene.api.keys.Keys;
import org.dockbox.selene.api.module.ModuleContainer;
import org.dockbox.selene.api.module.ModuleManager;
import org.dockbox.selene.api.module.annotations.Module;
import org.dockbox.selene.di.modules.SingleAnnotatedImplementationModule;
import org.dockbox.selene.di.modules.SingleImplementationModule;
import org.dockbox.selene.di.modules.SingleInstanceModule;
import org.dockbox.selene.di.properties.AnnotationProperty;
import org.dockbox.selene.di.properties.InjectableType;
import org.dockbox.selene.di.properties.InjectorProperty;
import org.dockbox.selene.util.Reflect;
import org.dockbox.selene.util.SeleneUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import sun.misc.Unsafe;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class InjectableBootstrap {

    private final transient List<AbstractModule> injectorModules = SeleneUtils.emptyConcurrentList();
    private final transient List<InjectionPoint<?>> injectionPoints = SeleneUtils.emptyConcurrentList();
    private Unsafe unsafe;
    private Injector injector;

    private static <T> Exceptional<T> getRawInstance(Class<T> type, Injector injector) {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            T t = ctor.newInstance();
            injector.injectMembers(t);
            return Exceptional.of(t);
        }
        catch (Exception e) {
            return Exceptional.of(e);
        }
    }

    private static <T> T getInjectedInstance(Injector injector, Class<T> type, InjectorProperty<?>... additionalProperties) {
        @SuppressWarnings("rawtypes")
        Exceptional<Class> annotation = Keys.value(AnnotationProperty.KEY, Class.class, additionalProperties);
        if (annotation.present() && annotation.get().isAnnotation()) {
            //noinspection unchecked
            return (T) injector.getInstance(Key.get(type, annotation.get()));
        }
        else {
            return injector.getInstance(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ModuleInjectionConfiguration getModuleConfiguration(T instance, ModuleContainer header) {
        ModuleInjectionConfiguration module = new ModuleInjectionConfiguration();

        if (!(null == instance || instance instanceof Class<?>)) {
            module.acceptBinding((Class<T>) instance.getClass(), instance);
            module.acceptInstance(instance);
        }
        if (null != header) module.acceptBinding(ModuleContainer.class, header);

        return module;
    }

    public <T> Exceptional<T> getInstanceSafe(Class<T> type, InjectorProperty<?>... additionalProperties) {
        return Exceptional.of(this.getInstance(type, additionalProperties));
    }

    public <T> Exceptional<T> getInstanceSafe(Class<T> type, Object module, InjectorProperty<?>... additionalProperties) {
        return Exceptional.of(this.getInstance(type, module, additionalProperties));
    }

    public <T> Exceptional<T> getInstanceSafe(Class<T> type, Class<?> module, InjectorProperty<?>... additionalProperties) {
        return Exceptional.of(this.getInstance(type, module, additionalProperties));
    }

    public <T> T getInstance(Class<T> type, InjectorProperty<?>... additionalProperties) {
        return this.getInstance(type, type, additionalProperties);
    }

    public <T> T getInstance(Class<T> type, Object module, InjectorProperty<?>... additionalProperties) {
        if (null != module) {
            return this.getInstance(type, module.getClass(), additionalProperties);
        }
        else {
            return this.getInstance(type, additionalProperties);
        }
    }

    /**
     * Gets an instance of a provided {@link Class} type. If the type is annotated with {@link Module}
     * it is ran through the {@link ModuleManager} instance to obtain the instance. If it is not
     * annotated as such, it is ran through the instance {@link Injector} to obtain the instance based
     * on implementation, or manually, provided mappings.
     *
     * @param <T>
     *         The type parameter for the instance to return
     * @param type
     *         The type of the instance
     * @param module
     *         The type of the module if module specific bindings are to be used
     * @param additionalProperties
     *         The properties to be passed into the type either during or after
     *         construction
     *
     * @return The instance, if present. Otherwise returns null
     */
    public <T> T getInstance(Class<T> type, Class<?> module, InjectorProperty<?>... additionalProperties) {
        T typeInstance = null;

        // Modules are initially created using #getInstance here (assuming SimpleModuleManager or a
        // derivative is
        // bound to ModuleManager). Therefore it may not yet be present in the ModuleManager's registry.
        // In which
        // case `null` is (re-)assigned to typeInstance so that it can be created through the generated
        // module-specific injector.
        if (type.isAnnotationPresent(Module.class)) {
            typeInstance =
                    this.getInstanceSafe(ModuleManager.class)
                            .map(moduleManager -> moduleManager.getInstance(type).orNull())
                            .orNull();
        }

        Injector injector = this.rebuildInjector();

        // Type instance can be present if it is a module. These instances are also created using Guice
        // injectors
        // and therefore do not need late member injection here.
        typeInstance = this.createInstanceOf(type, typeInstance, injector, additionalProperties);

        if (null != typeInstance) typeInstance = this.applyInjectionPoints(type, typeInstance);

        // Enables all fields which are not annotated with @Module or @DoNotEnable
        enableInjectionPoints(typeInstance);

        // Inject properties if applicable
        if (typeInstance instanceof InjectableType && ((InjectableType) typeInstance).canEnable())
            ((InjectableType) typeInstance).stateEnabling(additionalProperties);

        // May be null, but we have used all possible injectors, it's up to the developer now
        return typeInstance;
    }

    private <T> void enableInjectionPoints(T typeInstance) {
        if (typeInstance == null) return;
        SeleneUtils.merge(Reflect.annotatedFields(Inject.class, typeInstance.getClass())).stream()
                .filter(field -> field.isAnnotationPresent(DoNotEnable.class))
                .filter(field -> Reflect.assignableFrom(InjectableType.class, field.getType()))
                .map(field -> Selene.handle(() -> field.get(typeInstance)))
                .filter(Objects::nonNull)
                .map(fieldInstance -> (InjectableType) fieldInstance)
                .filter(InjectableType::canEnable)
                .forEach(InjectableType::stateEnabling);
    }

    @Nullable
    private <T> T createInstanceOf(Class<T> type, T typeInstance, Injector injector, InjectorProperty<?>[] additionalProperties) {
        if (null == typeInstance) {
            try {
                typeInstance = InjectableBootstrap.getInjectedInstance(injector, type, additionalProperties);
            }
            catch (ProvisionException e) {
                Selene.log().error("Could not create instance using registered injector " + injector + " for [" + type.getCanonicalName() + "]", e);
            }
            catch (ConfigurationException ce) {
                typeInstance = InjectableBootstrap.getRawInstance(type, injector)
                        .then(() -> this.getUnsafeInstance(type, injector))
                        .orNull();
            }
        }
        return typeInstance;
    }

    private <T> T applyInjectionPoints(Class<T> type, T typeInstance) {
        for (InjectionPoint<?> injectionPoint : this.injectionPoints) {
            if (injectionPoint.accepts(type)) {
                try {
                    //noinspection unchecked
                    typeInstance = ((InjectionPoint<T>) injectionPoint).apply(typeInstance);
                }
                catch (ClassCastException e) {
                    Selene.log().warn("Attempted to apply injection point to incompatible type [" + type.getCanonicalName() + "]");
                }
            }
        }
        return typeInstance;
    }

    private <T> @Nullable T getUnsafeInstance(Class<T> type, Injector injector) {
        Selene.log().warn("Attempting to get instance of [" + type.getCanonicalName() + "] through Unsafe");
        try {
            @SuppressWarnings("unchecked")
            T t = (T) this.getUnsafe().allocateInstance(type);
            injector.injectMembers(t);
            return t;
        }
        catch (Exception e) {
            Selene.handle("Could not create instance of [" + type.getCanonicalName() + "] through injected, raw or unsafe construction", e);
        }
        return null;
    }

    public Injector createModuleInjector(Object instance) {
        if (null != instance && instance.getClass().isAnnotationPresent(Module.class)) {
            Exceptional<ModuleContainer> context = this.getInstance(ModuleManager.class).getContext(instance.getClass());
            return this.createModuleInjector(instance, context.orNull());
        }
        return this.rebuildInjector();
    }

    public Injector createModuleInjector(Object instance, ModuleContainer container) {
        return this.rebuildInjector(InjectableBootstrap.getModuleConfiguration(instance, container));
    }

    public <T> T injectMembers(T type) {
        if (null != type) {
            this.rebuildInjector().injectMembers(type);
        }

        return type;
    }

    private Injector rebuildInjector(AbstractModule... additionalModules) {
        if (null == this.injector) {
            Collection<AbstractModule> modules = new ArrayList<>(this.injectorModules);
            modules.addAll(Arrays.stream(additionalModules).filter(Objects::nonNull).collect(Collectors.toList()));
            this.injector = Guice.createInjector(modules);
        }
        return this.injector;
    }

    public <T> T injectMembers(T type, Object module) {
        if (null != type) {
            if (null != module && module.getClass().isAnnotationPresent(Module.class)) {
                this.createModuleInjector(module).injectMembers(type);
            }
            else {
                this.rebuildInjector().injectMembers(type);
            }
        }
        return type;
    }

    public Map<Key<?>, Binding<?>> getAllBindings() {
        Map<Key<?>, Binding<?>> bindings = SeleneUtils.emptyConcurrentMap();
        this.rebuildInjector().getAllBindings().forEach((Key<?> key, Binding<?> binding) -> {
            try {
                Class<?> keyType = binding.getKey().getTypeLiteral().getRawType();
                Class<?> providerType = binding.getProvider().get().getClass();

                if (!keyType.equals(providerType) && null != providerType)
                    bindings.put(key, binding);
            }
            catch (ProvisionException | AssertionError ignored) {
            }
        });
        return bindings;
    }

    /**
     * Creates a custom binding for a given contract and implementation using a custom {@link
     * AbstractModule}. Requires the implementation to extend the contract type.
     *
     * <p>The binding is created by Guice, and can be annotated using Guice supported annotations
     * (e.g. {@link com.google.inject.Singleton})
     *
     * @param <T>
     *         The type parameter of the contract
     * @param contract
     *         The class type of the contract
     * @param implementation
     *         The class type of the implementation
     */
    public <T> void bind(Class<T> contract, Class<? extends T> implementation) {
        AbstractModule localModule = new SingleImplementationModule<>(contract, implementation);
        this.injectorModules.add(localModule);
        this.injector = null; // Reset injector so it regenerates later
    }

    public <C, T extends C> void bind(Class<C> contract, T instance) {
        AbstractModule localModule = new SingleInstanceModule<>(contract, instance);
        this.injectorModules.add(localModule);
        this.injector = null; // Reset injector so it regenerates later
    }

    public <C, T extends C, A extends Annotation> void bind(Class<C> contract, Class<? extends T> implementation, Class<A> annotation) {
        AbstractModule localModule = new SingleAnnotatedImplementationModule<>(contract, implementation, annotation);
        this.injectorModules.add(localModule);
        this.injector = null; // Reset injector so it regenerates later
    }

    public void bind(InjectConfiguration moduleConfiguration) {
        this.injectorModules.add(moduleConfiguration);
    }

    public void injectAt(InjectionPoint<?> property) {
        if (null != property) this.injectionPoints.add(property);
    }

    private Unsafe getUnsafe() {
        if (null == this.unsafe) {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                this.unsafe = (Unsafe) f.get(null);
            }
            catch (ReflectiveOperationException e) {
                Selene.handle(e);
            }
        }
        return this.unsafe;
    }

    @SuppressWarnings("unchecked")
    public <T, I extends T> Class<I> getBinding(Class<T> type) {
        for (Entry<Key<?>, Binding<?>> binding : this.getAllBindings().entrySet()) {
            if (binding.getKey().getTypeLiteral().getRawType().equals(type)) {
                return (Class<I>) binding.getValue().getKey().getTypeLiteral().getRawType();
            }
        }
        return null;
    }
}
