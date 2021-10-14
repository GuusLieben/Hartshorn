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

package org.dockbox.hartshorn.web;

import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.di.context.element.MethodContext;
import org.dockbox.hartshorn.di.context.element.TypeContext;
import org.dockbox.hartshorn.di.services.ServiceProcessor;
import org.dockbox.hartshorn.web.annotations.http.HttpRequest;
import org.dockbox.hartshorn.web.annotations.UseWebStarter;

public class RestControllerProcessor implements ServiceProcessor<UseWebStarter> {
    @Override
    public Class<UseWebStarter> activator() {
        return UseWebStarter.class;
    }

    @Override
    public boolean preconditions(final ApplicationContext context, final TypeContext<?> type) {
        return !type.flatMethods(HttpRequest.class).isEmpty();
    }

    @Override
    public <T> void process(final ApplicationContext context, final TypeContext<T> type) {
        final ControllerContext controllerContext = context.first(ControllerContext.class).get();
        for (final MethodContext<?, T> method : type.flatMethods(HttpRequest.class)) {
            controllerContext.add(new RequestHandlerContext(context, method));
        }
    }
}
