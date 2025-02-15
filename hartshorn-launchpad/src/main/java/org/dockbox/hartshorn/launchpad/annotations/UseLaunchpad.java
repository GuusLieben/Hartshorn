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

package org.dockbox.hartshorn.launchpad.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.dockbox.hartshorn.launchpad.activation.ServiceActivator;
import org.dockbox.hartshorn.launchpad.launch.ApplicationContextFactory;
import org.dockbox.hartshorn.launchpad.launch.StandardApplicationContextFactory;
import org.dockbox.hartshorn.launchpad.configuration.DefaultConfigurationBinderPostProcessor;

/**
 * Default service activator for Launchpad-based applications, enabling lifecycle observers and component proxying. When using
 * the built-in {@link ApplicationContextFactory} ({@link StandardApplicationContextFactory}), this activator is automatically
 * registered and used to activate services.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ServiceActivator(
        binderPostProcessors = DefaultConfigurationBinderPostProcessor.class
)
@UseLifecycleObservers
@UseProxying
public @interface UseLaunchpad {

}
