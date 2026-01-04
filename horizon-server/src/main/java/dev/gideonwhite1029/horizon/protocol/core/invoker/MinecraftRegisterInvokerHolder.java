package dev.gideonwhite1029.horizon.protocol.core.invoker;

import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import dev.gideonwhite1029.horizon.protocol.core.IdentifierSelector;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolHandler;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Method;

public class MinecraftRegisterInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.MinecraftRegister> {
    public MinecraftRegisterInvokerHolder(HorizonProtocol owner, Method invoker, ProtocolHandler.MinecraftRegister handler) {
        super(owner, invoker, handler, null, handler.stage().identifier(), Identifier.class);
    }

    public void invoke(IdentifierSelector selector, Identifier id) {
        invoke0(false, selector.select(handler.stage()), id);
    }
}
