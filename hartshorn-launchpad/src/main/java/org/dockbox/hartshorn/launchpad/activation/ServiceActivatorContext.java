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

package org.dockbox.hartshorn.launchpad.activation;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dockbox.hartshorn.launchpad.launch.ApplicationContextFactory;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.inject.DefaultFallbackCompatibleContext;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;

/**
 * Carrier context for {@link ServiceActivator} annotations. This context is used to store all
 * {@link ServiceActivator} annotations that are found in the application. This context is used
 * to determine which activators are available, and to retrieve the actual annotation instance.
 *
 * <p>Depending on the {@link ApplicationContextFactory} that is used to create the
 * {@link ApplicationContext}, this context may be used to supply the {@link ServiceActivator}
 * annotations.
 *
 * <p>This context should always be attached to the {@link ApplicationContext}, and yield the
 * same result as {@link ApplicationContext#activators()}.
 *
 * @see ApplicationContextFactory
 * @see ApplicationContext#activators()
 * @see ServiceActivator
 * @see ActivatorHolder
 *
 * @since 0.4.13
 *
 * @author Guus Lieben
 */
public class ServiceActivatorContext extends DefaultFallbackCompatibleContext implements Reportable {

    private final Map<Class<? extends Annotation>, Annotation> activators = new ConcurrentHashMap<>();

    public ServiceActivatorContext(Set<Annotation> serviceActivators) {
        for (Annotation serviceActivator : serviceActivators) {
            if (!serviceActivator.annotationType().isAnnotationPresent(ServiceActivator.class)) {
                throw new IllegalArgumentException("Annotation " + serviceActivator + " is not a valid service activator");
            }
            this.activators.put(serviceActivator.annotationType(), serviceActivator);
        }
    }

    /**
     * Returns all {@link ServiceActivator} annotations that are available in this context. This
     * includes all activators that are provided to this context, and will not filter out any hierarchical
     * activators.
     *
     * @return All {@link ServiceActivator} annotations that are available in this context.
     */
    public Set<Annotation> activators() {
        return Set.copyOf(this.activators.values());
    }

    /**
     * Returns whether this context contains an activator for the given {@link ServiceActivator}. This
     * method will only return {@code true} if the given activator is directly available in this context,
     * but not if the given activator is hierarchical through (virtual) inheritance.
     *
     * @param activator The activator to check for
     * @return {@code true} if the given activator is directly available in this context, {@code false} otherwise.
     *
     * @see org.dockbox.hartshorn.util.introspect.annotations.Extends
     */
    public boolean hasActivator(Class<? extends Annotation> activator) {
        if (!activator.isAnnotationPresent(ServiceActivator.class)) {
            throw new InvalidActivatorException("Requested activator " + activator.getSimpleName() + " is not decorated with @ServiceActivator");
        }
        return this.activators.containsKey(activator);
    }

    /**
     * Returns the {@link ServiceActivator} annotation instance for the given activator type. This method
     * will only return a value if the given activator is directly available in this context, but not if
     * the given activator is hierarchical through (virtual) inheritance.
     *
     * @param activator The activator to retrieve
     * @param <A> The type of the activator
     *
     * @return The {@link ServiceActivator} annotation instance for the given activator type, or {@code null} if the
     *         given activator is not directly available in this context.
     */
    public <A> A activator(Class<A> activator) {
        Annotation annotation = this.activators.get(activator);
        if (annotation != null) {
            return activator.cast(annotation);
        }
        return null;
    }

    @Override
    public void report(DiagnosticsPropertyCollector collector) {
        String[] activators = this.activators().stream()
                .map(activator -> activator.annotationType().getCanonicalName())
                .toArray(String[]::new);
        collector.property("activators").writeStrings(activators);
    }
}
