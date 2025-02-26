package dev.gideonwhite1029.horizon.protocol;

import dev.gideonwhite1029.horizon.HorizonConfig;
import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@HorizonProtocol(namespace = {"xaerominimap", "xaeroworldmap"})
public class XaeroMapProtocol {

    public static final String PROTOCOL_ID_MINI = "xaerominimap";
    public static final String PROTOCOL_ID_WORLD = "xaeroworldmap";

    private static final ResourceLocation MINIMAP_KEY = idMini("main");
    private static final ResourceLocation WORLDMAP_KEY = idWorld("main");

    @Contract("_ -> new")
    public static ResourceLocation idMini(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID_MINI, path);
    }

    @Contract("_ -> new")
    public static ResourceLocation idWorld(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID_WORLD, path);
    }

    public static void onSendWorldInfo(@NotNull ServerPlayer player) {
        if (HorizonConfig.xaeroMapEnable) {
            ProtocolUtils.sendPayloadPacket(player, MINIMAP_KEY, buf -> {
                buf.writeByte(0);
                buf.writeInt(HorizonConfig.xaeroMapServerID);
            });
            ProtocolUtils.sendPayloadPacket(player, WORLDMAP_KEY, buf -> {
                buf.writeByte(0);
                buf.writeInt(HorizonConfig.xaeroMapServerID);
            });
        }
    }
}
