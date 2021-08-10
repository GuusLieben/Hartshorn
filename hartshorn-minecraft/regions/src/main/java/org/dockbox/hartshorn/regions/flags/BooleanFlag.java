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

package org.dockbox.hartshorn.regions.flags;

import org.dockbox.hartshorn.di.annotations.inject.Bound;
import org.dockbox.hartshorn.i18n.common.ResourceEntry;

public class BooleanFlag extends AbstractRegionFlag<Boolean> {

    @Bound
    public BooleanFlag(String id, ResourceEntry description) {
        super(id, description);
    }

    @Override
    public String serialize(Boolean object) {
        return String.valueOf(object);
    }

    @Override
    public Boolean restore(String raw) {
        return Boolean.parseBoolean(raw);
    }

    @Override
    public Class<Boolean> type() {
        return Boolean.class;
    }
}
