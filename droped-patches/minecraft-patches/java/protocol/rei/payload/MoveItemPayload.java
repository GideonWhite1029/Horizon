package dev.gideonwhite1029.horizon.protocol.rei.payload;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import dev.gideonwhite1029.horizon.protocol.rei.REIServerProtocol;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MoveItemPayload(ResourceLocation category, boolean isShift, CompoundTag nbt) implements HorizonCustomPayload<MoveItemPayload> {

    private static final ResourceLocation ID = REIServerProtocol.id("move_items_new");

    @New
    public MoveItemPayload(ResourceLocation location, @NotNull FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readBoolean(), buf.readNbt());
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(category);
        buf.writeBoolean(isShift);
        buf.writeNbt(nbt);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
