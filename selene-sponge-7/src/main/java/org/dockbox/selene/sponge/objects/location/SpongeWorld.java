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

package org.dockbox.selene.sponge.objects.location;

import com.flowpowered.math.vector.Vector3i;

import org.dockbox.selene.api.domain.Exceptional;
import org.dockbox.selene.api.domain.tuple.Vector3N;
import org.dockbox.selene.server.minecraft.dimension.Chunk;
import org.dockbox.selene.server.minecraft.dimension.position.Location;
import org.dockbox.selene.server.minecraft.dimension.world.World;
import org.dockbox.selene.server.minecraft.players.Gamemode;
import org.dockbox.selene.sponge.util.SpongeConversionUtil;
import org.dockbox.selene.util.SeleneUtils;
import org.dockbox.selene.util.Wrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.extent.Extent;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongeWorld extends World implements Wrapper<org.spongepowered.api.world.World>, SpongeDimension {

    private WeakReference<org.spongepowered.api.world.World> reference = new WeakReference<>(null);

    public SpongeWorld(
            @NotNull UUID worldUniqueId,
            @NotNull String name,
            boolean loadOnStartup,
            @NotNull Vector3N spawnPosition,
            long seed,
            @NotNull Gamemode defaultGamemode
    ) {
        super(worldUniqueId, name, loadOnStartup, spawnPosition, seed, defaultGamemode);
        this.setReference(this.constructInitialReference());
    }

    @Override
    public int getPlayerCount() {
        if (this.referenceExists()) return this.getReference().get().getPlayers().size();
        else return 0;
    }

    @Override
    public boolean unload() {
        if (this.referenceExists()) {
            return Sponge.getServer().unloadWorld(this.getReference().get());
        }
        else return true; // Already unloaded
    }

    @Override
    public boolean load() {
        if (!this.isLoaded()) {
            return Sponge.getServer().loadWorld(this.getWorldUniqueId()).isPresent();
        }
        else return this.isLoaded();
    }

    @Override
    public boolean isLoaded() {
        if (this.referenceExists()) {
            return this.getReference().get().isLoaded();
        }
        else return false; // No reference means the world is not loaded (as it is obtained through #getWorld rather than #loadWorld
    }

    @Override
    public Exceptional<org.spongepowered.api.world.World> getReference() {
        // Do NOT load the world here as this reference is also used for several methods where the world
        // does not have to be loaded, or even _should_ not be loaded due to the performance impact of
        // loading a world.
        if (null == this.reference.get())
            this.setReference(Exceptional.of(Sponge.getServer().getWorld(this.getWorldUniqueId())));
        return Exceptional.of(this.reference.get());
    }

    @Override
    public void setReference(@NotNull Exceptional<org.spongepowered.api.world.World> reference) {
        reference.present(world -> this.reference = new WeakReference<>(world));
    }

    @Override
    public Exceptional<org.spongepowered.api.world.World> constructInitialReference() {
        return Exceptional.of(Sponge.getServer().getWorld(this.getWorldUniqueId()));
    }

    public org.spongepowered.api.world.World getReferenceWorld() {
        return this.getReference().orNull();
    }

    @Override
    public void setGamerule(String key, String value) {
        if (this.referenceExists()) {
            this.getReference().get().getProperties().setGameRule(key, value);
        }
    }

    @Override
    public boolean isLoadOnStartup() {
        if (this.referenceExists()) {
            return this.getReference().get().getProperties().loadOnStartup();
        }
        else return false;
    }

    @Override
    public void setLoadOnStartup(boolean loadOnStartup) {
        if (this.referenceExists()) {
            this.getReference().get().getProperties().setLoadOnStartup(loadOnStartup);
        }
    }

    @NotNull
    @Override
    public Vector3N getSpawnPosition() {
        if (this.referenceExists()) {
            Vector3i vector3i = this.getReference().get().getProperties().getSpawnPosition();
            return Vector3N.of(vector3i.getX(), vector3i.getY(), vector3i.getZ());
        }
        else return Vector3N.empty();
    }

    @Override
    public void setSpawnPosition(@NotNull Vector3N spawnPosition) {
        if (this.referenceExists()) {
            this.getReference()
                    .get()
                    .getProperties()
                    .setSpawnPosition(
                            new Vector3i(spawnPosition.getXi(), spawnPosition.getYi(), spawnPosition.getZi()));
        }
    }

    @Override
    public long getSeed() {
        if (this.referenceExists()) {
            return this.getReference().get().getProperties().getSeed();
        }
        else return 0;
    }

    @Override
    public void setSeed(long seed) {
        if (this.referenceExists()) {
            this.getReference().get().getProperties().setSeed(seed);
        }
    }

    @NotNull
    @Override
    public Gamemode getDefaultGamemode() {
        if (this.referenceExists()) {
            return SpongeConversionUtil.fromSponge(
                    this.getReference().get().getProperties().getGameMode());
        }
        else return Gamemode.OTHER;
    }

    @Override
    public void setDefaultGamemode(@NotNull Gamemode defaultGamemode) {
        if (this.referenceExists()) {
            this.getReference()
                    .get()
                    .getProperties()
                    .setGameMode(SpongeConversionUtil.toSponge(defaultGamemode));
        }
    }

    @Override
    public Map<String, String> getGamerules() {
        if (this.referenceExists()) {
            return this.getReference().get().getProperties().getGameRules();
        }
        return SeleneUtils.emptyMap();
    }

    @Override
    public Exceptional<Chunk> getChunk(Location location) {
        if (this.referenceExists()) {
            Vector3i position = SpongeConversionUtil.toSponge(location).get().getBlockPosition();
            Exceptional<org.spongepowered.api.world.Chunk> chunkAtBlock = Exceptional.of(this.getReferenceWorld().getChunkAtBlock(position));
            return chunkAtBlock.map(SpongeConversionUtil::fromSponge);
        }
        return Exceptional.none();
    }

    @Override
    public Exceptional<Chunk> getChunk(int x, int y) {
        if (this.referenceExists()) {
            Exceptional<org.spongepowered.api.world.Chunk> exceptional = Exceptional.of(this.getReferenceWorld().getChunk(x, 0, y));
            return exceptional.map(SpongeConversionUtil::fromSponge);
        }
        return Exceptional.none();
    }

    @Override
    public Collection<Chunk> getLoadedChunks() {
        if (this.referenceExists()) {
            return SeleneUtils.stream(this.getReferenceWorld().getLoadedChunks()).map(SpongeConversionUtil::fromSponge).collect(Collectors.toList());
        }
        return SeleneUtils.emptyList();
    }

    @Override
    public Extent getExtent() {
        return this.getReferenceWorld();
    }
}
