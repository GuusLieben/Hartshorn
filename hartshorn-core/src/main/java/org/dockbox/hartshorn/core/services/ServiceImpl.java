/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.core.services;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.lang.annotation.Annotation;

public class ServiceImpl implements Service {
    @Override
    public String id() {
        return "";
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public Class<?> owner() {
        return Service.class;
    }

    @Override
    public boolean singleton() {
        return false;
    }

    @Override
    public boolean lazy() {
        return false;
    }

    @Override
    public Class<? extends Annotation>[] activators() {
        return new Class[0];
    }

    @Override
    public boolean permitProxying() {
        return true;
    }

    @Override
    public String[] requires() {
        return new String[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Service.class;
    }
}
