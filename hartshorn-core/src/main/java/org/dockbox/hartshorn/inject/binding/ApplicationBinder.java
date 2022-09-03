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

package org.dockbox.hartshorn.inject.binding;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.application.environment.ApplicationEnvironment;
import org.dockbox.hartshorn.component.processing.ComponentPostProcessor;
import org.dockbox.hartshorn.component.processing.ComponentPreProcessor;
import org.dockbox.hartshorn.inject.Key;
import org.dockbox.hartshorn.inject.ProviderContext;

/**
 * A specialized {@link Binder} that is used to bind prefixes and {@link ProviderContext}s. These configurations
 * are typically used to configure an active {@link ApplicationContext}.
 */
public interface ApplicationBinder extends Binder {

    /**
     * Binds the given prefix, which represents a package. The given prefix is scanned for  {@link ComponentPreProcessor}s
     * and {@link ComponentPostProcessor}s, as well as automatically bound types. The prefix is also registered to the
     * active {@link ApplicationEnvironment}. After all components are registered, the located {@link ComponentPreProcessor}s
     * are activated.
     *
     * @param prefix The prefix to bind.
     */
    void bind(String prefix);

    /**
     * Binds the given {@link ProviderContext} to the {@link Key} provided through {@link ProviderContext#key()}. The
     * context is directly registered to the {@link BindingHierarchy} of the {@link Key}, using its
     * {@link ProviderContext#priority()}. If the context is marked as a singleton provider through
     * {@link ProviderContext#singleton()}, the {@link ProviderContext#provider()} is immediately called to obtain a
     * singleton instance of type <code>T</code>.
     *
     * @param <T> The type of the provider context.
     * @param context The context to bind.
     */
    <T> void add(ProviderContext<T> context);
}
