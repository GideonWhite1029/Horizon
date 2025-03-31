package dev.gideonwhite1029.horizon.protocol.jade.provider.entity;

import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.provider.StreamServerDataProvider;
import dev.gideonwhite1029.horizon.protocol.jade.util.CommonUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public enum AnimalOwnerProvider implements StreamServerDataProvider<EntityAccessor, String> {
    INSTANCE;

    private static final ResourceLocation MC_ANIMAL_OWNER = JadeProtocol.mc_id("animal_owner");

    @Override
    public String streamData(@NotNull EntityAccessor accessor) {
        return CommonUtil.getLastKnownUsername(getOwnerUUID(accessor.getEntity()));
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, String> streamCodec() {
        return ByteBufCodecs.STRING_UTF8.cast();
    }

    public static UUID getOwnerUUID(Entity entity) {
        if (entity instanceof OwnableEntity ownableEntity) {
            return ownableEntity.getOwnerUUID();
        }
        return null;
    }

    @Override
    public ResourceLocation getUid() {
        return MC_ANIMAL_OWNER;
    }
}