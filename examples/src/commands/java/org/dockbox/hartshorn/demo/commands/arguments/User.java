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

package org.dockbox.hartshorn.demo.commands.arguments;

import org.dockbox.hartshorn.commands.annotations.Parameter;
import org.dockbox.hartshorn.commands.context.CommandContext;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A simple bean type which can be constructed through a {@link org.dockbox.hartshorn.commands.arguments.CustomParameterPattern}.
 *
 * @see org.dockbox.hartshorn.commands.arguments.HashtagParameterPattern
 * @see org.dockbox.hartshorn.demo.commands.services.CommandService#build(CommandContext)
 */
@Parameter("user")
@AllArgsConstructor
@Getter
public class User {
    private String name;
    private int age;
}
