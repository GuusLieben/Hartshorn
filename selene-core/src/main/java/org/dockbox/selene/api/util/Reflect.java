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

package org.dockbox.selene.api.util;

import org.dockbox.selene.api.annotations.Rejects;
import org.dockbox.selene.api.annotations.entity.Accessor;
import org.dockbox.selene.api.annotations.entity.Extract;
import org.dockbox.selene.api.annotations.entity.Extract.Behavior;
import org.dockbox.selene.api.annotations.entity.Ignore;
import org.dockbox.selene.api.annotations.entity.Metadata;
import org.dockbox.selene.api.annotations.entity.Property;
import org.dockbox.selene.api.annotations.module.OwnedBy;
import org.dockbox.selene.api.exceptions.TypeRejectedException;
import org.dockbox.selene.api.module.ModuleContainer;
import org.dockbox.selene.api.module.ModuleManager;
import org.dockbox.selene.api.objects.Exceptional;
import org.dockbox.selene.api.server.Selene;
import org.dockbox.selene.api.server.Server;
import org.dockbox.selene.api.util.SeleneUtils.Provision;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings({ "unused", "OverlyComplexClass" })
public final class Reflect {

    private static final Map<String, Reflections> reflectedPrefixes = SeleneUtils.emptyConcurrentMap();
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = SeleneUtils.ofEntries(
            SeleneUtils.entry(boolean.class, Boolean.class),
            SeleneUtils.entry(byte.class, Byte.class),
            SeleneUtils.entry(char.class, Character.class),
            SeleneUtils.entry(double.class, Double.class),
            SeleneUtils.entry(float.class, Float.class),
            SeleneUtils.entry(int.class, Integer.class),
            SeleneUtils.entry(long.class, Long.class),
            SeleneUtils.entry(short.class, Short.class));

    private Reflect() {}

    @SuppressWarnings("unchecked")
    public static <T> Exceptional<T> getFieldValue(Class<?> fieldHolder, Object instance, String field, Class<T> expectedType) {
        try {
            Field declaredField = fieldHolder.getDeclaredField(field);
            declaredField.setAccessible(true);
            T value = (T) getFieldValue(declaredField, instance);
            return Exceptional.of(value);
        }
        catch (ClassCastException | ReflectiveOperationException e) {
            return Exceptional.of(e);
        }
    }

    public static Exceptional<?> getFieldValue(Field field, Object instance) {
        if (field.isAnnotationPresent(Accessor.class)) {
            Accessor accessor = field.getAnnotation(Accessor.class);
            if (!accessor.getter().equals("")) {
                return getMethodValue(instance, accessor.getter(), field.getType());
            }
        }
        try {
            return Exceptional.of(field.get(instance));
        }
        catch (IllegalAccessException | IllegalArgumentException e) {
            return Exceptional.of(e);
        }
    }

    /**
     * Attempts to get the return value of a method which may not be publicly accessible (e.g.
     * protected or private). If the method does not exist, or throws a exception the caught is wrapped
     * in a {@link Exceptional}. Otherwise the (nullable) return value is returned wrapped in a {@link
     * Exceptional}.
     *
     * @param <T>
     *         The type of the expected return value
     * @param instance
     *         The instance to call the method on
     * @param method
     *         The method to call
     * @param expectedType
     *         The type of the expected return value
     * @param args
     *         The arguments which are provided to the method call
     *
     * @return The result of the method call, wrapped in {@link Exceptional}
     */
    public static <T> Exceptional<T> getMethodValue(Object instance, String method, Class<T> expectedType, Object... args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        return Reflect.getMethodValue(
                instance.getClass(), instance, method, expectedType, argTypes, args);
    }

    /**
     * Attempts to get the return value of a method which may not be publicly accessible (e.g.
     * protected or private). If the method does not exist, or throws a exception the caught is wrapped
     * in a {@link Exceptional}. Otherwise the (nullable) return value is returned wrapped in a {@link
     * Exceptional}.
     *
     * @param <T>
     *         The type of the expected return value
     * @param methodHolder
     *         The type to call the method on
     * @param instance
     *         The instance to call the method with
     * @param method
     *         The method to call
     * @param expectedType
     *         The type of the expected return value
     * @param argumentTypes
     *         The types of the arguments, used to collect the appropriate method
     * @param args
     *         The arguments which are provided to the method call
     *
     * @return The result of the method call, wrapped in {@link Exceptional}
     */
    @SuppressWarnings("unchecked")
    public static <T> Exceptional<T> getMethodValue(
            Class<?> methodHolder,
            Object instance,
            String method,
            Class<T> expectedType,
            Class<?>[] argumentTypes,
            Object... args
    ) {
        try {
            Method m = methodHolder.getDeclaredMethod(method, argumentTypes);
            if (!m.isAccessible()) m.setAccessible(true);
            T value = (T) m.invoke(instance, args);
            return Exceptional.of(value);
        }
        catch (ClassCastException
                | NoSuchMethodException
                | InvocationTargetException
                | IllegalAccessException e) {
            return Exceptional.of(e);
        }
    }

    @Contract("null, _ -> false; !null, null -> false")
    public static <T> boolean isGenericInstanceOf(T instance, Class<?> type) {
        return null != instance && Reflect.isAssignableFrom(type, instance.getClass());
    }

    /**
     * Returns true if {@code to} is equal to-, a super type of, or a primitive wrapper of {@code
     * from}.
     *
     * <p>Primitive wrappers include all JDK wrappers for native types (int, char, double, etc). E.g.
     * all of the following assignabilities return true:
     *
     * <pre>{@code
     * SeleneUtils.isAssignableFrom(int.class, Integer.class);
     * SeleneUtils.isAssignableFrom(Integer.class, int.class);
     * SeleneUtils.isAssignableFrom(int.class, int.class);
     * SeleneUtils.isAssignableFrom(Number.class, Integer.class);
     *
     * }</pre>
     *
     * @param to
     *         The possible (super) type or primite wrapper of {@code from}
     * @param from
     *         The type to compare assignability against
     *
     * @return true if {@code to} is equal to-, a super type of, or a primitive wrapper of {@code
     *         from}
     * @see Reflect#isPrimitiveWrapperOf(Class, Class)
     */
    public static boolean isAssignableFrom(Class<?> to, Class<?> from) {
        if (null == to || null == from) return false;
        //noinspection ConstantConditions
        if (to == from || to.equals(from)) return true;

        if (to.isAssignableFrom(from)) {
            return true;
        }
        if (from.isPrimitive()) {
            return Reflect.isPrimitiveWrapperOf(to, from);
        }
        if (to.isPrimitive()) {
            return Reflect.isPrimitiveWrapperOf(from, to);
        }
        return false;
    }

    /**
     * Returns true if {@code targetClass} is a primitive wrapper of {@code primitive}.
     *
     * @param targetClass
     *         The primitive wrapper (e.g. Integer)
     * @param primitive
     *         The primitive type (e.g. int)
     *
     * @return true if {@code targetClass} is a primitive wrapper of {@code primitive}.
     */
    public static boolean isPrimitiveWrapperOf(Class<?> targetClass, Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            throw new IllegalArgumentException("First argument has to be primitive type");
        }
        return primitiveWrapperMap.get(primitive) == targetClass;
    }

    /**
     * Gets all methods annotated by a given annotation, which match the given rule, inside a class.
     * If the method is not annotated with the given annotation, or does not match the given rule, it
     * will not be returned. Also collects methods from parent types of the class.
     *
     * @param <A>
     *         The annotation constraint
     * @param clazz
     *         The class to scan for methods
     * @param annotation
     *         The annotation expected to be present on one or more methods
     * @param rule
     *         The additional rule for methods to match
     *
     * @return All methods annotated by a given annotation, which match the given rule.
     */
    @NotNull
    @Unmodifiable
    public static <A extends Annotation> Collection<Method> getAnnotedMethods(Class<?> clazz, Class<A> annotation, Predicate<A> rule) {
        return Reflect.getAnnotedMethods(clazz, annotation, rule, false);
    }

    /**
     * Gets all methods annotated by a given annotation, which match the given rule, inside a class.
     * If the method is not annotated with the given annotation, or does not match the given rule, it
     * will not be returned. Also collects methods from parent types of the class if {@code
     * skipParents} is false.
     *
     * @param <A>
     *         The annotation constraint
     * @param clazz
     *         The class to scan for methods
     * @param annotation
     *         The annotation expected to be present on one or more methods
     * @param rule
     *         The additional rule for methods to match
     * @param skipParents
     *         Whether or not to include methods in parent types
     *
     * @return All methods annotated by a given annotation, which match the given rule.
     */
    @NotNull
    @Unmodifiable
    public static <A extends Annotation> Collection<Method> getAnnotedMethods(Class<?> clazz, Class<A> annotation, Predicate<A> rule, boolean skipParents) {
        List<Method> annotatedMethods = SeleneUtils.emptyList();
        for (Method method : SeleneUtils.asList(skipParents ? clazz.getMethods() : clazz.getDeclaredMethods())) {
            if (!method.isAccessible()) method.setAccessible(true);
            if (method.isAnnotationPresent(annotation) && rule.test(method.getAnnotation(annotation))) {
                annotatedMethods.add(method);
            }
        }
        return SeleneUtils.asUnmodifiableList(annotatedMethods);
    }

    /**
     * Gets types annotated with a given annotation, both classes and annotations. The prefix is
     * typically a package. If the annotation is present on a parent of the type, the highest level
     * member will be included.
     *
     * @param <A>
     *         The annotation constraint
     * @param prefix
     *         The package prefix
     * @param annotation
     *         The annotation expected to be present on one or more types
     *
     * @return The annotated types
     */
    public static <A extends Annotation> Collection<Class<?>> getAnnotatedTypes(String prefix, Class<A> annotation) {
        return Reflect.getAnnotatedTypes(prefix, annotation, false);
    }

    /**
     * Gets types annotated with a given annotation, both classes and annotations. The prefix is
     * typically a package. If the annotation is present on a parent of the type, it will only be
     * included if {@code skipParents} is false.
     *
     * @param <A>
     *         The annotation constraint
     * @param prefix
     *         The package prefix
     * @param annotation
     *         The annotation expected to be present on one or more types
     * @param skipParents
     *         Whether or not to include the type if supertypes are annotated
     *
     * @return The annotated types
     */
    public static <A extends Annotation> Collection<Class<?>> getAnnotatedTypes(String prefix, Class<A> annotation, boolean skipParents) {
        Reflections reflections = getReflectedPrefix(prefix);
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(annotation, !skipParents);
        return SeleneUtils.asList(types);
    }

    private static Reflections getReflectedPrefix(String prefix) {
        if (!reflectedPrefixes.containsKey(prefix)) {
            reflectedPrefixes.put(prefix, new Reflections(prefix));
        }
        return reflectedPrefixes.get(prefix);
    }

    /**
     * Gets all sub-types of a given type. The prefix is typically a package. If no sub-types exist
     * for the given type, and empty list is returned.
     *
     * @param prefix
     *         The package prefix
     * @param parent
     *         The parent type to scan for subclasses
     * @param <T>
     *         The type of the parent
     *
     * @return The list of sub-types, or a empty list
     */
    public static <T> Collection<Class<? extends T>> getSubTypes(String prefix, Class<T> parent) {
        Reflections reflections = getReflectedPrefix(prefix);
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(parent);
        return SeleneUtils.asList(subTypes);
    }

    /**
     * Is either assignable from boolean.
     *
     * @param to
     *         the to
     * @param from
     *         the from
     *
     * @return the boolean
     */
    public static boolean isEitherAssignableFrom(Class<?> to, Class<?> from) {
        return Reflect.isAssignableFrom(from, to) || Reflect.isAssignableFrom(to, from);
    }

    /**
     * Gets static fields.
     *
     * @param type
     *         the type
     *
     * @return the static fields
     */
    public static Collection<Field> getStaticFields(Class<?> type) {
        Field[] declaredFields = type.getDeclaredFields();
        Collection<Field> staticFields = SeleneUtils.emptyList();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                staticFields.add(field);
            }
        }
        return staticFields;
    }

    /**
     * Gets enum values.
     *
     * @param type
     *         the type
     *
     * @return the enum values
     */
    public static Collection<? extends Enum<?>> getEnumValues(Class<?> type) {
        if (!type.isEnum()) return SeleneUtils.emptyList();
        Collection<Enum<?>> constants = SeleneUtils.emptyList();
        try {
            Field f = type.getDeclaredField("$VALUES");
            if (!f.isAccessible()) f.setAccessible(true);
            Object o = f.get(null);
            Enum<?>[] e = (Enum<?>[]) o;
            constants.addAll(Arrays.asList(e));
        }
        catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | ClassCastException e) {
            Selene.log().warn("Error obtaining enum constants in " + type.getCanonicalName(), e);
        }
        return constants;
    }

    /**
     * Is annotation present recursively boolean.
     *
     * @param <T>
     *         the type parameter
     * @param method
     *         the method
     * @param annotationClass
     *         the annotation class
     *
     * @return the boolean
     * @throws SecurityException
     *         the security exception
     */
    public static <T extends Annotation> boolean isAnnotationPresentRecursively(Method method, Class<T> annotationClass) throws SecurityException {
        return null != Reflect.getAnnotationRecursively(method, annotationClass);
    }

    /**
     * Gets annotation recursively.
     *
     * @param <T>
     *         the type parameter
     * @param method
     *         the method
     * @param annotationClass
     *         the annotation class
     *
     * @return the annotation recursively
     * @throws SecurityException
     *         the security exception
     */
    public static <T extends Annotation> T getAnnotationRecursively(Method method, Class<T> annotationClass) throws SecurityException {
        T result;
        if (null == (result = method.getAnnotation(annotationClass))) {
            final String name = method.getName();
            final Class<?>[] params = method.getParameterTypes();

            Class<?> declaringClass = method.getDeclaringClass();
            for (Class<?> supertype : Reflect.getSupertypes(declaringClass)) {
                try {
                    Method m = supertype.getDeclaredMethod(name, params);

                    // Static method doesn't override
                    if (Modifier.isStatic(m.getModifiers())) break;

                    if (null != (result = m.getAnnotation(annotationClass))) break;
                }
                catch (NoSuchMethodException ignored) {
                    // Current class doesn't have Reflect method
                }
            }
        }
        return result;
    }

    /**
     * Gets supertypes.
     *
     * @param current
     *         the current
     *
     * @return the supertypes
     */
    public static Collection<Class<?>> getSupertypes(Class<?> current) {
        Set<Class<?>> supertypes = SeleneUtils.emptySet();
        Set<Class<?>> next = SeleneUtils.emptySet();
        Class<?> superclass = current.getSuperclass();

        if (Object.class != superclass && null != superclass) {
            supertypes.add(superclass);
            next.add(superclass);
        }

        for (Class<?> interfaceClass : current.getInterfaces()) {
            supertypes.add(interfaceClass);
            next.add(interfaceClass);
        }

        for (Class<?> cls : next) {
            supertypes.addAll(Reflect.getSupertypes(cls));
        }

        return supertypes;
    }

    /**
     * Gets methods recursively.
     *
     * @param cls
     *         the cls
     *
     * @return the methods recursively
     * @throws SecurityException
     *         the security exception
     */
    public static List<Method> getMethodsRecursively(Class<?> cls) throws SecurityException {
        try {
            Set<InternalMethodWrapper> set = SeleneUtils.emptySet();
            Class<?> current = cls;
            do {
                Method[] methods = current.getDeclaredMethods();
                for (Method m : methods) {
                    // if there's already a method that is overriding the current method, add() will return
                    // false
                    set.add(new InternalMethodWrapper(m));
                }
            }
            while (Object.class != (current = current.getSuperclass()) && null != current);

            // Guava equivalent:       Lists.transform(set, w -> w.method);
            // Stream API equivalent:  set.stream().map(w -> w.method).collect(Collectors.toList());
            List<Method> result = SeleneUtils.emptyList();
            for (InternalMethodWrapper methodWrapper : set) result.add(methodWrapper.method);
            return result;
        }
        catch (Throwable e) {
            return SeleneUtils.emptyList();
        }
    }

    /**
     * Gets module.
     *
     * @param type
     *         the type
     *
     * @return the module
     */
    @Nullable
    public static ModuleContainer getModule(Class<?> type) {
        if (null == type) return null;
        if (type.equals(Selene.class))
            return Reflect.getModule(Selene.provide(Server.class).getClass());

        if (type.isAnnotationPresent(OwnedBy.class)) {
            OwnedBy owner = type.getAnnotation(OwnedBy.class);
            return Reflect.getModule(owner.value());
        }

        return Selene.getServer()
                    .getInstanceSafe(ModuleManager.class)
                    .map(em -> em.getContainer(type).orNull())
                    .orNull();
    }

    /**
     * Run with module t.
     *
     * @param <T>
     *         the type parameter
     * @param type
     *         the type
     * @param function
     *         the function
     *
     * @return the t
     */
    @Nullable
    public static <T> T runWithModule(Class<?> type, Function<ModuleContainer, T> function) {
        ModuleContainer module = Reflect.getModule(type);
        if (null != module) return function.apply(module);
        return null;
    }

    /**
     * Run with module.
     *
     * @param type
     *         the type
     * @param consumer
     *         the consumer
     */
    public static void runWithModule(Class<?> type, Consumer<ModuleContainer> consumer) {
        ModuleContainer module = Reflect.getModule(type);
        if (null != module) consumer.accept(module);
    }

    /**
     * Run with module.
     *
     * @param <T>
     *         the type parameter
     * @param type
     *         the type
     * @param consumer
     *         the consumer
     */
    public static <T> void runWithInstance(Class<T> type, Consumer<T> consumer) {
        T instance = Selene.provide(type);
        if (null != instance) consumer.accept(instance);
    }

    @Nullable
    public static String getClassAlias(Class<?> type) {
        String className = null;
        if (type.isAnnotationPresent(Metadata.class))
            className = type.getAnnotation(Metadata.class).alias();
        return className;
    }

    /**
     * Try create from map exceptional.
     *
     * @param <T>
     *         the type parameter
     * @param type
     *         the type
     * @param map
     *         the map
     *
     * @return the exceptional
     */
    public static <T> Exceptional<T> tryCreateFromMap(Class<T> type, Map<String, Object> map) {
        return Reflect.tryCreateFromProcessed(type, key -> map.getOrDefault(key, null), true);
    }

    /**
     * Try create from processed exceptional.
     *
     * @param <T>
     *         the type parameter
     * @param type
     *         the type
     * @param valueCollector
     *         the value collector
     * @param inject
     *         the inject
     *
     * @return the exceptional
     */
    public static <T> Exceptional<T> tryCreateFromProcessed(
            Class<T> type, Function<String, Object> valueCollector, boolean inject) {
        return Reflect.tryCreate(type, valueCollector, inject, Provision.FIELD_NAME);
    }

    /**
     * Try create exceptional.
     *
     * @param <T>
     *         the type parameter
     * @param <A>
     *         the type parameter
     * @param type
     *         the type
     * @param valueCollector
     *         the value collector
     * @param inject
     *         the inject
     * @param provision
     *         the provision
     *
     * @return the exceptional
     */
    public static <T, A> Exceptional<T> tryCreate(Class<T> type, Function<A, Object> valueCollector, boolean inject, Provision provision) {
        T instance = inject ? Selene.provide(type) : Reflect.getInstance(type);
        if (null != instance)
            try {
                for (Field field : type.getDeclaredFields()) {
                    if (!field.isAccessible()) field.setAccessible(true);
                    if (field.isAnnotationPresent(Ignore.class)) continue;

                    Object value = extractFieldValue(field, provision, valueCollector);
                    if (null == value) continue;

                    boolean usedSetter = canUseSetter(type, instance, field, value);

                    if (!usedSetter && Reflect.isAssignableFrom(field.getType(), value.getClass()))
                        field.set(instance, value);
                }
            }
            catch (IllegalAccessException
                    | NoSuchMethodException
                    | InvocationTargetException
                    | ClassCastException e) {
                return Exceptional.of(e);
            }
        return Exceptional.of(instance);
    }

    /**
     * Gets instance.
     *
     * @param <T>
     *         the type parameter
     * @param clazz
     *         the clazz
     *
     * @return the instance
     */
    public static <T> T getInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getConstructor();
            return ctor.newInstance();
        }
        catch (NoSuchMethodException
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException e) {
            return Selene.provide(clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private static <A> Object extractFieldValue(Field field, Provision provision, Function<A, Object> valueCollector) {
        if (Provision.FIELD == provision) return valueCollector.apply((A) field);
        else {
            String fieldName = Reflect.getFieldPropertyName(field);
            return valueCollector.apply((A) fieldName);
        }
    }

    private static <T> boolean canUseSetter(Class<T> type, T instance, Field field, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (field.isAnnotationPresent(Property.class)) {
            Property property = field.getAnnotation(Property.class);

            if (!property.setter().isEmpty() && Reflect.hasMethod(type, property.setter())) {
                Class<?> parameterType = field.getType();
                if (Reflect.isNotVoid(property.accepts())) parameterType = property.accepts();

                Method method = type.getMethod(property.setter(), parameterType);
                method.invoke(instance, value);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets field property name.
     *
     * @param field
     *         the field
     *
     * @return the field property name
     */
    public static String getFieldPropertyName(Field field) {
        return field.isAnnotationPresent(Property.class)
                ? field.getAnnotation(Property.class).value()
                : field.getName();
    }

    /**
     * Returns true if a type has a declared method of which the name equals the value of {@code
     * method} and has no parameters.
     *
     * @param type
     *         The type
     * @param method
     *         The name of the method to check for
     *
     * @return true if the type has a declared method which matches {@code method}
     */
    public static boolean hasMethod(Class<?> type, @NonNls String method) {
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().equals(method)) return true;
        }
        return false;
    }

    /**
     * Is not void boolean.
     *
     * @param type
     *         the type
     *
     * @return the boolean
     */
    public static boolean isNotVoid(Class<?> type) {
        return !(type.equals(Void.class) || type == Void.TYPE);
    }

    /**
     * Try create from raw exceptional.
     *
     * @param <T>
     *         the type parameter
     * @param type
     *         the type
     * @param valueCollector
     *         the value collector
     * @param inject
     *         the inject
     *
     * @return the exceptional
     */
    public static <T> Exceptional<T> tryCreateFromRaw(Class<T> type, Function<Field, Object> valueCollector, boolean inject) {
        return Reflect.tryCreate(type, valueCollector, inject, Provision.FIELD);
    }

    /**
     * Returns true if a instance has a declared method of which the name equals the value of {@code
     * method} and has no parameters.
     *
     * @param instance
     *         The instance
     * @param method
     *         The name of the method to check for
     *
     * @return true if the type of the instance has a declared method which matches {@code method}
     */
    public static boolean hasMethod(Object instance, String method) {
        return Reflect.hasMethod(instance.getClass(), method);
    }

    public static boolean hasFieldRecursive(Class<?> type, String field) {
        Class<?> original = type;
        while (null != type) {
            try {
                if (field.contains("*") && !field.contains("?")) {
                    type.getDeclaredField(field);
                    return true;
                }
            }
            catch (ReflectiveOperationException e) {
                type = type.getSuperclass();
            }
        }
        return false;
    }

    public static boolean rejects(Class<?> holder, Class<?> potentialReject) {
        return Reflect.rejects(holder, potentialReject, false);
    }

    public static boolean rejects(Class<?> holder, Class<?> potentialReject, boolean throwIfRejected) {
        if (holder.isAnnotationPresent(Rejects.class)) {
            Rejects rejects = holder.getAnnotation(Rejects.class);

            boolean rejected = false;
            for (Class<?> rejectedType : rejects.value())
                if (potentialReject.isAssignableFrom(rejectedType)) rejected = true;

            if (rejected && throwIfRejected) throw new TypeRejectedException(potentialReject, holder);
            return rejected;
        }
        return false;
    }

    public static void forEachFieldIn(Class<?> type, BiConsumer<Class<?>, Field> consumer) {
        for (Field declaredField : type.getDeclaredFields()) {
            consumer.accept(type, declaredField);
        }
        if (null != type.getSuperclass()) Reflect.forEachFieldIn(type.getSuperclass(), consumer);
    }

    public static Collection<Field> getAccessibleFields(Class<?> type) {
        if (type.isAnnotationPresent(Extract.class)) {
            Extract extract = type.getAnnotation(Extract.class);
            Behavior behavior = extract.value();
            if (behavior == Behavior.KEEP) {
                return getAllNonSkippedFields(type);
            }
            else if (behavior == Behavior.SKIP) return getAllKeptFields(type);
            else throw new IllegalArgumentException("Unsupported behavior " + behavior);
        }
        else return getAllKeptFields(type);
    }

    private static Collection<Field> getAllNonSkippedFields(Class<?> type) {
        Collection<Field> fields = SeleneUtils.emptySet();
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Extract.class)) {
                Extract extract = field.getAnnotation(Extract.class);
                if (extract.value() == Behavior.KEEP) fields.add(field);
            }
            else if (!field.isAnnotationPresent(Ignore.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static Collection<Field> getAllKeptFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Extract.class))
                .filter(field -> field.getAnnotation(Extract.class).value() == Behavior.KEEP)
                .collect(Collectors.toSet());
    }

    public static void setFieldValue(Field field, Object to, Object value) {
        try {
            if (field.isAnnotationPresent(Accessor.class)) {
                Accessor accessor = field.getAnnotation(Accessor.class);
                Method setter = to.getClass().getDeclaredMethod(accessor.setter(), value.getClass());
                setter.setAccessible(true);
                setter.invoke(to, value);
                return;
            }
            field.set(to, value);
        }
        catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Selene.handle(e);
        }
    }

    public static Collection<Field> getAnnotatedFields(Class<? extends Annotation> annotation, Class<?> type) {
        Collection<Field> fields = new ArrayList<>();
        for (Field declaredField : type.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(annotation)) fields.add(declaredField);
        }
        if (type.getSuperclass() != null) fields.addAll(getAnnotatedFields(annotation, type.getSuperclass()));
        return fields;
    }

    public static <T> Collection<Constructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotation, Class<T> type) {
        Collection<Constructor<T>> constructors = SeleneUtils.emptyList();
        //noinspection unchecked
        for (Constructor<T> constructor : (Constructor<T>[]) type.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(annotation)) constructors.add(constructor);
        }
        return constructors;
    }
}
