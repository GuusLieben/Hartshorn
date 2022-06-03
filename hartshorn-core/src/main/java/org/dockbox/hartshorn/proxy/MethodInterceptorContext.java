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

package org.dockbox.hartshorn.proxy;

import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * The context of a {@link MethodInterceptor}. It contains the method to be intercepted, the arguments to be passed to the method,
 * the return type of the method, the {@link MethodContext} of the method, the instance of the object to be intercepted, and
 * utility callables to call the underlying method.
 *
 * @param <T> the type of the proxy object
 * @author Guus Lieben
 * @since 22.2
 */
public class MethodInterceptorContext<T> {

    private final MethodContext<?, T> method;
    private final Object[] args;
    private final T instance;
    private final Callable<Object> callable;
    private final CustomInvocation customInvocation;
    private final Object result;

    public MethodInterceptorContext(final Method method, final Object[] args, final T instance, final Callable<Object> callable, final CustomInvocation customInvocation, final Object result) {
        this.method = (MethodContext<?, T>) MethodContext.of(method);
        this.args = args;
        this.instance = instance;
        this.callable = callable;
        this.customInvocation = customInvocation;
        this.result = result;
    }

    public MethodInterceptorContext(final MethodInterceptorContext<T> context, final Object result) {
        this(context.method.method(), context.args, context.instance, context.callable, context.customInvocation, result);
    }

    public MethodInterceptorContext(final Method method, final Object[] args, final T instance, final CustomInvocation customInvocation) {
        this(method, args, instance, customInvocation.toCallable(args), customInvocation, MethodContext.of(method).returnType().defaultOrNull());
    }

    /**
     * Returns the intercepted method, as it was defined on the original class.
     * @return the intercepted method
     */
    public MethodContext<?, T> method() {
        return this.method;
    }

    /**
     * Returns the arguments which were originally passed to the intercepted method.
     * @return the arguments which were originally passed to the intercepted method
     */
    public Object[] args() {
        return this.args;
    }

    /**
     * Returns the instance of the intercepted object. If an instance delegate exists for the active proxy, this delegate will be
     * returned. Otherwise, the proxy instance itself will be returned.
     * @return the instance of the intercepted object
     */
    public T instance() {
        return this.instance;
    }

    /**
     * Invokes the underlying method with the original arguments. This allows the intercepted method to be invoked without
     * any additional logic.
     *
     * @return the result of the underlying method
     * @throws Throwable if the underlying method throws an exception
     */
    public Object invokeDefault() throws Throwable {
        if (this.callable != null) {
            return this.callable.call();
        }
        return this.result();
    }

    /**
     * Invokes the underlying method with the given arguments. This allows the intercepted method to be invoked without any
     * additional logic.
     *
     * @param args the arguments to pass to the underlying method
     * @return the result of the underlying method
     * @throws Throwable if the underlying method throws an exception
     */
    public Object invokeDefault(final Object... args) throws Throwable {
        if (this.customInvocation != null) {
            return this.customInvocation.call(args);
        }
        return this.result();
    }

    /**
     * The result of the previous interceptor, if any. If this is the first interceptor, the result will be the default value
     * for the return type of the intercepted method.
     *
     * @return the result of the previous interceptor, if any
     * @see TypeContext#defaultOrNull()
     */
    public Object result() {
        return this.result;
    }
}
