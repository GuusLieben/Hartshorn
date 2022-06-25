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

package org.dockbox.hartshorn.inject.processing;

import org.dockbox.hartshorn.component.processing.Provider;
import org.dockbox.hartshorn.inject.Key;
import org.dockbox.hartshorn.util.reflect.AnnotatedElementContext;

public class ProviderContext {

    private final Key<?> key;
    private final AnnotatedElementContext<?> element;
    private final Provider provider;

    public ProviderContext(final Key<?> key, final AnnotatedElementContext<?> element, final Provider provider) {
        this.key = key;
        this.element = element;
        this.provider = provider;
    }

    public Key<?> key() {
        return this.key;
    }

    public AnnotatedElementContext<?> element() {
        return this.element;
    }

    public Provider provider() {
        return this.provider;
    }
}
