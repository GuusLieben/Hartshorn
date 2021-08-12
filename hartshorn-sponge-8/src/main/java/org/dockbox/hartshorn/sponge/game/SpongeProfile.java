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

package org.dockbox.hartshorn.sponge.game;

import org.dockbox.hartshorn.di.annotations.inject.Bound;
import org.dockbox.hartshorn.server.minecraft.players.Profile;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpongeProfile implements Profile {

    private GameProfile profile;

    @Bound
    public SpongeProfile(UUID uuid) {
        this.profile = GameProfile.of(uuid);
    }

    @Override
    public UUID uniqueId() {
        return this.profile.uuid();
    }

    @Override
    public SpongeProfile uniqueId(UUID uuid) {
        final List<ProfileProperty> properties = this.profile.properties();
        this.profile = GameProfile.of(uuid).withProperties(properties);
        return this;
    }

    @Override
    public Map<String, String> properties() {
        Map<String, String> properties = HartshornUtils.emptyMap();
        for (ProfileProperty property : this.profile.properties())
            properties.put(property.name(), property.value());
        return properties;
    }

    @Override
    public void property(String key, String value) {
        this.profile = this.profile.withProperty(ProfileProperty.of(key, value));
    }

    @Override
    public SpongeProfile properties(Map<String, String> properties) {
        final List<ProfileProperty> profileProperties = properties.entrySet().stream()
                .map(property -> ProfileProperty.of(property.getKey(), property.getValue()))
                .toList();
        this.profile = this.profile.withProperties(profileProperties);
        return this;
    }

    public GameProfile profile() {
        return this.profile;
    }
}
