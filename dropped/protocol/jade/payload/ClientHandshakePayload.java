package dev.gideonwhite1029.horizon.protocol.jade.payload;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ClientHandshakePayload(String protocolVersion) implements HorizonCustomPayload {

    @ID
    private static final ResourceLocation PACKET_CLIENT_HANDSHAKE = JadeProtocol.id("client_handshake");

    @Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, ClientHandshakePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClientHandshakePayload::protocolVersion, ClientHandshakePayload::new
    );
}