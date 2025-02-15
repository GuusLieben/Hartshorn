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

package org.dockbox.hartshorn.launchpad.processing;

import org.dockbox.hartshorn.launchpad.context.ApplicationContextCarrier;
import org.dockbox.hartshorn.inject.processing.ProcessingPriority;
import org.dockbox.hartshorn.inject.processing.proxy.ProxyDelegationPostProcessor;

/**
 * TODO: #1060 Add documentation
 *
 * @since 0.4.9
 *
 * @author Guus Lieben
 */
public class ContextCarrierDelegationPostProcessor extends ProxyDelegationPostProcessor<ApplicationContextCarrier> {

    @Override
    protected Class<ApplicationContextCarrier> parentTarget() {
        return ApplicationContextCarrier.class;
    }

    @Override
    protected boolean skipConcreteMethods() {
        return true;
    }

    @Override
    public int priority() {
        return ProcessingPriority.NORMAL_PRECEDENCE;
    }
}
