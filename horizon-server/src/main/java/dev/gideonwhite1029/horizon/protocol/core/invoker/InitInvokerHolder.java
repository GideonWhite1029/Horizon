package dev.gideonwhite1029.horizon.protocol.core.invoker;

import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class InitInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.Init> {
    public InitInvokerHolder(HorizonProtocol owner, Method invoker, ProtocolHandler.Init handler) {
        super(owner, invoker, handler, null);
    }

    public void invoke() {
        invoke0(true);
    }
}
