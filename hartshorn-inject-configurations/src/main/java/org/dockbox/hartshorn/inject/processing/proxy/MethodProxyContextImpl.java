/*
 * Copyright 2019-2024 the original author or authors.
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

package org.dockbox.hartshorn.inject.processing.proxy;

import org.dockbox.hartshorn.inject.DefaultInjectionApplicationAwareContext;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

import java.lang.annotation.Annotation;

/**
 * TODO: #1060 Add documentation
 *
 * @param <T> ...
 *
 * @since 0.4.1
 *
 * @author Guus Lieben
 */
public class MethodProxyContextImpl<T> extends DefaultInjectionApplicationAwareContext implements MethodProxyContext<T> {

    private final TypeView<T> type;
    private final MethodView<T, ?> method;

    public MethodProxyContextImpl(InjectionCapableApplication application, TypeView<T> type, MethodView<T, ?> method) {
        super(application);
        this.type = type;
        this.method = method;
    }

    @Override
    public <A extends Annotation> A annotation(Class<A> annotation) {
        return this.method.annotations().get(annotation).orNull();
    }

    @Override
    public TypeView<T> type() {
        return this.type;
    }

    @Override
    public MethodView<T, ?> method() {
        return this.method;
    }
}
