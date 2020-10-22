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

package org.dockbox.selene.core.util.extension

import java.util.*

interface ExtensionManager {

    fun getContext(type: Class<*>): Optional<ExtensionContext>
    fun getContext(id: String): Optional<ExtensionContext>

    fun getHeader(type: Class<*>): Optional<Extension>
    fun getHeader(id: String): Optional<Extension>

    fun <T> getInstance(type: Class<T>): Optional<T>
    fun getInstance(id: String): Optional<*>

    fun initialiseExtensions(): List<ExtensionContext>

    fun getRegisteredExtensionIds(): List<String>
}
