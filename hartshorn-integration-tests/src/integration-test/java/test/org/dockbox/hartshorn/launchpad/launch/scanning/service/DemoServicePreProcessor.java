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

package test.org.dockbox.hartshorn.launchpad.launch.scanning.service;

import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.processing.ComponentPreProcessor;
import org.dockbox.hartshorn.inject.processing.ComponentProcessingContext;
import org.dockbox.hartshorn.inject.processing.ProcessingPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoServicePreProcessor extends ComponentPreProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DemoServicePreProcessor.class);

    private int processed = 0;

    public int processed() {
        return this.processed;
    }

    @Override
    public <T> void process(InjectionCapableApplication application, ComponentProcessingContext<T> processingContext) {
        if (processingContext.type().is(DemoService.class)) {
            LOG.debug("Processing %s".formatted(processingContext));
            this.processed++;
        }
    }

    @Override
    public int priority() {
        return ProcessingPriority.NORMAL_PRECEDENCE;
    }
}
