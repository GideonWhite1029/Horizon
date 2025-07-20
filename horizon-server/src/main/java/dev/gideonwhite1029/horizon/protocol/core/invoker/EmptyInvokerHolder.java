package dev.gideonwhite1029.horizon.protocol.core.invoker;

import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;

import java.lang.reflect.Method;

public class EmptyInvokerHolder<T> extends AbstractInvokerHolder<T> {
    public EmptyInvokerHolder(HorizonProtocol owner, Method invoker, T handler) {
        super(owner, invoker, handler, null);
    }

    public void invoke() {
        invoke0(false);
    }
}
