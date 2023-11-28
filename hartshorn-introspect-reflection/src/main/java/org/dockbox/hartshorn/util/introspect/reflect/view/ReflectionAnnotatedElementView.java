/*
 * Copyright 2019-2023 the original author or authors.
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

package org.dockbox.hartshorn.util.introspect.reflect.view;

import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.util.introspect.ElementAnnotationsIntrospector;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.reflect.ReflectionElementAnnotationsIntrospector;
import org.dockbox.hartshorn.util.introspect.reflect.ReflectionIntrospector;
import org.dockbox.hartshorn.util.introspect.view.AnnotatedElementView;
import org.dockbox.hartshorn.util.introspect.view.IntrospectorAwareView;

import java.lang.reflect.AnnotatedElement;

public abstract class ReflectionAnnotatedElementView extends DefaultContext implements AnnotatedElementView, IntrospectorAwareView {

    private final ReflectionIntrospector introspector;
    private ElementAnnotationsIntrospector annotationsIntrospector;

    protected ReflectionAnnotatedElementView(ReflectionIntrospector introspector) {
        this.introspector = introspector;
    }

    protected abstract AnnotatedElement annotatedElement();

    @Override
    public ElementAnnotationsIntrospector annotations() {
        if (this.annotationsIntrospector == null) {
            this.annotationsIntrospector = new ReflectionElementAnnotationsIntrospector(this.introspector, this.annotatedElement());
        }
        return this.annotationsIntrospector;
    }

    @Override
    public Introspector introspector() {
        return this.introspector;
    }
}
