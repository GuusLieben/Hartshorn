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

package org.dockbox.hartshorn.properties.list;

import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.properties.value.ValuePropertyParser;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Collection;

/**
 * A parser to convert all single values in a {@link ListProperty} to instances of a specific type. This
 * requires all elements in the list to be compatible single-value {@link ValueProperty} instances.
 *
 * @param <T> the type to convert the value to
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ValueListPropertyParser<T> implements ListPropertyParser<T> {

    private final ValuePropertyParser<T> delegate;

    public ValueListPropertyParser(ValuePropertyParser<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<T> parse(ListProperty property) {
        return property.values().stream()
                .map(this.delegate::parse)
                .flatMap(Option::stream)
                .toList();
    }
}
