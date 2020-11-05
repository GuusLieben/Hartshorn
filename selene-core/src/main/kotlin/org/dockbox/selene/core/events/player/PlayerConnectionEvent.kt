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

package org.dockbox.selene.core.events.player

import java.net.InetSocketAddress
import org.dockbox.selene.core.objects.events.Targetable
import org.dockbox.selene.core.objects.targets.Target

abstract class PlayerConnectionEvent(private val target: Target?) : Targetable {

    override fun getTarget(): Target? {
        return this.target
    }

    override fun setTarget(target: Target) {
        throw UnsupportedOperationException("Cannot change target of connection event")
    }

    class Join(target: Target) : PlayerConnectionEvent(target)
    class Leave(target: Target) : PlayerConnectionEvent(target)
    class Auth(address: InetSocketAddress, host: InetSocketAddress) : PlayerConnectionEvent(null) {

        override fun getTarget(): Target {
            throw UnsupportedOperationException("Cannot get target while authenticating")
        }

    }

}
