package dev.gideonwhite1029.horizon.protocol.jade.provider.block;

import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.BlockAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.provider.ItemStorageProvider;
import dev.gideonwhite1029.horizon.protocol.jade.provider.StreamServerDataProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ChiseledBookshelfProvider implements StreamServerDataProvider<BlockAccessor, ItemStack> {
    INSTANCE;

    private static final ResourceLocation MC_CHISELED_BOOKSHELF = JadeProtocol.mc_id("chiseled_bookshelf");

    @Override
    public @Nullable ItemStack streamData(@NotNull BlockAccessor accessor) {
        int slot = ((ChiseledBookShelfBlock) accessor.getBlock()).getHitSlot(accessor.getHitResult(), accessor.getBlockState()).orElse(-1);
        if (slot == -1) {
            return null;
        }
        return ((ChiseledBookShelfBlockEntity) accessor.getBlockEntity()).getItem(slot);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
        return ItemStack.OPTIONAL_STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return MC_CHISELED_BOOKSHELF;
    }

    @Override
    public int getDefaultPriority() {
        return ItemStorageProvider.getBlock().getDefaultPriority() + 1;
    }
}
