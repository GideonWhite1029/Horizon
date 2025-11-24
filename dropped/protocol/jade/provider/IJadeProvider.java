package dev.gideonwhite1029.horizon.protocol.jade.provider;

import net.minecraft.resources.ResourceLocation;

public interface IJadeProvider {

    ResourceLocation getUid();

    default int getDefaultPriority() {
        return 0;
    }
}
