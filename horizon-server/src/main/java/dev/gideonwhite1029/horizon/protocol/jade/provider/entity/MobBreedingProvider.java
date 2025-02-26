package dev.gideonwhite1029.horizon.protocol.jade.provider.entity;

import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.provider.StreamServerDataProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MobBreedingProvider implements StreamServerDataProvider<EntityAccessor, Integer> {
    INSTANCE;

    private static final ResourceLocation MC_MOB_BREEDING = JadeProtocol.mc_id("mob_breeding");

    @Override
    public @Nullable Integer streamData(@NotNull EntityAccessor accessor) {
        int time = 0;
        Entity entity = accessor.getEntity();
        if (entity instanceof Allay allay) {
            if (allay.duplicationCooldown > 0 && allay.duplicationCooldown < Integer.MAX_VALUE) {
                time = (int) allay.duplicationCooldown;
            }
        } else {
            time = ((Animal) entity).getAge();
        }
        return time > 0 ? time : null;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
        return ByteBufCodecs.VAR_INT.cast();
    }

    @Override
    public ResourceLocation getUid() {
        return MC_MOB_BREEDING;
    }
}
