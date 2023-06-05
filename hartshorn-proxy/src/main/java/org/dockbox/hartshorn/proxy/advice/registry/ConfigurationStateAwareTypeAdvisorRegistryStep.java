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

package org.dockbox.hartshorn.proxy.advice.registry;

import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.option.Option;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Basic implementation of {@link StateAwareTypeAdvisorRegistryStep}. This implementation is used to configure the
 * {@link StateAwareAdvisorRegistry} by adding class and instance delegates. As both the registry and this registry
 * step are stateful, the registry is marked as modified when this step is configured.
 *
 * @param <S> the advised type, which is assignable to the registry's advised type
 * @param <T> the type of the proxy object
 *
 * @author Guus Lieben
 * @since 23.1
 */
public class ConfigurationStateAwareTypeAdvisorRegistryStep<S, T> implements StateAwareTypeAdvisorRegistryStep<S, T> {

    private final StateAwareAdvisorRegistry<T> registry;
    private final Class<S> type;
    private S delegate;

    public ConfigurationStateAwareTypeAdvisorRegistryStep(final StateAwareAdvisorRegistry<T> registry, final Class<S> type) {
        this.registry = registry;
        this.type = type;
    }

    @Override
    public Option<S> delegate() {
        return Option.of(this.delegate);
    }

    @Override
    public Class<S> advisedType() {
        return this.type;
    }

    @Override
    public AdvisorRegistry<T> delegate(final S delegateInstance) {
        if (delegateInstance == null) {
            throw new IllegalArgumentException("Delegate cannot be null");
        }
        for (final Method declaredMethod : this.type.getDeclaredMethods()) {
            this.addDelegateAdvice(delegateInstance, declaredMethod);
        }
        this.delegate = delegateInstance;
        this.registry.state().modify();
        return this.registry;
    }

    @Override
    public AdvisorRegistry<T> delegateAbstractOnly(final S delegateInstance) {
        if (delegateInstance == null) {
            throw new IllegalArgumentException("Delegate cannot be null");
        }
        for (final Method declaredMethod : this.type.getDeclaredMethods()) {
            this.delegateAbstractOverrideCandidate(delegateInstance, declaredMethod);
        }
        if (this.registry.advisedType() == delegateInstance.getClass()) {
            this.delegate = delegateInstance;
            this.registry.state().modify();
        }
        return this.registry;
    }

    private void delegateAbstractOverrideCandidate(final S delegateInstance, final Method declaredMethod) {
        try {
            final Method override = this.registry.advisedType().getMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
            if (!Modifier.isAbstract(override.getModifiers()) || override.isDefault() || declaredMethod.isDefault()) {
                return;
            }
        }
        catch (final NoSuchMethodException e) {
            // Ignore error, delegate is not concrete
        }
        this.addDelegateAdvice(delegateInstance, declaredMethod);
    }

    private void addDelegateAdvice(final S delegateInstance, final Method declaredMethod) {
        final MethodAdvisorRegistryStep<T, ?> method = this.registry.method(declaredMethod);
        method.delegate(TypeUtils.adjustWildcards(delegateInstance, Object.class));
        this.registry.state().modify();
    }
}
