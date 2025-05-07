package dev.gideonwhite1029.horizon.protocol.jade.payload;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ReceiveDataPayload(CompoundTag tag) implements HorizonCustomPayload<ReceiveDataPayload> {

    private static final ResourceLocation PACKET_RECEIVE_DATA = JadeProtocol.id("receive_data");

    @New
    public ReceiveDataPayload(ResourceLocation id, FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    @Override
    public ResourceLocation id() {
        return PACKET_RECEIVE_DATA;
    }
}
