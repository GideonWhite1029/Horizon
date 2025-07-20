package dev.gideonwhite1029.horizon.protocol.core.invoker;

import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;

public class PlayerInvokerHolder<T> extends AbstractInvokerHolder<T> {
    public PlayerInvokerHolder(HorizonProtocol owner, Method invoker, T handler) {
        super(owner, invoker, handler, null, ServerPlayer.class);
    }

    public void invoke(ServerPlayer player) {
        invoke0(false, player);
    }
}
