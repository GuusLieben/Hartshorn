/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.commands.arguments;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.commands.CommandSource;
import org.dockbox.hartshorn.commands.annotations.Parameter;
import org.dockbox.hartshorn.commands.service.CommandParameter;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Collection;

/**
 * The default converter for any type which can be constructed with a {@link CustomParameterPattern}. Typically
 * this only applies to types decorated with {@link Parameter},
 * however this is not a requirement.
 *
 * @param <T>
 *         The generic type
 */
public class DynamicPatternConverter<T> extends DefaultArgumentConverter<T> {

    private final CustomParameterPattern pattern;

    public DynamicPatternConverter(Class<T> type, CustomParameterPattern pattern, String... keys) {
        super(type, keys);
        this.pattern = pattern;
    }

    @Override
    public Exceptional<T> convert(CommandSource source, String argument) {
        return this.pattern.request(this.type(), source, argument);
    }

    @Override
    public Exceptional<T> convert(CommandSource source, CommandParameter<String> value) {
        return this.pattern.request(this.type(), source, value.value());
    }

    @Override
    public Collection<String> suggestions(CommandSource source, String argument) {
        return HartshornUtils.emptyList();
    }
}
