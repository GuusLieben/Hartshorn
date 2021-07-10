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

package org.dockbox.hartshorn.di.annotations.service;

import org.dockbox.hartshorn.api.domain.tuple.Tristate;
import org.dockbox.hartshorn.di.annotations.component.ComponentLike;
import org.dockbox.hartshorn.di.annotations.component.ComponentLink;
import org.dockbox.hartshorn.di.services.ComponentAspect;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ComponentLike(singleton = Tristate.UNDEFINED)
public @interface Service {

    @ComponentLink(ComponentAspect.ID)
    String id() default "";

    @ComponentLink(ComponentAspect.NAME)
    String name() default "";

    @ComponentLink(ComponentAspect.ENABLED)
    boolean enabled() default true;

    @ComponentLink(ComponentAspect.OWNER)
    Class<?> owner() default Void.class;

    @ComponentLink(ComponentAspect.SINGLETON)
    boolean singleton() default true;

    Class<? extends Annotation>[] activators() default Service.class;

    String[] dependencies() default {};
}
