package dev.gideonwhite1029.horizon.protocol.jade.provider.entity;

import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.provider.StreamServerDataProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombieVillager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ZombieVillagerProvider implements StreamServerDataProvider<EntityAccessor, Integer> {
    INSTANCE;

    private static final ResourceLocation MC_ZOMBIE_VILLAGER = JadeProtocol.mc_id("zombie_villager");

    @Override
    public @Nullable Integer streamData(@NotNull EntityAccessor accessor) {
        int time = ((ZombieVillager) accessor.getEntity()).villagerConversionTime;
        return time > 0 ? time : null;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
        return ByteBufCodecs.VAR_INT.cast();
    }

    @Override
    public ResourceLocation getUid() {
        return MC_ZOMBIE_VILLAGER;
    }
}
