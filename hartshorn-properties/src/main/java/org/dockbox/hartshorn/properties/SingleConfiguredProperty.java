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

package org.dockbox.hartshorn.properties;

import org.dockbox.hartshorn.properties.parse.ConfiguredPropertyParser;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A simple {@link ConfiguredProperty} implementation that holds a single name-value pair.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class SingleConfiguredProperty implements ConfiguredProperty {

    private final String name;
    private final String value;

    public SingleConfiguredProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Option<String> value() {
        return Option.of(this.value);
    }

    @Override
    public <T> Option<T> value(ConfiguredPropertyParser<T> parser) {
        return parser.parse(this);
    }
}
