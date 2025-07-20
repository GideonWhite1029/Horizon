package dev.gideonwhite1029.horizon.protocol.core.invoker;


import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import dev.gideonwhite1029.horizon.protocol.core.IdentifierSelector;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class PayloadReceiverInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.PayloadReceiver> {
    public PayloadReceiverInvokerHolder(HorizonProtocol owner, Method invoker, ProtocolHandler.PayloadReceiver handler) {
        super(owner, invoker, handler, null, handler.stage().identifier(), handler.payload());
    }

    public void invoke(IdentifierSelector selector, HorizonCustomPayload payload) {
        invoke0(false, selector.select(handler.stage()), payload);
    }
}
