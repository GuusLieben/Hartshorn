/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.core.proxy.javassist;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.core.CustomMultiMap;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.DefaultContext;
import org.dockbox.hartshorn.core.context.ParameterLoaderContext;
import org.dockbox.hartshorn.core.context.element.FieldContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.dockbox.hartshorn.core.proxy.MethodProxyContext;
import org.dockbox.hartshorn.core.proxy.ProxyMethodCallback;
import org.dockbox.hartshorn.core.proxy.ProxyContext;
import org.dockbox.hartshorn.core.proxy.ProxyContextImpl;
import org.dockbox.hartshorn.core.proxy.ProxyHandler;
import org.dockbox.hartshorn.core.services.parameter.ParameterLoader;
import org.dockbox.hartshorn.core.proxy.CallbackPhase;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyFactory.UniqueName;
import lombok.Getter;

public class JavassistProxyHandler<T> extends DefaultContext implements ProxyHandler<T>, MethodHandler {

    static {
        ProxyFactory.nameGenerator = new UniqueName() {
            private final String sep = "_$$_hh" + Integer.toHexString(this.hashCode() & 0xfff) + "_";
            private int counter = 0;

            @Override
            public String get(final String classname) {
                return classname + this.sep + Integer.toHexString(this.counter++);
            }
        };
    }

    // Delegated instance
    @Nullable
    private final T instance;

    // Proxy instance, there will only ever be one instance per handler, however it may be absent until #proxy is called
    @Nullable
    private T proxyInstance;

    @Getter
    @NonNull
    private final ApplicationContext applicationContext;

    @Getter
    private final TypeContext<T> type;

    private final ParameterLoader<ParameterLoaderContext> parameterLoader = new UnproxyingParameterLoader();
    private final Map<Method, MethodProxyContext<T, ?>> handlers = new ConcurrentHashMap<>();
    private final MultiMap<Method, ProxyMethodCallback<T>> callbacks = new CustomMultiMap<>(CopyOnWriteArrayList::new);

    public JavassistProxyHandler(final @NonNull ApplicationContext applicationContext, final @Nullable T instance) {
        this(applicationContext, instance, (Class<T>) instance.getClass());
    }

    public JavassistProxyHandler(final @NonNull ApplicationContext applicationContext, final @Nullable T instance, final @NonNull Class<T> type) {
        this(applicationContext, instance, TypeContext.of(type));
    }

    public JavassistProxyHandler(final @NonNull ApplicationContext applicationContext, final @Nullable T instance, final @NonNull TypeContext<T> type) {
        this.applicationContext = applicationContext;
        this.instance = instance;
        this.type = type;
    }

    @SafeVarargs
    public final void delegate(final @NonNull MethodProxyContext<T, ?> @NonNull ... properties) {
        for (final MethodProxyContext<T, ?> property : properties) this.delegate(property);
    }

    @Override
    public void delegate(final @NonNull MethodProxyContext<T, ?> property) {
        if (Modifier.isFinal(property.target().getModifiers()))
            throw new IllegalArgumentException("Cannot proxy final method " + property.target().getName());

        if (this.handlers.containsKey(property.target()))
            throw new IllegalArgumentException("Cannot proxy method " + property.target().getName() + " twice");

        this.handlers.put(property.target(), property);
    }

    @Override
    public void callback(final MethodContext<?, T> method, final @NonNull ProxyMethodCallback<T> callback) {
        this.callbacks.put(method.method(), callback);
    }

    @Override
    public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
        final T callbackTarget = this.instance == null ? (T) self : this.instance;
        final MethodContext<?, T> methodContext = (MethodContext<?, T>) MethodContext.of(thisMethod);
        final ProxyContext proxyContext = new ProxyContextImpl(this, proceed == null ? null : MethodContext.of(proceed), self);

        final Object[] arguments = this.resolveArgs(thisMethod, self, args);

        this.performPhase(methodContext, callbackTarget, arguments, proxyContext, CallbackPhase.BEFORE);

        // The handler listens for all methods, while not all methods are proxied
        try {
            final Object out = this.handlers.containsKey(thisMethod)
                    ? this.invokeRegistered(self, thisMethod, proceed, arguments)
                    : this.invokeUnregistered(self, thisMethod, proceed, arguments);

            this.performPhase(methodContext, callbackTarget, arguments, proxyContext, CallbackPhase.AFTER);

            return out;
        }
        catch (final Throwable e) {
            this.performPhase(methodContext, callbackTarget, arguments, proxyContext, CallbackPhase.THROWING);
            throw e;
        }
    }

    protected void performPhase(final @NonNull MethodContext<?, T> methodContext, final @NonNull T instance, final @NonNull Object[] arguments, final @NonNull ProxyContext proxyContext, final @NonNull CallbackPhase phase) {
        this.callbacks.get(methodContext.method()).stream()
                .filter(callback -> callback.phase() == phase)
                .forEach(callback -> callback.accept(methodContext, instance, arguments, proxyContext));
    }

    protected Object invokeRegistered(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
        final MethodProxyContext<T, ?> context = this.handlers.get(thisMethod);
        Object returnValue = null;

        final MethodContext<?, ?> methodContext = proceed == null ? null : MethodContext.of(proceed);
        final Object result = context.delegate(this.instance, this, methodContext, self, args);
        if (context.overwriteResult() && !Void.TYPE.equals(thisMethod.getReturnType()))
            returnValue = result;

        return returnValue;
    }

    protected Object invokeUnregistered(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
        // If no handler is known, default to the original method. This is delegated to the instance
        // created, as it is typically created through Hartshorn's injectors and therefore DI dependent.
        Method target = thisMethod;
        if (this.instance == null && thisMethod == null)
            target = proceed;

        if (target != null) {
            // If the target type is a class, whether it is abstract or not, we can invoke the native method directly. However, when
            // the target type is an interface, this method is not defined, requiring us to perform our own equality check.
            if (target.getName().equals("equals")
                    && target.getDeclaringClass().equals(Object.class)
                    && this.instance == null
                    && this.type().isInterface()
            ) return this.proxyEquals(args[0]);

            return this.invokeTarget(self, thisMethod, target, args);
        }
        else {
            final StackTraceElement element = Thread.currentThread().getStackTrace()[3];
            final String name = element.getMethodName();
            final String className = this.type == null ? "" : this.type.name() + ".";
            throw new AbstractMethodError("Cannot invoke method '" + className + name + "' because it is abstract. This type is proxied, but no proxy property was found for the method.");
        }
    }

    protected boolean proxyEquals(final Object obj) {
        if (obj == null) return false;
        if (this.instance().map(instance -> instance.equals(obj)).or(false)) return true;
        return this.proxyInstance().map(proxyInstance -> proxyInstance == obj).or(false);
    }

    protected Object invokeTarget(final Object self, final Method thisMethod, final Method target, final Object[] args) throws Throwable {
        final Class<T> declaringType = this.type().type();

        try {
            // If the proxy associated with this handler has a delegate, use it.
            if (this.instance != null) return this.invokeDelegate(target, args);

                // If the method is default inside an interface, we cannot invoke it directly using a proxy instance. Instead we
                // need to lookup the method on the class and invoke it through the method handle directly.
            else if (thisMethod.isDefault()) return this.invokeDefault(declaringType, thisMethod, self, args);

                // If the current target instance (self) is not a proxy, we can invoke the method directly using reflections.
            else if (!(self instanceof Proxy || ProxyFactory.isProxyClass(self.getClass()))) return this.invokeSelf(self, target, args);

                // If the target method is concrete in an abstract class, we cannot invoke the method directly using reflections.
                // This solution uses private lookups to invoke the method, unreflecting the method first so it can be invoked using
                // proxy instances.
            else if (this.type().isAbstract() && !MethodContext.of(thisMethod).isAbstract()) return this.invokePrivate(declaringType, thisMethod, self, args);

                // If none of the above conditions are met, we have no way to handle the method.
            else throw new IllegalArgumentException("Cannot invoke method " + thisMethod.getName() + " on proxy " + self.getClass().getName());
        }
        catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    protected Object[] resolveArgs(final Method method, final Object instance, final Object[] args) {
        final MethodContext<?, ?> methodContext = MethodContext.of(method);
        final ParameterLoaderContext context = new ParameterLoaderContext(methodContext, methodContext.parent(), instance, this.applicationContext());
        return this.parameterLoader.loadArguments(context, args).toArray();
    }

    protected Object invokeDelegate(final Method target, final Object[] args) throws InvocationTargetException, IllegalAccessException {
        return this.invokeAccessible(target, method -> method.invoke(this.instance, args));
    }

    protected Object invokeSelf(final Object self, final Method target, final Object[] args) throws InvocationTargetException, IllegalAccessException {
        return this.invokeAccessible(target, method -> method.invoke(self, args));
    }

    protected Object invokeAccessible(final Method target, final MethodInvoker function) throws InvocationTargetException, IllegalAccessException {
        target.setAccessible(true);
        final Object result = function.invoke(target);
        target.setAccessible(false);
        return result;
    }

    protected Object invokeDefault(final Class<T> declaringType, final Method thisMethod, final Object self, final Object[] args) throws Throwable {
        return MethodHandles.lookup().findSpecial(
                declaringType,
                thisMethod.getName(),
                MethodType.methodType(thisMethod.getReturnType(), thisMethod.getParameterTypes()),
                declaringType
        ).bindTo(self).invokeWithArguments(args);
    }

    protected Object invokePrivate(final Class<T> declaringType, final Method thisMethod, final Object self, final Object[] args) throws Throwable {
        return MethodHandles.privateLookupIn(declaringType, MethodHandles.lookup())
                .in(declaringType)
                .unreflectSpecial(thisMethod, declaringType)
                .bindTo(self)
                .invokeWithArguments(args);
    }

    @Override
    public T proxy() throws ApplicationException {
        return this.proxy(null);
    }

    @Override
    public T proxy(final T existing) throws ApplicationException {
        // Proxy handlers can be reused, so we need to check if the proxy has already been created.
        if (this.proxyInstance != null)
            throw new IllegalStateException("Proxy already created, if you lost access to the original proxy instance, use #proxyInstance() instead. " +
                    "If you wish to expand the existing proxy by registering new " + MethodProxyContext.class.getSimpleName() + "s, you do not need to call #proxy() again.");

        if (this.type().isInterface()) return this.interfaceProxy();
        return this.newProxy(existing);
    }

    protected T interfaceProxy() {
        return new JavassistInterfaceHandler<>(this).proxy();
    }

    protected T newProxy(final T existing) throws ApplicationException {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(this.type().type());
        // As proxies can be expanded, we do not set a filter here early on.

        try {
            final T proxy = (T) factory.create(new Class<?>[0], new Object[0], this);
            // New proxy instances
            if (existing != null) this.restoreFields(existing, proxy);
            this.proxyInstance(proxy);
            return proxy;
        }
        catch (final InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public Exceptional<T> proxyInstance() {
        return Exceptional.of(this.proxyInstance);
    }

    @Override
    public Exceptional<T> instance() {
        return Exceptional.of(this.instance);
    }

    public void proxyInstance(final T proxyInstance) {
        this.proxyInstance = proxyInstance;
    }

    protected void restoreFields(final T existing, final T proxy) {
        final TypeContext<T> typeContext = this.applicationContext().environment().manager().isProxy(existing)
                ? this.type()
                : TypeContext.of(this.instance);
        for (final FieldContext<?> field : typeContext.fields()) {
            if (field.isStatic()) continue;
            field.set(proxy, field.get(existing).orNull());
        }
    }
}
