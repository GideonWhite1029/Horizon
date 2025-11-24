package dev.gideonwhite1029.horizon.protocol.jade.provider.entity;

import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.provider.StreamServerDataProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum PetArmorProvider implements StreamServerDataProvider<EntityAccessor, ItemStack> {
    INSTANCE;

    private static final ResourceLocation MC_PET_ARMOR = JadeProtocol.mc_id("pet_armor");

    @Nullable
    @Override
    public ItemStack streamData(@NotNull EntityAccessor accessor) {
        ItemStack armor = ((Mob) accessor.getEntity()).getBodyArmorItem();
        return armor.isEmpty() ? null : armor;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
        return ItemStack.OPTIONAL_STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return MC_PET_ARMOR;
    }
}
