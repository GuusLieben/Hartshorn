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

package org.dockbox.hartshorn.test.services;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.server.minecraft.players.Player;
import org.dockbox.hartshorn.server.minecraft.players.Players;
import org.dockbox.hartshorn.test.objects.living.JUnitPlayer;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.List;
import java.util.UUID;

public class JUnitPlayers implements Players {

    private static final String PLAYER_ONE_NAME = "PlayerOne";
    public static final Player PLAYER_ONE = new JUnitPlayer(UUID.randomUUID(), PLAYER_ONE_NAME);
    private static final String PLAYER_TWO_NAME = "PlayerTwo";
    public static final Player PLAYER_TWO = new JUnitPlayer(UUID.randomUUID(), PLAYER_TWO_NAME);
    private static final String PLAYER_THREE_NAME = "PlayerThree";
    public static final Player PLAYER_THREE = new JUnitPlayer(UUID.randomUUID(), PLAYER_THREE_NAME);

    @Override
    public List<Player> onlinePlayers() {
        return HartshornUtils.asList(Player::online, PLAYER_ONE, PLAYER_TWO, PLAYER_THREE);
    }

    @Override
    public Exceptional<Player> player(String name) {
        if (PLAYER_ONE_NAME.equals(name)) return Exceptional.of(PLAYER_ONE);
        else if (PLAYER_TWO_NAME.equals(name)) return Exceptional.of(PLAYER_TWO);
        else if (PLAYER_THREE_NAME.equals(name)) return Exceptional.of(PLAYER_THREE);
        return Exceptional.empty();
    }

    @Override
    public Exceptional<Player> player(UUID uuid) {
        if (PLAYER_ONE.uniqueId().equals(uuid)) return Exceptional.of(PLAYER_ONE);
        else if (PLAYER_TWO.uniqueId().equals(uuid)) return Exceptional.of(PLAYER_TWO);
        else if (PLAYER_THREE.uniqueId().equals(uuid)) return Exceptional.of(PLAYER_THREE);
        return Exceptional.empty();
    }
}
