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

package org.dockbox.hartshorn.inject;

import org.dockbox.hartshorn.application.UseBootstrap;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.inject.processing.UseServiceProvision;
import org.dockbox.hartshorn.component.processing.Provider;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.context.ConcreteContextCarrier;
import org.dockbox.hartshorn.context.ContextCarrier;

import jakarta.inject.Singleton;

@Service
@RequiresActivator({ UseBootstrap.class, UseServiceProvision.class })
public class DefaultProviders {

    @Provider
    @Singleton
    public Class<? extends ContextCarrier> contextCarrier() {
        return ConcreteContextCarrier.class;
    }
}
