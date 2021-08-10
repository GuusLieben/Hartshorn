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

package org.dockbox.hartshorn.i18n.entry;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.i18n.annotations.Resource;
import org.dockbox.hartshorn.i18n.common.ResourceEntry;

@Service(owner = Hartshorn.class)
public interface DefaultResources {

    static DefaultResources instance() {
        return Hartshorn.context().get(DefaultResources.class);
    }

    @org.dockbox.hartshorn.i18n.annotations.Resource(value = "$3[] $1", key = "prefix")
    ResourceEntry prefix();

    @org.dockbox.hartshorn.i18n.annotations.Resource(value = "$4An unknown occurred. $3{0}", key = "caught")
    ResourceEntry unknownError(String message);

    @org.dockbox.hartshorn.i18n.annotations.Resource(value = "$4Cannot apply to this type", key = "caught.failedbinding")
    ResourceEntry bindingFailure();

    @org.dockbox.hartshorn.i18n.annotations.Resource(value = "$4Reference to object lost", key = "caught.reference.lost")
    ResourceEntry referenceLost();

    @org.dockbox.hartshorn.i18n.annotations.Resource(value = "Unknown", key = "source.unknown")
    ResourceEntry unknown();

    @Resource(value = "$4{0}", key = "hartshorn.exception")
    ResourceEntry exception(String message);

}
