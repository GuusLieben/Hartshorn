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

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.commands.CommandSource;

import java.net.InetAddress;
import java.time.LocalDateTime;

public class IpUnbannedEvent extends BanEvent<InetAddress> {

    /**
     * The event fired when a IP is unbanned, represented by a {@link InetAddress}.
     *
     * @param host
     *         The IP being unbanned
     * @param reason
     *         The reason of the original ban
     * @param source
     *         The {@link CommandSource} executing the pardon
     * @param creation
     *         The {@link LocalDateTime} of when the pardon was issued.
     */
    public IpUnbannedEvent(
            final InetAddress host,
            final CommandSource source,
            final Exceptional<String> reason,
            final LocalDateTime creation
    ) {
        super(host, source, creation, reason, Exceptional.empty());
    }
}
