package dev.gideonwhite1029.horizon.protocol.jade.provider.entity;

import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.provider.StreamServerDataProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MobGrowthProvider implements StreamServerDataProvider<EntityAccessor, Integer> {
    INSTANCE;

    private static final ResourceLocation MC_MOB_GROWTH = JadeProtocol.mc_id("mob_growth");

    @Override
    public @Nullable Integer streamData(@NotNull EntityAccessor accessor) {
        int time = -1;
        Entity entity = accessor.getEntity();
        if (entity instanceof AgeableMob ageable) {
            time = -ageable.getAge();
        } else if (entity instanceof Tadpole tadpole) {
            time = tadpole.getTicksLeftUntilAdult();
        }
        return time > 0 ? time : null;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
        return ByteBufCodecs.VAR_INT.cast();
    }


    @Override
    public ResourceLocation getUid() {
        return MC_MOB_GROWTH;
    }
}
