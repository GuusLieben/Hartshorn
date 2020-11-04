/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.core.impl.command.convert;

import org.dockbox.selene.core.impl.command.convert.impl.DefaultArgumentConverters;
import org.dockbox.selene.core.objects.optional.Exceptional;
import org.dockbox.selene.core.objects.targets.CommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class ArgumentConverter<T> {

    private final String[] keys;
    private final Class<T> type;

    protected ArgumentConverter(Class<T> type, String... keys) {
        if (0 == keys.length)
            throw new IllegalArgumentException("Cannot create argument converter without at least one key");
        this.keys = keys;
        this.type = type;
        DefaultArgumentConverters.registerConverter(this);
    }

    public List<String> getKeys() {
        return Arrays.asList(this.keys);
    }

    public Class<T> getType() {
        return this.type;
    }


    public abstract Exceptional<T> convert(CommandSource source, String argument);

    public abstract Collection<String> getSuggestions(CommandSource source, String argument);
}
