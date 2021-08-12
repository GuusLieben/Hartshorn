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

package org.dockbox.hartshorn.test.util;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.config.GlobalConfig;
import org.dockbox.hartshorn.api.task.TaskRunner;
import org.dockbox.hartshorn.cache.CacheManager;
import org.dockbox.hartshorn.commands.SystemSubject;
import org.dockbox.hartshorn.config.ConfigurationManager;
import org.dockbox.hartshorn.di.InjectConfiguration;
import org.dockbox.hartshorn.di.Key;
import org.dockbox.hartshorn.di.TypeFactoryImpl;
import org.dockbox.hartshorn.di.TypeFactory;
import org.dockbox.hartshorn.discord.DiscordCommandSource;
import org.dockbox.hartshorn.discord.DiscordUtils;
import org.dockbox.hartshorn.persistence.FileManager;
import org.dockbox.hartshorn.server.minecraft.MinecraftVersion;
import org.dockbox.hartshorn.server.minecraft.bossbar.Bossbar;
import org.dockbox.hartshorn.server.minecraft.dimension.Worlds;
import org.dockbox.hartshorn.server.minecraft.entities.ArmorStand;
import org.dockbox.hartshorn.server.minecraft.entities.ItemFrame;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.maps.CustomMapService;
import org.dockbox.hartshorn.server.minecraft.players.Players;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.test.files.JUnitFileManager;
import org.dockbox.hartshorn.test.objects.JUnitBossbar;
import org.dockbox.hartshorn.test.objects.JUnitDiscordCommandSource;
import org.dockbox.hartshorn.test.objects.JUnitItem;
import org.dockbox.hartshorn.test.objects.JUnitProfile;
import org.dockbox.hartshorn.test.objects.JUnitSystemSubject;
import org.dockbox.hartshorn.test.objects.living.JUnitArmorStand;
import org.dockbox.hartshorn.test.objects.living.JUnitItemFrame;
import org.dockbox.hartshorn.test.services.JUnitCustomMapService;
import org.dockbox.hartshorn.test.services.JUnitPlayers;
import org.dockbox.hartshorn.test.services.JUnitWorlds;
import org.slf4j.Logger;

public class JUnitInjector extends InjectConfiguration {

    @Override
    public void collect() {
        // Factory creation
        this.bind(Key.of(TypeFactory.class), TypeFactoryImpl.class);

        // Tasks
        this.bind(Key.of(TaskRunner.class), JUnitTaskRunner.class);

        // Persistence
        this.bind(Key.of(FileManager.class), JUnitFileManager.class);

        // Services
        this.bind(Key.of(Players.class), JUnitPlayers.class);
        this.bind(Key.of(Worlds.class), JUnitWorlds.class);
        this.bind(Key.of(CustomMapService.class), JUnitCustomMapService.class);
        this.bind(Key.of(CacheManager.class), JUnitCacheManager.class);

        // Wired types - do NOT call directly!
        this.manual(Key.of(Item.class), JUnitItem.class);
        this.manual(Key.of(Bossbar.class), JUnitBossbar.class);
        this.manual(Key.of(Profile.class), JUnitProfile.class);
        this.manual(Key.of(ItemFrame.class), JUnitItemFrame.class);
        this.manual(Key.of(ArmorStand.class), JUnitArmorStand.class);
        this.manual(Key.of(DiscordCommandSource.class), JUnitDiscordCommandSource.class);
        this.manual(Key.of(ConfigurationManager.class), JUnitConfigurationManager.class);

        // Log is created from LoggerFactory externally
        this.bind(Key.of(Logger.class), Hartshorn.log());

        // Console is a constant singleton, to avoid recreation
        this.bind(Key.of(SystemSubject.class), new JUnitSystemSubject());

        this.bind(Key.of(GlobalConfig.class), JUnitGlobalConfig.class);
        this.bind(Key.of(MinecraftVersion.class), MinecraftVersion.INDEV);

        this.bind(Key.of(DiscordUtils.class), JUnitDiscordUtils.class);
    }
}
