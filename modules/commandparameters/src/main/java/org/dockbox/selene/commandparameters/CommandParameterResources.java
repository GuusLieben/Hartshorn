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

package org.dockbox.selene.commandparameters;

import org.dockbox.selene.api.annotations.i18n.Resources;
import org.dockbox.selene.api.i18n.common.ResourceEntry;
import org.dockbox.selene.api.i18n.entry.Resource;

@Resources(module = CommandParameters.class)
public class CommandParameterResources {

    public static final ResourceEntry USAGE = new Resource("Usage: {0}", "selene.command.usage");
    public static final ResourceEntry NOT_ENOUGH_ARGUMENTS = new Resource("Not enough arguments. {0}", "selene.command.missing_args");
    public static final ResourceEntry TOO_MANY_ARGUMENTS = new Resource("Too many arguments. {0}", "selene.command.too_many_args");
    public static final ResourceEntry HASHTAG_PATTERN_WRONG_FORMAT = new Resource("Pattern has to be formatted as #type[arg1][arg2][etc.]", "selene.command.hashtag.wrong_format");
    public static final ResourceEntry MISSING_CONVERTER = new Resource("Parameter of type {0} has no register converter", "selene.command.missing_converter");

}
