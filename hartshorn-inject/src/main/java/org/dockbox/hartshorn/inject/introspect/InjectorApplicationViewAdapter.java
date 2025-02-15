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

package org.dockbox.hartshorn.inject.introspect;

import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.ComponentRequestContext;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.scope.Scope;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.introspect.util.ParameterLoaderRule;
import org.dockbox.hartshorn.util.introspect.view.ConstructorView;
import org.dockbox.hartshorn.util.introspect.view.ExecutableElementView;
import org.dockbox.hartshorn.util.introspect.view.FieldView;
import org.dockbox.hartshorn.util.introspect.view.GenericTypeView;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

/**
 * TODO: #1060 Add documentation
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class InjectorApplicationViewAdapter extends DefaultContext implements ViewContextAdapter {

    private final InjectionCapableApplication application;
    private final Scope scope;

    public InjectorApplicationViewAdapter(InjectorApplicationViewAdapter adapter, Scope scope) {
        this.application = adapter.application;
        this.scope = scope;
    }

    public InjectorApplicationViewAdapter(InjectionCapableApplication applicationContext) {
        this.application = applicationContext;
        this.scope = null; // default scope
    }

    private ComponentRequestContext componentRequestContext() {
        return this.firstContext(ComponentRequestContext.class)
                .orElseGet(ComponentRequestContext::createForComponent);
    }

    @Override
    public ViewContextAdapter scope(Scope scope) {
        return new InjectorApplicationViewAdapter(this, scope);
    }

    @Override
    public <T> Option<T> create(ConstructorView<T> constructor) throws Throwable {
        Object[] parameters = this.loadParameters(constructor);
        return Option.of(constructor.create(parameters));
    }

    @Override
    public Object[] loadParameters(ExecutableElementView<?> element) {
        ExecutableElementContextParameterLoader parameterLoader = new ExecutableElementContextParameterLoader(
            this.application
        );

        ParameterLoaderRule<ApplicationBoundParameterLoaderContext> rule = new InjectionPointParameterLoaderRule(this.componentRequestContext());
        parameterLoader.add(rule);

        ApplicationBoundParameterLoaderContext loaderContext = new ApplicationBoundParameterLoaderContext(element, null, this.application, this.scope());
        this.copyToContext(loaderContext);
        return parameterLoader.loadArguments(loaderContext).toArray();
    }

    @Override
    public <P, R> Option<R> invoke(MethodView<P, R> method) throws Throwable {
        if (method.modifiers().isStatic()) {
            return this.invokeStatic(method);
        }
        Object[] parameters = this.loadParameters(method);
        P instance = this.application.defaultProvider().get(this.key(method.declaredBy().type()), this.componentRequestContext());
        return method.invoke(instance, parameters);
    }

    @Override
    public <P, R> Option<R> invokeStatic(MethodView<P, R> method) throws Throwable {
        if (!method.modifiers().isStatic()) {
            return this.invoke(method);
        }
        Object[] parameters = this.loadParameters(method);
        return method.invokeStatic(parameters);
    }

    @Override
    public <P, R> Option<R> load(FieldView<P, R> field) throws Throwable {
        P instance = this.application.defaultProvider().get(this.key(field.declaredBy().type()), this.componentRequestContext());
        return field.get(instance);
    }

    @Override
    public <T> Option<T> load(GenericTypeView<T> element) throws Throwable {
        return switch(element) {
            case TypeView<?> typeView -> {
                ComponentKey<T> key = this.key(TypeUtils.unchecked(typeView.type(), Class.class));
                yield Option.of(this.application.defaultProvider().get(key, this.componentRequestContext()));
            }
            case FieldView<?, ?> fieldView -> this.load(TypeUtils.unchecked(fieldView, FieldView.class));
            case MethodView<?, ?> methodView -> this.invoke(TypeUtils.unchecked(methodView, MethodView.class));
            case ConstructorView<?> constructorView -> this.create(TypeUtils.unchecked(constructorView, ConstructorView.class));
            case ParameterView<?> parameterView -> {
                ComponentKey<T> key = this.key(TypeUtils.unchecked(parameterView.type().type(), Class.class));
                yield Option.of(this.application.defaultProvider().get(key, this.componentRequestContext()));
            }
            default -> throw new IllegalArgumentException("Unsupported element type: " + element.getClass().getName());
        };
    }

    @Override
    public boolean isProxy(TypeView<?> type) {
        return !type.isWildcard() && this.application.environment().proxyOrchestrator().isProxy(type.type());
    }

    private <T> ComponentKey<T> key(Class<T> type) {
        return ComponentKey.builder(type).scope(this.scope()).build();
    }

    private Scope scope() {
        return this.scope != null ? this.scope : this.application.defaultProvider().scope();
    }
}
