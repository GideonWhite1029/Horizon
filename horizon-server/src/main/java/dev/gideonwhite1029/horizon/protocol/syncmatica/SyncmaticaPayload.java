package dev.gideonwhite1029.horizon.protocol.syncmatica;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SyncmaticaPayload(Identifier packetType, FriendlyByteBuf data) implements HorizonCustomPayload {

    @ID
    private static final Identifier NETWORK_ID = Identifier.tryBuild(SyncmaticaProtocol.PROTOCOL_ID, "main");

    @Codec
    private static final StreamCodec<FriendlyByteBuf, SyncmaticaPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeIdentifier(payload.packetType()).writeBytes(payload.data()),
            buf -> new SyncmaticaPayload(buf.readIdentifier(), new FriendlyByteBuf(buf.readBytes(buf.readableBytes())))
    );
}
