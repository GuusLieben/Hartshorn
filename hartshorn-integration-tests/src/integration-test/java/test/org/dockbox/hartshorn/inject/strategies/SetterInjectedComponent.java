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

package test.org.dockbox.hartshorn.inject.strategies;

import org.dockbox.hartshorn.inject.annotations.Component;
import org.dockbox.hartshorn.inject.annotations.Named;
import org.dockbox.hartshorn.inject.annotations.Required;

import org.dockbox.hartshorn.inject.annotations.Inject;

import test.org.dockbox.hartshorn.inject.stereotype.ComponentType;
import test.org.dockbox.hartshorn.inject.context.SampleContext;

@Component
public class SetterInjectedComponent {

    private ComponentType component;
    private SampleContext context;

    @Inject
    public void setComponent(@Required ComponentType component) {
        this.component = component;
    }

    @Inject
    public void setContext(@Named("setter") SampleContext context) {
        this.context = context;
    }

    public ComponentType component() {
        return this.component;
    }

    public SampleContext context() {
        return this.context;
    }
}
