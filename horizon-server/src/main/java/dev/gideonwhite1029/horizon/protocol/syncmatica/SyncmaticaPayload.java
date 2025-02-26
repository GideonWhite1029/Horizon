package dev.gideonwhite1029.horizon.protocol.syncmatica;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SyncmaticaPayload(ResourceLocation packetType, FriendlyByteBuf data) implements HorizonCustomPayload<SyncmaticaPayload> {

    private static final ResourceLocation NETWORK_ID = ResourceLocation.tryBuild(SyncmaticaProtocol.PROTOCOL_ID, "main");

    @New
    public static SyncmaticaPayload decode(ResourceLocation location, FriendlyByteBuf buf) {
        return new SyncmaticaPayload(buf.readResourceLocation(), new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.packetType);
        buf.writeBytes(this.data.readBytes(this.data.readableBytes()));
    }

    @Override
    public ResourceLocation id() {
        return NETWORK_ID;
    }
}
