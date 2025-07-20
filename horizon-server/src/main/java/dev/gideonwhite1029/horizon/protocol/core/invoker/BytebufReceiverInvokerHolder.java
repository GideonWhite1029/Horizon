package dev.gideonwhite1029.horizon.protocol.core.invoker;

import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import dev.gideonwhite1029.horizon.protocol.core.IdentifierSelector;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolHandler;
import net.minecraft.network.FriendlyByteBuf;


import java.lang.reflect.Method;

public class BytebufReceiverInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.BytebufReceiver> {
    public BytebufReceiverInvokerHolder(HorizonProtocol owner, Method invoker, ProtocolHandler.BytebufReceiver handler) {
        super(owner, invoker, handler, null, handler.stage().identifier(), FriendlyByteBuf.class);
    }

    public boolean invoke(IdentifierSelector selector, FriendlyByteBuf buf) {
        return invoke0(false, selector.select(handler.stage()), buf) instanceof Boolean b && b;
    }
}