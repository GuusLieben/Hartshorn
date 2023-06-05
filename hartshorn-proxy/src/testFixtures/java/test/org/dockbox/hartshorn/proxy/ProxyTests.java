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

package test.org.dockbox.hartshorn.proxy;

import org.dockbox.hartshorn.proxy.ApplicationProxier;
import org.dockbox.hartshorn.proxy.ApplicationProxierLoader;
import org.dockbox.hartshorn.proxy.Proxy;
import org.dockbox.hartshorn.proxy.ProxyFactory;
import org.dockbox.hartshorn.proxy.ProxyManager;
import org.dockbox.hartshorn.proxy.advice.intercept.MethodInterceptor;
import org.dockbox.hartshorn.proxy.advice.wrap.MethodWrapper;
import org.dockbox.hartshorn.proxy.advice.wrap.ProxyCallbackContext;
import org.dockbox.hartshorn.proxy.constraint.ProxyConstraintViolationException;
import org.dockbox.hartshorn.proxy.lookup.StateAwareProxyFactory;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.function.CheckedSupplier;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.view.ConstructorView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import test.org.dockbox.hartshorn.proxy.types.ConcreteProxyTarget;
import test.org.dockbox.hartshorn.proxy.types.FinalProxyTarget;

@SuppressWarnings("unchecked")
public abstract class ProxyTests {

    protected abstract ApplicationProxierLoader proxierLoader();
    protected abstract Introspector introspector();

    @Test
    void testConcreteMethodsCanBeProxied() throws ApplicationException, NoSuchMethodException {
        final Method name = ConcreteProxyTarget.class.getMethod("name");
        final ProxyFactory<ConcreteProxyTarget> handler = this.proxierLoader().create(this.introspector()).factory(ConcreteProxyTarget.class);
        handler.advisors().method(name).intercept(context -> "Hartshorn");
        final ConcreteProxyTarget proxy = handler.proxy().get();

        Assertions.assertNotNull(proxy);
        Assertions.assertNotNull(proxy.name());
        Assertions.assertEquals("Hartshorn", proxy.name());
    }

    @Test
    void testFinalMethodsCanNotBeProxied() throws NoSuchMethodException {
        final Method name = FinalProxyTarget.class.getMethod("name");
        final ProxyFactory<FinalProxyTarget> handler = this.proxierLoader()
                .create(this.introspector())
                .factory(FinalProxyTarget.class);

        Assertions.assertThrows(IllegalArgumentException.class, () -> handler.advisors()
                .method(name)
                .intercept(context -> "Hartshorn")
        );
    }

    public static Stream<Arguments> proxyTypes() {
        return Stream.of(
                Arguments.of(InterfaceProxy.class),
                Arguments.of(AbstractProxy.class),
                Arguments.of(ConcreteProxy.class)
        );
    }

    @Test
    void testRecordProxyCannotBeCreated() {
        // Records are final and cannot be proxied
        final ProxyFactory<RecordProxy> handler = this.proxierLoader().create(this.introspector()).factory(RecordProxy.class);
        Assertions.assertThrows(ProxyConstraintViolationException.class, handler::proxy);
    }

    @Test
    void testSealedClassProxyCannotBeCreated() {
        // Sealed classes only allow for a limited number of subclasses and should not be proxied
        final ProxyFactory<SealedProxy> handler = this.proxierLoader().create(this.introspector()).factory(SealedProxy.class);
        Assertions.assertThrows(ProxyConstraintViolationException.class, handler::proxy);
    }

    @Test
    void testFinalClassProxyCannotBeCreated() {
        // Final classes cannot be extended and should not be proxied
        final ProxyFactory<FinalProxy> handler = this.proxierLoader().create(this.introspector()).factory(FinalProxy.class);
        Assertions.assertThrows(ProxyConstraintViolationException.class, handler::proxy);
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testEmptyProxyCanCreate(final Class<? extends InterfaceProxy> proxyParent) throws ApplicationException {
        final ProxyFactory<? extends InterfaceProxy> handler = this.proxierLoader().create(this.introspector()).factory(proxyParent);
        final InterfaceProxy proxy = handler.proxy().get();
        Assertions.assertNotNull(proxy);
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testMethodsCanBeDelegatedToOriginalInstance(final Class<InterfaceProxy> proxyType) throws ApplicationException {
        final ProxyFactory<InterfaceProxy> factory = this.proxierLoader().create(this.introspector()).factory(proxyType);
        factory.advisors().type().delegate(new ConcreteProxy());
        final Option<InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final InterfaceProxy proxyInstance = proxy.get();
        Assertions.assertEquals("concrete", proxyInstance.name());
    }

    @Test
    void testConcreteProxyWithNonDefaultConstructorUsesConstructor() {
        final StateAwareProxyFactory<ConcreteProxyWithNonDefaultConstructor> factory = this.proxierLoader().create(this.introspector()).factory(ConcreteProxyWithNonDefaultConstructor.class);

        final TypeView<ConcreteProxyWithNonDefaultConstructor> typeView = this.introspector().introspect(ConcreteProxyWithNonDefaultConstructor.class);
        final ConstructorView<ConcreteProxyWithNonDefaultConstructor> constructor = typeView.constructors().all().get(0);
        final Option<ConcreteProxyWithNonDefaultConstructor> proxy = Assertions.assertDoesNotThrow(() -> factory.proxy(constructor, new Object[]{"Hello world"}));
        Assertions.assertTrue(proxy.present());

        final ConcreteProxyWithNonDefaultConstructor proxyInstance = proxy.get();
        Assertions.assertEquals("Hello world", proxyInstance.message());
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testMethodsCanBeIntercepted(final Class<? extends InterfaceProxy> proxyType) throws ApplicationException, NoSuchMethodException {
        final ProxyFactory<? extends InterfaceProxy> factory = this.proxierLoader().create(this.introspector()).factory(proxyType);
        factory.advisors().method(proxyType.getMethod("name")).intercept(context -> "Hartshorn");
        final Option<? extends InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final InterfaceProxy proxyInstance = proxy.get();
        Assertions.assertEquals("Hartshorn", proxyInstance.name());
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testMethodsCanBeDelegated(final Class<? extends InterfaceProxy> proxyType) throws ApplicationException, NoSuchMethodException {
        final ProxyFactory<InterfaceProxy> factory = (ProxyFactory<InterfaceProxy>) this.proxierLoader().create(this.introspector()).factory(proxyType);
        factory.advisors().method(proxyType.getMethod("name")).delegate(new ConcreteProxy());
        final Option<? extends InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final InterfaceProxy proxyInstance = proxy.get();
        Assertions.assertEquals("concrete", proxyInstance.name());
    }

    @Test
    void testTypesCanBeDelegated() throws ApplicationException {
        // Use a custom interface for this type of delegation, as the other proxy types override methods from their parent
        final ProxyFactory<NamedAgedProxy> factory = this.proxierLoader().create(this.introspector()).factory(NamedAgedProxy.class);
        factory.advisors().type(AgedProxy.class).delegate(() -> 12);
        factory.advisors().type(NamedProxy.class).delegate(() -> "NamedProxy");
        final Option<NamedAgedProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final NamedAgedProxy proxyInstance = proxy.get();
        Assertions.assertEquals(12, proxyInstance.age());
        Assertions.assertEquals("NamedProxy", proxyInstance.name());
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testWrapperInterceptionIsCorrect(final Class<? extends InterfaceProxy> proxyType) throws NoSuchMethodException, ApplicationException {
        final ProxyFactory<InterfaceProxy> factory = (ProxyFactory<InterfaceProxy>) this.proxierLoader().create(this.introspector()).factory(proxyType);
        final AtomicInteger count = new AtomicInteger();
        factory.advisors().method(proxyType.getMethod("name")).intercept(context -> "done");
        factory.advisors().method(proxyType.getMethod("name")).wrapAround(new MethodWrapper<>() {
            @Override
            public void acceptBefore(final ProxyCallbackContext<InterfaceProxy> context) {
                Assertions.assertEquals(0, count.getAndIncrement());
            }

            @Override
            public void acceptAfter(final ProxyCallbackContext<InterfaceProxy> context) {
                Assertions.assertEquals(1, count.getAndIncrement());
            }

            @Override
            public void acceptError(final ProxyCallbackContext<InterfaceProxy> context) {
                // Not thrown
                Assertions.fail();
            }
        });
        final Option<InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final InterfaceProxy proxyInstance = proxy.get();
        Assertions.assertEquals("done", proxyInstance.name());
        Assertions.assertEquals(2, count.get());
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testErrorWrapperInterceptionIsCorrect(final Class<? extends InterfaceProxy> proxyType) throws NoSuchMethodException, ApplicationException {
        final ProxyFactory<InterfaceProxy> factory = (ProxyFactory<InterfaceProxy>) this.proxierLoader().create(this.introspector()).factory((proxyType));
        final AtomicInteger count = new AtomicInteger();
        factory.advisors().method(proxyType.getMethod("name")).intercept(context -> {
            throw new IllegalStateException("not done");
        });
        factory.advisors().method(proxyType.getMethod("name")).wrapAround(new MethodWrapper<>() {
            @Override
            public void acceptBefore(final ProxyCallbackContext<InterfaceProxy> context) {
                Assertions.assertEquals(0, count.getAndIncrement());
            }

            @Override
            public void acceptAfter(final ProxyCallbackContext<InterfaceProxy> context) {
                Assertions.fail();
            }

            @Override
            public void acceptError(final ProxyCallbackContext<InterfaceProxy> context) {
                final Throwable error = context.error();
                Assertions.assertNotNull(error);
                Assertions.assertTrue(error instanceof IllegalStateException);
                Assertions.assertEquals("not done", error.getMessage());
                Assertions.assertEquals(1, count.getAndIncrement());
            }
        });
        final Option<InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final InterfaceProxy proxyInstance = proxy.get();
        final IllegalStateException error = Assertions.assertThrows(IllegalStateException.class, proxyInstance::name);
        Assertions.assertEquals("not done", error.getMessage());
        Assertions.assertEquals(2, count.get());
    }

    @Test
    void testProxyManagerTracksInterceptorsAndDelegates() throws NoSuchMethodException, ApplicationException {
        final ProxyFactory<NamedAgedProxy> factory = this.proxierLoader().create(this.introspector()).factory(NamedAgedProxy.class);

        final AgedProxy aged = () -> 12;
        factory.advisors().type(AgedProxy.class).delegate(aged);

        final MethodInterceptor<NamedAgedProxy, Object> named = context -> "NamedProxy";
        factory.advisors().method(NamedProxy.class.getMethod("name")).intercept(named);
        final Option<NamedAgedProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final Proxy<?> proxyInstance = (Proxy<?>) proxy.get();
        final ProxyManager<?> manager = proxyInstance.manager();

        final Option<?> agedDelegate = manager.advisor()
                .resolver()
                .type(AgedProxy.class)
                .delegate();
        Assertions.assertTrue(agedDelegate.present());
        Assertions.assertSame(agedDelegate.get(), aged);

        final Option<?> namedInterceptor = manager.advisor()
                .resolver()
                .method(NamedProxy.class.getMethod("name"))
                .interceptor();
        Assertions.assertTrue(namedInterceptor.present());
        Assertions.assertSame(namedInterceptor.get(), named);
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testProxyCanHaveExtraInterfaces(final Class<? extends InterfaceProxy> proxyType) throws ApplicationException {
        final ProxyFactory<InterfaceProxy> factory = (ProxyFactory<InterfaceProxy>) this.proxierLoader().create(this.introspector()).factory(proxyType);
        factory.implement(DescribedProxy.class);
        final Option<InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final InterfaceProxy proxyInstance = proxy.get();
        Assertions.assertTrue(proxyInstance instanceof DescribedProxy);
    }


    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testProxiesAlwaysImplementProxyType(final Class<? extends InterfaceProxy> proxyType) throws ApplicationException {
        final ProxyFactory<InterfaceProxy> factory = (ProxyFactory<InterfaceProxy>) this.proxierLoader().create(this.introspector()).factory(proxyType);
        final Option<InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());
        final InterfaceProxy proxyInstance = proxy.get();
        Assertions.assertTrue(proxyInstance instanceof Proxy);
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testProxiesExposeManager(final Class<? extends InterfaceProxy> proxyType) throws ApplicationException {
        final ProxyFactory<InterfaceProxy> factory = (ProxyFactory<InterfaceProxy>) this.proxierLoader().create(this.introspector()).factory(proxyType);
        final Option<InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final Proxy<?> proxyInstance = (Proxy<?>) proxy.get();
        Assertions.assertNotNull(proxyInstance.manager());
    }

    @ParameterizedTest
    @MethodSource("proxyTypes")
    void testProxyManagerExposesTargetAndProxyType(final Class<? extends InterfaceProxy> proxyType) throws ApplicationException {
        final ProxyFactory<InterfaceProxy> factory = (ProxyFactory<InterfaceProxy>) this.proxierLoader().create(this.introspector()).factory(proxyType);
        final Option<InterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final ProxyManager<InterfaceProxy> manager = ((Proxy<InterfaceProxy>) proxy.get()).manager();
        Assertions.assertNotNull(manager.proxyClass());
        Assertions.assertNotNull(manager.targetClass());

        Assertions.assertNotEquals(proxyType, manager.proxyClass());
        Assertions.assertSame(proxyType, manager.targetClass());

        Assertions.assertTrue(manager.applicationProxier().isProxy(manager.proxyClass()));
    }

    @Test
    void testInterfaceProxyDoesNotEqual() throws ApplicationException {
        final DemoServiceA serviceA1 = this.createProxy(DemoServiceA.class);
        final DemoServiceA serviceA2 = this.createProxy(DemoServiceA.class);

        Assertions.assertNotSame(serviceA1, serviceA2);
        Assertions.assertNotEquals(serviceA1, serviceA2);
    }

    @Test
    void testAbstractClassProxyDoesNotEqual() throws ApplicationException {
        final DemoServiceB serviceC1 = this.createProxy(DemoServiceB.class);
        final DemoServiceB serviceC2 = this.createProxy(DemoServiceB.class);

        Assertions.assertNotSame(serviceC1, serviceC2);
        Assertions.assertNotEquals(serviceC1, serviceC2);
    }

    @Test
    void testConcreteClassProxyWithoutDelegateDoesNotEqual() throws ApplicationException {
        final DemoServiceC serviceB1 = this.createProxy(DemoServiceC.class);
        final DemoServiceC serviceB2 = this.createProxy(DemoServiceC.class);

        Assertions.assertNotSame(serviceB1, serviceB2);
        Assertions.assertNotEquals(serviceB1, serviceB2);
    }

    @Test
    public void testConcreteClassProxyWithNonEqualsImplementedDelegateDoesNotEqual() throws ApplicationException {
        final CheckedSupplier<DemoServiceC> supplier = () -> this.proxierLoader().create(this.introspector())
                .factory(DemoServiceC.class)
                .advisors(advisors -> advisors.type().delegate(new DemoServiceC()))
                .proxy()
                .get();

        final DemoServiceC serviceC3 = supplier.get();
        final DemoServiceC serviceC4 = supplier.get();

        Assertions.assertNotSame(serviceC3, serviceC4);
        Assertions.assertNotEquals(serviceC3, serviceC4);
    }

    @Test
    void testConcreteClassProxyWithDelegateDoesNotEqual() throws ApplicationException {
        final CheckedSupplier<DemoServiceD> supplier = () -> this.proxierLoader().create(this.introspector())
                .factory(DemoServiceD.class)
                .advisors(advisors -> advisors.type().delegate(new DemoServiceD("name")))
                .proxy()
                .get();

        final DemoServiceD serviceD1 = supplier.get();
        final DemoServiceD serviceD2 = supplier.get();

        Assertions.assertNotSame(serviceD1, serviceD2);
        Assertions.assertEquals(serviceD1, serviceD2);
    }

    private <T> T createProxy(final Class<T> type) throws ApplicationException {
        return this.proxierLoader().create(this.introspector()).factory(type).proxy().get();
    }

    public interface DemoServiceA { }

    public abstract static class DemoServiceB { }

    public static class DemoServiceC { }

    public static class DemoServiceD {
        private String name;

        public DemoServiceD(final String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public DemoServiceD() {
            // Default constructor for proxier. Note that this is typically handled by providing a constructor to the
            // proxier factory, but this is a test, so we're not doing that.
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) return true;
            if (other == null || this.getClass() != other.getClass()) return false;
            final DemoServiceD service = (DemoServiceD) other;
            return this.name.equals(service.name);
        }
    }

    @Test
    void testConcreteProxySelfEquality() throws ApplicationException {
        final ProxyFactory<EqualProxy> factory = this.proxierLoader().create(this.introspector()).factory(EqualProxy.class);
        final Option<EqualProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final EqualProxy proxyInstance = proxy.get();
        Assertions.assertEquals(proxyInstance, proxyInstance);
        Assertions.assertTrue(proxyInstance.test(proxyInstance));
    }

    @Test
    void testServiceSelfEquality() throws ApplicationException {
        final EqualServiceProxy service = this.proxierLoader().create(this.introspector()).factory(EqualServiceProxy.class).proxy().get();
        Assertions.assertEquals(service, service);
        Assertions.assertTrue(service.test(service));
    }

    @Test
    void testInterfaceProxySelfEquality() throws ApplicationException {
        final ProxyFactory<EqualInterfaceProxy> factory = this.proxierLoader().create(this.introspector()).factory(EqualInterfaceProxy.class);
        final Option<EqualInterfaceProxy> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());

        final EqualInterfaceProxy proxyInstance = proxy.get();
        Assertions.assertEquals(proxyInstance, proxyInstance);
        Assertions.assertTrue(proxyInstance.test(proxyInstance));
    }

    @Test
    void testLambdaCanBeProxied() throws NoSuchMethodException, ApplicationException {
        final Class<Supplier<String>> supplierClass = (Class<Supplier<String>>) (Class<?>) Supplier.class;
        final StateAwareProxyFactory<Supplier<String>> factory = this.proxierLoader().create(this.introspector()).factory(supplierClass);
        factory.advisors().method(Supplier.class.getMethod("get")).intercept(context -> "foo");
        final Option<Supplier<String>> proxy = factory.proxy();
        Assertions.assertTrue(proxy.present());
        Assertions.assertEquals("foo", proxy.get().get());
    }

    @Test
    void testIsProxyIsTrueIfTypeIsProxy() throws ApplicationException {
        final Introspector introspector = this.introspector();
        final ApplicationProxier proxier = this.proxierLoader().create(introspector);
        final ProxyFactory<?> factory = proxier.factory(Object.class);
        final Object proxy = factory.proxy().get();

        final boolean instanceIsProxy = proxier.isProxy(proxy);
        Assertions.assertTrue(instanceIsProxy);

        final boolean typeIsProxy = proxier.isProxy(proxy.getClass());
        Assertions.assertTrue(typeIsProxy);
    }

    @Test
    void testIsProxyIsFalseIfTypeIsNormal() {
        final Introspector introspector = this.introspector();
        final ApplicationProxier proxier = this.proxierLoader().create(introspector);
        final TypeView<?> view = introspector.introspect(Object.class);
        final boolean isProxy = proxier.isProxy(view);
        Assertions.assertFalse(isProxy);
    }
}
