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

package org.dockbox.selene.core.events.chat

import org.dockbox.selene.core.events.AbstractTargetCancellableEvent
import org.dockbox.selene.core.objects.targets.MessageReceiver
import org.dockbox.selene.core.text.Text

/**
 * The event fired when Selene is about to send a message to any [MessageReceiver].
 *
 * @property message The message
 *
 * @param target The target message receiver
 */
class SendMessageEvent(
        target: MessageReceiver,
        val message: Text
) : AbstractTargetCancellableEvent(target)

