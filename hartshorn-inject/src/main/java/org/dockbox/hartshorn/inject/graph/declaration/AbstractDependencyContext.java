/*
 * Copyright 2019-2024 the original author or authors.
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

package org.dockbox.hartshorn.inject.graph.declaration;

import java.util.Set;

import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.collection.ComponentCollection;
import org.dockbox.hartshorn.inject.graph.ComponentMemberType;
import org.dockbox.hartshorn.inject.graph.DependencyMap;
import org.dockbox.hartshorn.inject.graph.DependencyResolutionType;
import org.dockbox.hartshorn.inject.processing.ComponentPostProcessor;
import org.dockbox.hartshorn.inject.provider.LifecycleType;
import org.dockbox.hartshorn.inject.scope.ScopeKey;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A simple implementation of {@link DependencyContext} that is used for components that are not managed by the container,
 * and rely on metadata that is provided by the declaration of the dependency. This context is typically used for
 * components that are created by the container, but are not automatically registered as a component.
 *
 * @param <T> the type of the component
 *
 * @see DependencyContext
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public abstract class AbstractDependencyContext<T> implements DependencyContext<T> {

    private final ComponentKey<T> componentKey;
    private final DependencyMap dependencies;
    private final ScopeKey scope;
    private final int priority;
    private final ComponentMemberType memberType;

    private boolean lazy;
    private LifecycleType lifecycleType;
    private boolean processAfterInitialization = true;

    protected AbstractDependencyContext(AbstractDependencyContextBuilder<T, ?> builder) {
        this.componentKey = builder.componentKey;
        this.dependencies = builder.dependencies;
        this.scope = builder.scope;
        this.priority = builder.priority;
        this.memberType = builder.memberType;
        this.lazy = builder.lazy;
        this.lifecycleType = builder.lifecycleType;
        this.processAfterInitialization = builder.processAfterInitialization;
    }

    protected AbstractDependencyContext(DependencyContext<T> dependencyContext) {
        this(
            dependencyContext.componentKey(),
            dependencyContext.dependencies(),
            dependencyContext.scope().orNull(),
            dependencyContext.priority(),
            dependencyContext.memberType()
        );

        if (dependencyContext instanceof AbstractDependencyContext<T> abstractDependencyContext) {
            this.lazy = abstractDependencyContext.lazy();
            this.lifecycleType = abstractDependencyContext.lifecycleType();
            this.processAfterInitialization = abstractDependencyContext.processAfterInitialization();
        }
        else {
            this.lazy = false;
            this.lifecycleType = LifecycleType.PROTOTYPE; // TODO: Dynamic?
            this.processAfterInitialization = true;
        }
    }

    protected AbstractDependencyContext(ComponentKey<T> componentKey, DependencyMap dependencies,
                                        ScopeKey scope, int priority, ComponentMemberType memberType) {
        this.componentKey = componentKey;
        this.dependencies = dependencies;
        this.scope = scope;
        this.priority = priority;
        this.memberType = memberType;
    }

    /**
     * Whether the component should be created lazily. If {@code true}, the component will only be created when it is
     * requested for the first time. If {@code false}, the component will be created when the container is initialized.
     *
     * <p>Only effective when the component's {@link #lifecycleType()} is {@link LifecycleType#SINGLETON 'Singleton'}.
     *
     * @param lazy whether the component should be created lazily
     * @return this context
     */
    public AbstractDependencyContext<T> lazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    /**
     * The lifecycle type of the component. The lifecycle type determines how the component is
     * managed by the container.
     *
     * @param lifecycleType the lifecycle type
     * @return this context
     */
    public AbstractDependencyContext<T> lifecycleType(LifecycleType lifecycleType) {
        this.lifecycleType = lifecycleType;
        return this;
    }

    /**
     * Whether the component should be processed using {@link ComponentPostProcessor}s after it has been initialized. If
     * {@code true}, the component will be processed after it has been initialized. If {@code false}, the component will
     * not be processed automatically.
     *
     * @param processAfterInitialization whether the component should be processed after it has been initialized
     * @return this context
     */
    public AbstractDependencyContext<T> processAfterInitialization(boolean processAfterInitialization) {
        this.processAfterInitialization = processAfterInitialization;
        return this;
    }

    @Override
    public ComponentKey<T> componentKey() {
        return this.componentKey;
    }

    @Override
    public DependencyMap dependencies() {
        return this.dependencies;
    }

    @Override
    public Set<ComponentKey<?>> dependencies(DependencyResolutionType resolutionType) {
        return Set.copyOf(this.dependencies.get(resolutionType));
    }

    @Override
    public boolean needsImmediateResolution(ComponentKey<?> dependencyCandidate) {
        return this.dependencies(DependencyResolutionType.IMMEDIATE).contains(dependencyCandidate);
    }

    @Override
    public int priority() {
        return this.priority;
    }

    @Override
    public Option<ScopeKey> scope() {
        return Option.of(this.scope);
    }

    @Override
    public ComponentMemberType memberType() {
        return this.memberType;
    }

    /**
     * Whether the component should be created lazily. If {@code true}, the component will only be created when it is
     * requested for the first time. If {@code false}, the component will be created when the container is initialized.
     *
     * <p>Only effective when the component's {@link #lifecycleType()} is {@link LifecycleType#SINGLETON 'Singleton'}.
     *
     * @return whether the component should be created lazily
     */
    @Override
    public boolean lazy() {
        return this.lazy;
    }

    /**
     * The lifecycle type of the component. The lifecycle type determines how the component is
     * managed by the container.
     *
     * @return the lifecycle type
     */
    @Override
    public LifecycleType lifecycleType() {
        return this.lifecycleType;
    }

    /**
     * Whether the component should be processed using {@link ComponentPostProcessor}s after it has been initialized. If
     * {@code true}, the component will be processed after it has been initialized. If {@code false}, the component will
     * not be processed automatically.
     *
     * @return whether the component should be processed after it has been initialized
     */
    @Override
    public boolean processAfterInitialization() {
        return this.processAfterInitialization;
    }

    /**
     * Base class for builders of {@link AbstractDependencyContext} instances.
     *
     * @param <T> the type of the component
     * @param <B> the type of the builder
     *
     * @see AbstractDependencyContext
     * @see DependencyContext
     *
     * @since 0.5.0
     *
     * @author Guus Lieben
     */
    public abstract static class AbstractDependencyContextBuilder<T, B extends AbstractDependencyContextBuilder<T, B>> {

        private final ComponentKey<T> componentKey;

        private DependencyMap dependencies;
        private ScopeKey scope;
        private int priority;
        private ComponentMemberType memberType;
        private boolean lazy;
        private LifecycleType lifecycleType;
        private boolean processAfterInitialization;

        protected AbstractDependencyContextBuilder(ComponentKey<T> componentKey) {
            this.componentKey = componentKey;
        }

        /**
         * Sets the dependencies of the component, which should be satisfied by the container before
         * this component can be created.
         *
         * @param dependencies the dependencies of the component
         * @return this builder
         */
        public B dependencies(DependencyMap dependencies) {
            this.dependencies = dependencies;
            return this.self();
        }

        /**
         * Sets the scope of the component. The scope determines the lifecycle of the component.
         *
         * @param scope the scope of the component
         * @return this builder
         */
        public B scope(ScopeKey scope) {
            this.scope = scope;
            return this.self();
        }

        /**
         * Sets the priority of the component. The priority is used to determine which components
         * are actually created when there are multiple components that satisfy the same dependency.
         *
         * @param priority the priority of the component
         * @return this builder
         */
        public B priority(int priority) {
            this.priority = priority;
            return this.self();
        }

        /**
         * Sets the member type of the component. The member type determines whether the component is
         * capable of existing on its own, or whether it is a member of a composite {@link ComponentCollection}.
         *
         * @param memberType the member type of the component
         * @return this builder
         */
        public B memberType(ComponentMemberType memberType) {
            this.memberType = memberType;
            return this.self();
        }

        /**
         * Whether the component should be created lazily. If {@code true}, the component will only be created when it is
         * requested for the first time. If {@code false}, the component will be created when the container is initialized.
         *
         * <p>Only effective when the component's {@link #lifecycleType()} is {@link LifecycleType#SINGLETON 'Singleton'}.
         *
         * @param lazy whether the component should be created lazily
         * @return this builder
         */
        public B lazy(boolean lazy) {
            this.lazy = lazy;
            return this.self();
        }

        /**
         * The lifecycle type of the component. The lifecycle type determines how the component is
         * managed by the container.
         *
         * @param lifecycleType the lifecycle type
         * @return this builder
         */
        public B lifecycleType(LifecycleType lifecycleType) {
            this.lifecycleType = lifecycleType;
            return this.self();
        }

        /**
         * Whether the component should be processed using {@link ComponentPostProcessor}s after it has been initialized. If
         * {@code true}, the component will be processed after it has been initialized. If {@code false}, the component will
         * not be processed automatically.
         *
         * @param processAfterInitialization whether the component should be processed after it has been initialized
         * @return this builder
         */
        public B processAfterInitialization(boolean processAfterInitialization) {
            this.processAfterInitialization = processAfterInitialization;
            return this.self();
        }

        protected abstract B self();

        public abstract DependencyContext<T> build();
    }
}
