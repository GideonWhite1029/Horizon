package dev.gideonwhite1029.horizon.protocol.jade.provider;

import dev.gideonwhite1029.horizon.protocol.jade.accessor.Accessor;
import net.minecraft.nbt.CompoundTag;

public interface IServerDataProvider<T extends Accessor<?>> extends IJadeProvider {
    void appendServerData(CompoundTag data, T accessor);
}
