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

package org.dockbox.hartshorn.core.context;

import org.dockbox.hartshorn.core.annotations.activate.UseBootstrap;
import org.dockbox.hartshorn.core.annotations.activate.ServiceActivator;
import org.dockbox.hartshorn.core.annotations.activate.UseProxying;
import org.dockbox.hartshorn.core.boot.ApplicationManager;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * The environment of an active application. The environment is managed by a {@link ApplicationManager} and can be
 * responsible for multiple {@link ApplicationContext}s, though typically only one {@link ApplicationContext} is bound
 * to the {@link ApplicationEnvironment}.
 */
public interface ApplicationEnvironment {

    /**
     * Gets the context of all registered prefixes. This context is responsible for keeping track of known prefixes,
     * and the components known within those prefixes.
     * @return The context of all registered prefixes
     */
    PrefixContext prefixContext();

    /**
     * Indicates whether the current environment exists within a Continuous Integration environment. If this returns
     * <code>true</code> this indicates the application is not active in a production environment. For example, the
     * default test suite for the framework will indicate the environment acts as a CI environment.
     *
     * @return <code>true</code> if the environment is a CI environment, <code>false</code> otherwise.
     */
    boolean isCI();

    /**
     * Gets the {@link ApplicationManager} responsible for managing this environment.
     *
     * @return The {@link ApplicationManager} responsible for managing this environment.
     */
    ApplicationManager manager();

    /**
     * Registers the given prefix, allowing it to be indexed for components.
     *
     * @param prefix The prefix to register.
     */
    void prefix(String prefix);

    /**
     * Gets types decorated with a given annotation, both classes and annotations.
     *
     * @param <A> The annotation constraint
     * @param annotation The annotation expected to be present on one or more types
     * @return The annotated types
     */
    <A extends Annotation> Collection<TypeContext<?>> types(final Class<A> annotation);

    /**
     * Gets types decorated with a given annotation, both classes and annotations. The prefix is typically a package.
     * If <code>skipParents</code> is true, the type will only be included if it is annotated directly.
     *
     * @param prefix The prefix to scan for annotated types
     * @param annotation The annotation expected to be present on one or more types
     * @param skipParents Whether to skip the parent types
     * @param <A> The annotation constraint
     * @return The annotated types
     */
    <A extends Annotation> Collection<TypeContext<?>> types(final String prefix, final Class<A> annotation, final boolean skipParents);

    /**
     * Gets types decorated with a given annotation, both classes and annotations. If <code>skipParents</code> is
     * true, the type will only be included if it is annotated directly.
     *
     * @param <A> The annotation constraint
     * @param annotation The annotation expected to be present on one or more types
     * @param skipParents Whether to include the type if supertypes are annotated
     * @return The annotated types
     */
    <A extends Annotation> Collection<TypeContext<?>> types(final Class<A> annotation, final boolean skipParents);

    /**
     * Gets all sub-types of a given type. The prefix is typically a package. If no sub-types exist for the given type,
     * and empty list is returned.
     *
     * @param parent The parent type to scan for subclasses
     * @param <T> The type of the parent
     * @return The list of sub-types, or an empty list
     */
    <T> Collection<TypeContext<? extends T>> children(final TypeContext<T> parent);

    /**
     * Gets all sub-types of a given type. The prefix is typically a package. If no sub-types exist for the given type,
     * and empty list is returned.
     *
     * @param parent The parent type to scan for subclasses
     * @param <T> The type of the parent
     * @return The list of sub-types, or an empty list
     */
    <T> Collection<TypeContext<? extends T>> children(final Class<T> parent);

    /**
     * Gets annotations of the given type, which are decorated with the given annotation. For example, if the given
     * annotation is {@link ServiceActivator} on the application
     * activator, the results will include all service activators like {@link UseBootstrap} and {@link UseProxying}.
     *
     * @param type The type to scan for annotations
     * @param annotation The annotation expected to be present on zero or more annotations
     * @return The annotated annotations
     */
    List<Annotation> annotationsWith(final TypeContext<?> type, final Class<? extends Annotation> annotation);
}
