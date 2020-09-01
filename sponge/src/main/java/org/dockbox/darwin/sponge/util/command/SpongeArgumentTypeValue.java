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

package org.dockbox.darwin.sponge.util.command;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import org.dockbox.darwin.core.command.AbstractArgumentValue;
import org.dockbox.darwin.core.command.SimpleCommandBus.Arguments;
import org.dockbox.darwin.core.server.Server;
import org.dockbox.darwin.sponge.util.command.SpongeCommandBus.FaweArgument;
import org.dockbox.darwin.sponge.util.command.SpongeCommandBus.FaweArgument.FaweTypes;
import org.dockbox.darwin.sponge.util.command.SpongeCommandBus.ModuleArgument;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

@SuppressWarnings({"unchecked", "rawtypes", "Guava"})
public class SpongeArgumentTypeValue extends AbstractArgumentValue<CommandElement> {

    public SpongeArgumentTypeValue(String type, String permission, String key) throws IllegalArgumentException {
        super(Arguments.valueOf(type.toUpperCase()), permission, key);
        Optional<Arguments> argCandidate = Enums.getIfPresent(Arguments.class, type.toUpperCase());
        if (!argCandidate.isPresent()) {
            try {
                Class<?> clazz = Class.forName(type);
                if (clazz.isEnum()) {
                    Class<? extends Enum> enumType = (Class<? extends Enum>) clazz;
                    this.setElement(GenericArguments.enumValue(Text.of(key), enumType));
                } else throw new IllegalArgumentException("Type '" + type.toLowerCase() + "' is not supported");
            } catch (Exception e) {
                Server.getServer().except("No argument of type `" + type + "` can be read", e);
            }
        }
    }

    @Override
    protected CommandElement parseArgument(Arguments argument, String key) {
        if (argument == null) return null;
        switch (argument) {
            case BOOL:
                return GenericArguments.bool(Text.of(key));
            case DOUBLE:
                return GenericArguments.doubleNum(Text.of(key));
            case ENTITY:
                return GenericArguments.entity(Text.of(key));
            case INTEGER:
                return GenericArguments.integer(Text.of(key));
            case LOCATION:
                return GenericArguments.location(Text.of(key));
            case LONG:
                return GenericArguments.longNum(Text.of(key));
            case PLAYER:
                return GenericArguments.player(Text.of(key));
            case MODULE:
                return new ModuleArgument(Text.of(key));
            case REMAININGSTRING:
                return GenericArguments.remainingJoinedStrings(Text.of(key));
            case STRING:
                return GenericArguments.string(Text.of(key));
            case USER:
                return GenericArguments.user(Text.of(key));
            case UUID:
                return GenericArguments.uuid(Text.of(key));
            case VECTOR:
                return GenericArguments.vector3d(Text.of(key));
            case WORLD:
                return GenericArguments.world(Text.of(key));
            case EDITSESSION:
                return new FaweArgument(Text.of(key), FaweTypes.EDIT_SESSION);
            case MASK:
                return new FaweArgument(Text.of(key), FaweTypes.MASK);
            case PATTERN:
                return new FaweArgument(Text.of(key), FaweTypes.PATTERN);
            case REGION:
                return new FaweArgument(Text.of(key), FaweTypes.REGION);
            case OTHER:
            default:
                return null;
        }
    }

    public CommandElement getArgument() {
        return getPermission() == null ? super.getElement() : GenericArguments.requiringPermission(getElement(), getPermission());
    }
}
