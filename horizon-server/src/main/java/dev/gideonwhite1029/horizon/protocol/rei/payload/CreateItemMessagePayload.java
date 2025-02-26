package dev.gideonwhite1029.horizon.protocol.rei.payload;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import dev.gideonwhite1029.horizon.protocol.rei.REIServerProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record CreateItemMessagePayload(ItemStack item, String playerName) implements HorizonCustomPayload<CreateItemMessagePayload> {

    private static final ResourceLocation ID = REIServerProtocol.id("ci_msg");

    @New
    public CreateItemMessagePayload(ResourceLocation location, @NotNull FriendlyByteBuf buf) {
        this(buf.readJsonWithCodec(ItemStack.OPTIONAL_CODEC), buf.readUtf());
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, item);
        buf.writeUtf(playerName);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
