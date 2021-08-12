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

package org.dockbox.hartshorn.commands.definition;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.i18n.permissions.Permission;

/**
 * Represents a part of a command, which is typically either a argument or flag.
 */
public interface CommandPartial {

    /**
     * Gets the name of the part. This is typically used to obtain the part from
     * a {@link org.dockbox.hartshorn.commands.context.CommandContext} instance.
     *
     * @return The name of the part.
     */
    String name();

    /**
     * Gets the permission required to use the part, if any is required.
     *
     * @return The required permission, or {@link Exceptional#empty()}
     */
    Exceptional<Permission> permission();

}
