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

package org.dockbox.selene.core.impl.command.registration;

import org.dockbox.selene.core.annotations.command.Arg;
import org.dockbox.selene.core.annotations.command.Command;
import org.dockbox.selene.core.annotations.command.Flag;
import org.dockbox.selene.core.annotations.command.FromSource;
import org.dockbox.selene.core.command.context.CommandContext;
import org.dockbox.selene.core.command.source.CommandSource;
import org.dockbox.selene.core.exceptions.IllegalSourceException;
import org.dockbox.selene.core.i18n.entry.IntegratedResource;
import org.dockbox.selene.core.objects.Console;
import org.dockbox.selene.core.objects.Exceptional;
import org.dockbox.selene.core.objects.location.Location;
import org.dockbox.selene.core.objects.location.World;
import org.dockbox.selene.core.objects.player.Player;
import org.dockbox.selene.core.objects.targets.AbstractIdentifiable;
import org.dockbox.selene.core.objects.targets.Locatable;
import org.dockbox.selene.core.server.Selene;
import org.dockbox.selene.core.util.Reflect;
import org.dockbox.selene.core.util.SeleneUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

public class MethodCommandContext extends AbstractRegistrationContext {

    private final Method method;

    public MethodCommandContext(Command command, Method method) {
        super(command);
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getDeclaringClass() {
        return this.getMethod().getDeclaringClass();
    }

    @Override
    public Exceptional<IntegratedResource> call(CommandSource source, CommandContext context) {
        try {
            List<Object> args = this.prepareArguments(source, context);
            Object instance = this.prepareInstance();
            Command command = this.method.getAnnotation(Command.class);
            if (0 < command.cooldownDuration() && source instanceof AbstractIdentifiable) {
                String registrationId = this.getRegistrationId((AbstractIdentifiable<?>) source, context);
                SeleneUtils.cooldown(registrationId, command.cooldownDuration(), command.cooldownUnit());
            }
            this.method.invoke(instance, SeleneUtils.toArray(Object.class, args));
            return Exceptional.empty();
        } catch (IllegalSourceException e) {
            return Exceptional.of(e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Selene.handle("Failed to invoke command", e.getCause());
            return Exceptional.of(e);
        } catch (Throwable e) {
            Selene.handle("Failed to invoke command", e);
            return Exceptional.of(e);
        }
    }

    private List<Object> prepareArguments(CommandSource source, CommandContext context) {
        List<Object> finalArgs = SeleneUtils.emptyList();

        for (Parameter parameter : this.getMethod().getParameters()) {
            if (this.processFromSourceParameters(parameter, context, finalArgs)) continue;
            if (this.processFlagParameters(parameter, context, finalArgs)) continue;
            if (this.processArgumentParameters(parameter, context, finalArgs)) continue;

            Class<?> parameterType = parameter.getType();
            if (Reflect.isEitherAssignableFrom(CommandSource.class, parameterType)) {
                if (parameterType.equals(Player.class)) {
                    if (source instanceof Player) finalArgs.add(source);
                    else throw new IllegalSourceException("Command can only be ran by players");
                } else if (parameterType.equals(Console.class)) {
                    if (source instanceof Console) finalArgs.add(source);
                    else throw new IllegalSourceException("Command can only be ran by the console");
                } else finalArgs.add(source);
            } else if (Reflect.isEitherAssignableFrom(CommandContext.class, parameterType)) {
                finalArgs.add(context);
            } else {
                throw new IllegalStateException("Method requested parameter type '" + parameterType.getSimpleName() + "' which is not provided");
            }
        }
        return finalArgs;
    }

    private boolean processFromSourceParameters(Parameter parameter, CommandContext context, Collection<Object> finalArgs) {
        if (parameter.isAnnotationPresent(FromSource.class)) {
            Class<?> parameterType = parameter.getType();
            if (Reflect.isAssignableFrom(Player.class, parameterType)) {
                if (context.sender() instanceof Player) finalArgs.add(context.sender());
            } else if (Reflect.isAssignableFrom(World.class, parameterType)) {
                if (context.sender() instanceof Locatable) finalArgs.add(context.world());
            } else if (Reflect.isAssignableFrom(Location.class, parameterType)) {
                if (context.sender() instanceof Locatable) finalArgs.add(context.location());
            } else if (Reflect.isAssignableFrom(CommandSource.class, parameterType)) {
                finalArgs.add(context.sender());
            } else {
                Selene.log().warn(
                    "Parameter '" + parameter.getName() + "' has @FromSource annotation but cannot be provided [" + parameterType.getCanonicalName() +
                        "]");
                finalArgs.add(null);
            }
            return true;
        }
        return false;
    }

    private boolean processFlagParameters(AnnotatedElement parameter, CommandContext context, Collection<Object> finalArgs) {
        if (parameter.isAnnotationPresent(Flag.class)) {
            String flagName = parameter.getAnnotation(Flag.class).value();
            if (context.has(flagName) && context.flag(flagName).isPresent()) {
                finalArgs.add(context.flag(flagName).get().getValue());
            } else {
                // Flags are optional, therefore we do not log missing flags
                finalArgs.add(null);
            }
            return true;
        }
        return false;
    }

    private boolean processArgumentParameters(Parameter parameter, CommandContext context, Collection<Object> finalArgs) {
        if (parameter.isAnnotationPresent(Arg.class)) {
            Class<?> parameterType = parameter.getType();
            String argumentName = parameter.getAnnotation(Arg.class).value();
            if (context.has(argumentName) && context.argument(argumentName).isPresent()) {
                finalArgs.add(context.argument(argumentName).get().getValue());
            } else {
                if (!parameter.getAnnotation(Arg.class).optional()) {
                    Selene.log().warn("Parameter '" + parameter.getName() +
                        "' has @Argument annotation but cannot be provided, if it is optional ensure the annotation is marked as such [" +
                        parameterType.getCanonicalName() + "]");
                }
                finalArgs.add(null);
            }
            return true;
        }
        return false;
    }

    private Object prepareInstance() {
        Object instance;
        if (this.getDeclaringClass().equals(Selene.class) || Reflect.isAssignableFrom(Selene.class, this.getDeclaringClass())) {
            instance = Selene.getServer();
        } else {
            instance = Selene.provide(this.getDeclaringClass());
        }
        return instance;
    }

    private boolean isSenderInCooldown(CommandSource sender, CommandContext ctx) {
        Command command = this.getMethod().getAnnotation(Command.class);
        if (0 >= command.cooldownDuration()) return false;
        if (sender instanceof AbstractIdentifiable) {
            String registrationId = this.getRegistrationId((AbstractIdentifiable<?>) sender, ctx);
            return SeleneUtils.isInCooldown(registrationId);
        }
        return false;
    }

    public String getLocation() {
        return this.getDeclaringClass().getCanonicalName() + "." + this.getMethod().getName();
    }
}
