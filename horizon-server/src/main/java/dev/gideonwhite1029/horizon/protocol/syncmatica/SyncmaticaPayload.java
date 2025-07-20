package dev.gideonwhite1029.horizon.protocol.syncmatica;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record SyncmaticaPayload(ResourceLocation packetType, FriendlyByteBuf data) implements HorizonCustomPayload {

    @ID
    private static final ResourceLocation NETWORK_ID = ResourceLocation.tryBuild(SyncmaticaProtocol.PROTOCOL_ID, "main");

    @Codec
    private static final StreamCodec<FriendlyByteBuf, SyncmaticaPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeResourceLocation(payload.packetType()).writeBytes(payload.data()),
        buf -> new SyncmaticaPayload(buf.readResourceLocation(), new FriendlyByteBuf(buf.readBytes(buf.readableBytes())))
    );
}
