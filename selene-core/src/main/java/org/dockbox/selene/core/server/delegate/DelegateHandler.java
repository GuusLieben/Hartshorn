package org.dockbox.selene.core.server.delegate;

import org.dockbox.selene.core.server.properties.DelegateProperty;
import org.dockbox.selene.core.util.SeleneUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class DelegateHandler<T, R> implements MethodHandler {

    private final Map<Method, DelegateProperty<T, R>> handlers = SeleneUtils.emptyMap();
    private final T instance;

    public DelegateHandler(T instance) {
        this.instance = instance;
    }

    public void delegate(DelegateProperty<T, R> property) {
        this.handlers.put(property.getTarget(), property);
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        if (this.handlers.containsKey(thisMethod)) {
            return this.handlers.get(thisMethod).getDelegate().apply(this.instance, args);
        } else {
            return thisMethod.invoke(this.instance, args);
        }
    }

    public T proxy() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(this.instance.getClass());
        //noinspection unchecked
        return (T) factory.create(new Class<?>[0], new Object[0], this);
    }
}
