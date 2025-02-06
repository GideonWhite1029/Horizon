package dev.gideonwhite1029.horizon.bytebuf.packet;

import dev.gideonwhite1029.horizon.bytebuf.Bytebuf;

/**
 * Represents a packet with a specific type and associated byte buffer.
 *
 * @param type the type of the packet
 * @param bytebuf the byte buffer associated with the packet
 */
public record Packet(PacketType type, Bytebuf bytebuf) {
}
