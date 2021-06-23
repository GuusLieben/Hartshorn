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

package org.dockbox.hartshorn.server.minecraft.events.moderation;

import org.dockbox.hartshorn.commands.source.CommandSource;
import org.dockbox.hartshorn.api.domain.Exceptional;

import java.time.LocalDateTime;

public class NameUnbannedEvent extends BanEvent<String> {

    /**
     * The event fired when a name is unbanned.
     *
     * @param name
     *         The name being unbanned
     * @param reason
     *         The reason of the original ban
     * @param source
     *         The {@link CommandSource} executing the pardon
     * @param creation
     *         The {@link LocalDateTime} of when the pardon was issued.
     */
    public NameUnbannedEvent(String name, CommandSource source, Exceptional<String> reason, LocalDateTime creation) {
        super(name, source, reason, Exceptional.empty(), creation);
    }
}
