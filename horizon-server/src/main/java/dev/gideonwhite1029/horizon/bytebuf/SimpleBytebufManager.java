package dev.gideonwhite1029.horizon.bytebuf;

import dev.gideonwhite1029.horizon.bytebuf.packet.PacketListener;
import io.netty.buffer.Unpooled;
import org.bukkit.plugin.Plugin;

public class SimpleBytebufManager implements BytebufManager {

    private final InternalBytebufHandler internal;

    public SimpleBytebufManager(InternalBytebufHandler internal) {
        this.internal = internal;
    }

    @Override
    public void registerListener(Plugin plugin, PacketListener listener) {
        internal.listenerMap.put(listener, plugin);
    }

    @Override
    public void unregisterListener(Plugin plugin, PacketListener listener) {
        internal.listenerMap.remove(listener);
    }

    @Override
    public Bytebuf newBytebuf(int size) {
        return new WrappedBytebuf(Unpooled.buffer(size));
    }

    @Override
    public Bytebuf toBytebuf(byte[] bytes) {
        return new WrappedBytebuf(Unpooled.wrappedBuffer(bytes));
    }
}
