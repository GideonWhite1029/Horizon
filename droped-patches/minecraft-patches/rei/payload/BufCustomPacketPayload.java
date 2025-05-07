package dev.gideonwhite1029.horizon.protocol.rei.payload;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BufCustomPacketPayload(
    Type<BufCustomPacketPayload> type,
    byte[] payload
) implements HorizonCustomPayload<BufCustomPacketPayload> {
    @New
    public static BufCustomPacketPayload create(ResourceLocation location, @NotNull FriendlyByteBuf buf) {
        return new BufCustomPacketPayload(new Type<>(location), buf.readByteArray());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        FriendlyByteBuf.writeByteArray(buf, this.payload);
    }

    @Override
    public ResourceLocation id() {
        return type.id();
    }

    @NotNull
    @Override
    public Type<BufCustomPacketPayload> type() {
        return type;
    }
}
