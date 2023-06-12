/*
 * Copyright 2019-2023 the original author or authors.
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

package org.dockbox.hartshorn.application.context;

import org.dockbox.hartshorn.application.InitializingContext;
import org.dockbox.hartshorn.component.ComponentContainer;
import org.dockbox.hartshorn.component.ComponentKey;
import org.dockbox.hartshorn.component.PostProcessingComponentProvider;
import org.dockbox.hartshorn.component.processing.ComponentPostProcessor;
import org.dockbox.hartshorn.component.processing.ComponentPreProcessor;
import org.dockbox.hartshorn.component.processing.ComponentProcessingContext;
import org.dockbox.hartshorn.component.processing.ComponentProcessor;
import org.dockbox.hartshorn.component.processing.ExitingComponentProcessor;
import org.dockbox.hartshorn.inject.CompositeDependencyResolver;
import org.dockbox.hartshorn.inject.DependencyContext;
import org.dockbox.hartshorn.inject.DependencyResolver;
import org.dockbox.hartshorn.inject.processing.DependencyGraphBuilder;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.collections.StandardMultiMap.ConcurrentSetTreeMultiMap;
import org.dockbox.hartshorn.util.graph.Graph;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

import java.util.Collection;
import java.util.Set;

public class ClasspathApplicationContext extends DelegatingApplicationContext implements ProcessableApplicationContext {

    private final DependencyGraphBuilder graphBuilder = new DependencyGraphBuilder();
    protected transient MultiMap<Integer, ComponentPreProcessor> preProcessors;

    public ClasspathApplicationContext(final InitializingContext context) {
        super(context);
    }

    @Override
    protected void prepareInitialization() {
        this.preProcessors = new ConcurrentSetTreeMultiMap<>();
    }

    @Override
    public void add(final ComponentProcessor processor) {
        this.checkRunning();

        final Integer order = processor.priority();
        final String name = processor.getClass().getSimpleName();

        if (processor instanceof ComponentPostProcessor postProcessor && this.componentProvider() instanceof PostProcessingComponentProvider provider) {
            // Singleton binding is decided by the component provider, to allow for further optimization
            provider.postProcessor(postProcessor);
            this.log().debug("Added %s for component post-processing with order %d".formatted(name, order));
        }
        else if (processor instanceof ComponentPreProcessor preProcessor) {
            this.preProcessors.put(preProcessor.priority(), preProcessor);
            this.bind((Class<ComponentPreProcessor>) preProcessor.getClass()).singleton(preProcessor);
            this.log().debug("Added %s for component pre-processing with order %d".formatted(name, order));
        }
        else {
            this.log().warn("Unsupported component processor type [" + name + "]");
        }
    }

    @Override
    public void loadContext() {
        this.checkRunning();

        final Collection<ComponentContainer> containers = this.locator().containers();
        this.log().debug("Located %d components".formatted(containers.size()));

        this.initializeDependencyGraph(containers);
        this.processComponents(containers);

        this.isRunning = true;
    }

    private void initializeDependencyGraph(final Collection<ComponentContainer> containers) {
        // TODO: Registration hooks
        final DependencyResolver dependencyResolver = new CompositeDependencyResolver(Set.of());
        final Collection<DependencyContext> dependencyContexts = dependencyResolver.resolve(containers, this);
        final Graph<DependencyContext> dependencyGraph = this.graphBuilder.buildDependencyGraph(dependencyContexts);
        // TODO: Process graph
    }

    @Override
    public MultiMap<Integer, ComponentPreProcessor> processors() {
        return this.preProcessors;
    }

    protected void processComponents(final Collection<ComponentContainer> containers) {
        this.checkRunning();
        for (final ComponentPreProcessor serviceProcessor : this.preProcessors.allValues()) {
            this.log().debug("Processing %s components with registered processor %s".formatted(containers.size(), serviceProcessor.getClass().getSimpleName()));
            for (final ComponentContainer container : containers) {
                this.processStandaloneComponent(container, serviceProcessor);
            }
            if (serviceProcessor instanceof ExitingComponentProcessor exiting) {
                exiting.exit(this);
            }
        }
    }

    private void processStandaloneComponent(final ComponentContainer container, final ComponentPreProcessor serviceProcessor) {
        final TypeView<?> service = container.type();
        final ComponentKey<?> key = ComponentKey.of(service.type());
        final ComponentProcessingContext<?> context = new ComponentProcessingContext<>(this, key, null);
        this.log().debug("Processing component %s with registered processor %s".formatted(container.id(), serviceProcessor.getClass().getSimpleName()));
        serviceProcessor.process(context);
    }
}
