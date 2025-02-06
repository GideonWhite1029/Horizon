package dev.gideonwhite1029.horizon.bytebuf.packet;

import org.bukkit.entity.Player;

public interface PacketListener {

    /**
     * Handles an incoming packet from a player.
     *
     * @param player the player sending the packet
     * @param packet the packet being sent
     * @return the processed packet
     */
    Packet onPacketIn(Player player, Packet packet);

    /**
     * Handles an outgoing packet to a player.
     *
     * @param player the player receiving the packet
     * @param packet the packet being sent
     * @return the processed packet
     */
    Packet onPacketOut(Player player, Packet packet);
}