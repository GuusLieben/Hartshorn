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

package org.dockbox.hartshorn.application.context;

import org.dockbox.hartshorn.component.Scope;
import org.dockbox.hartshorn.inject.Provider;
import org.dockbox.hartshorn.inject.binding.Binder;
import org.dockbox.hartshorn.inject.binding.BindingFunction;
import org.dockbox.hartshorn.util.function.CheckedSupplier;

public class DelegatingApplicationBindingFunction<T> implements BindingFunction<T> {

    private final ApplicationContext applicationContext;
    private final BindingFunction<T> delegate;

    public DelegatingApplicationBindingFunction(final ApplicationContext applicationContext, final BindingFunction<T> delegate) {
        this.applicationContext = applicationContext;
        this.delegate = delegate;
    }

    @Override
    public BindingFunction<T> installTo(final Class<? extends Scope> scope) {
        return this.delegate.installTo(scope);
    }

    @Override
    public BindingFunction<T> priority(final int priority) {
        return this.delegate.priority(priority);
    }

    @Override
    public ApplicationContext to(final Class<? extends T> type) {
        this.delegate.to(type);
        return this.applicationContext;
    }

    @Override
    public ApplicationContext to(final CheckedSupplier<T> supplier) {
        this.delegate.to(supplier);
        return this.applicationContext;
    }

    @Override
    public Binder to(final Provider<T> provider) {
        return this.delegate.to(provider);
    }

    @Override
    public ApplicationContext singleton(final T instance) {
        this.delegate.singleton(instance);
        return this.applicationContext;
    }

    @Override
    public ApplicationContext lazySingleton(final Class<T> type) {
        this.delegate.lazySingleton(type);
        return this.applicationContext;
    }

    @Override
    public ApplicationContext lazySingleton(final CheckedSupplier<T> supplier) {
        this.delegate.lazySingleton(supplier);
        return this.applicationContext;
    }
}
