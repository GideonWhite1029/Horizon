package dev.gideonwhite1029.horizon.bytebuf;

import dev.gideonwhite1029.horizon.bytebuf.packet.PacketListener;
import org.bukkit.plugin.Plugin;

public interface BytebufManager {

    /**
     * Registers a packet listener for the specified plugin.
     *
     * @param plugin the plugin to register the listener
     * @param listener the packet listener to register
     */
    @Deprecated
    void registerListener(Plugin plugin, PacketListener listener);

    /**
     * Unregisters a packet listener for the specified plugin.
     *
     * @param plugin the plugin to unregister the listener
     * @param listener the packet listener to unregister
     */
    @Deprecated
    void unregisterListener(Plugin plugin, PacketListener listener);

    /**
     * Creates a new Bytebuf with the specified size.
     *
     * @param size the size of the Bytebuf
     * @return a new Bytebuf instance
     */
    Bytebuf newBytebuf(int size);

    /**
     * Converts a byte array to a Bytebuf.
     *
     * @param bytes the byte array to convert
     * @return a new Bytebuf instance
     */
    Bytebuf toBytebuf(byte[] bytes);
}
