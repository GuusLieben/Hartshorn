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

package org.dockbox.hartshorn.core.services;

import org.dockbox.hartshorn.core.ComponentType;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.HashSetMultiMap;
import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.stereotype.Component;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import lombok.Getter;

public class ComponentLocatorImpl implements ComponentLocator {

    private final MultiMap<String, ComponentContainer> cache = new HashSetMultiMap<>();
    @Getter
    private final ApplicationContext applicationContext;
    private final Set<ComponentActivationFilter> activationFilters = HartshornUtils.emptyConcurrentSet();

    public ComponentLocatorImpl(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.registerDefaultActivationFilters();
    }

    protected void registerDefaultActivationFilters() {
        this.registerActivationFilter(new ActivatorPresenceActivationFilter(this.applicationContext()));
        this.registerActivationFilter(new TypePresenceActivationFilter());
    }

    public void registerActivationFilter(final ComponentActivationFilter activationFilter) {
        if (activationFilter == null) return;
        this.activationFilters.add(activationFilter);
    }

    @Override
    public void register(final String prefix) {
        if (this.cache.containsKey(prefix)) return;

        this.applicationContext().log().debug("Registering prefix '" + prefix + "' for component locating");

        final long start = System.currentTimeMillis();

        final List<TypeContext<?>> newComponentTypes = this.applicationContext().environment()
                .types(prefix, Component.class, false)
                .stream()
                .filter(type -> this.cache.allValues().stream().noneMatch(container -> container.type().equals(type)))
                .toList();

        final List<ComponentContainer> newComponentContainers = newComponentTypes.stream()
                .map(type -> new ComponentContainerImpl(this.applicationContext(), type))
                .filter(container -> !container.type().isAnnotation()) // Exclude extended annotations
                .map(ComponentContainer.class::cast).toList();

        final List<ComponentContainer> filteredComponentContainers = newComponentContainers.stream()
                .filter(container -> this.activationFilters.stream().allMatch(activationFilter -> activationFilter.doActivate(container.type(), container)))
                .toList();

        final long duration = System.currentTimeMillis() - start;
        this.applicationContext().log().info("Located %d components with prefix %s in %dms".formatted(filteredComponentContainers.size(), prefix, duration));

        this.cache.putAll(prefix, filteredComponentContainers);
    }

    @Override
    public Collection<ComponentContainer> containers() {
        return this.cache.entrySet().stream().flatMap(a -> a.getValue().stream()).toList();
    }

    @Override
    public Collection<ComponentContainer> containers(final ComponentType componentType) {
        return this.containers().stream()
                .filter(container -> container.componentType() == componentType)
                .toList();
    }

    @Override
    public Exceptional<ComponentContainer> container(final TypeContext<?> type) {
        return Exceptional.of(this.containers()
                .stream()
                .filter(container -> container.type().equals(type))
                .findFirst()
        );
    }

    @Override
    public <T> void validate(final Key<T> key) {
        final TypeContext<T> contract = key.type();
        if (contract.annotation(Component.class).present() && this.container(contract).absent()) {
            this.applicationContext().log().warn("Component key '%s' is annotated with @Component, but is not registered.".formatted(contract.qualifiedName()));
        }
    }
}
