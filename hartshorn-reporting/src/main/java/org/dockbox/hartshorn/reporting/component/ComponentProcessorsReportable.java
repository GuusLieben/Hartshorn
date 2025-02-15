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

package org.dockbox.hartshorn.reporting.component;

import java.util.Collection;

import org.dockbox.hartshorn.inject.processing.ComponentProcessor;
import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;
import org.dockbox.hartshorn.util.collections.MultiMap;

/**
 * A {@link Reportable} that reports all provided {@link ComponentProcessor}s.
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class ComponentProcessorsReportable implements Reportable {

    private final MultiMap<Integer, ? extends ComponentProcessor> processors;

    public ComponentProcessorsReportable(MultiMap<Integer, ? extends ComponentProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public void report(DiagnosticsPropertyCollector propertyCollector) {
        Reportable[] reportables = processors.values().stream()
                .flatMap(Collection::stream)
                .map(processor -> (Reportable) processorCollector -> {
                    processorCollector.property("name").writeString(processor.getClass().getCanonicalName());
                    processorCollector.property("priority").writeInt(processor.priority());
                }).toArray(Reportable[]::new);

        propertyCollector.property("processors").writeDelegates(reportables);
    }
}
