package dev.gideonwhite1029.horizon.protocol.jade.provider;

import dev.gideonwhite1029.horizon.protocol.jade.accessor.Accessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public interface StreamServerDataProvider<T extends Accessor<?>, D> extends IServerDataProvider<T> {

    @Override
    default void appendServerData(CompoundTag data, T accessor) {
        D value = streamData(accessor);
        if (value != null) {
            data.put(getUid().toString(), accessor.encodeAsNbt(streamCodec(), value));
        }
    }

    @Nullable
    D streamData(T accessor);

    StreamCodec<RegistryFriendlyByteBuf, D> streamCodec();
}
