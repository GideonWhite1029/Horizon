package dev.gideonwhite1029.horizon.protocol.core;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;

public record Context(@NotNull GameProfile profile, Connection connection) {

}
