package dev.gideonwhite1029.horizon.protocol;

import dev.gideonwhite1029.horizon.HorizonConfig;
import dev.gideonwhite1029.horizon.HorizonLogger;
import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolHandler;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@HorizonProtocol(namespace = {"xaerominimap", "xaeroworldmap", "journeymap"})
public class MinimapsProtocol {

    private static final HorizonLogger LOGGER = HorizonLogger.LOGGER;

    public static final String PROTOCOL_ID_XAERO_MINI = "xaerominimap";
    public static final String PROTOCOL_ID_XAERO_WORLD = "xaeroworldmap";
    public static final String PROTOCOL_ID_JOURNEY = "journeymap";

    private static final ResourceLocation XAERO_MINIMAP_KEY = idXaeroMini("main");
    private static final ResourceLocation XAERO_WORLDMAP_KEY = idXaeroWorld("main");

    private static final String KICK_MESSAGE = HorizonConfig.blockMinimapsMessage;

    private static final Map<String, String> PLAYERS_WITH_MINIMAPS = new HashMap<>();

    @Contract("_ -> new")
    public static ResourceLocation idXaeroMini(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID_XAERO_MINI, path);
    }

    @Contract("_ -> new")
    public static ResourceLocation idXaeroWorld(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID_XAERO_WORLD, path);
    }

    @Contract("_ -> new")
    public static ResourceLocation idJourneyMap(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID_JOURNEY, path);
    }

    @ProtocolHandler.Init
    public static void init() {
        LOGGER.info("Initializing MiniMaps protocol handler (Xaero's Map, JourneyMap)");
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerJoin(ServerPlayer player) {
        PLAYERS_WITH_MINIMAPS.remove(player.getScoreboardName());
        if (HorizonConfig.xaeroMapEnable && !HorizonConfig.blockXaeromap) {
            sendXaeroServerInfo(player);
        }
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLeave(ServerPlayer player) {
        PLAYERS_WITH_MINIMAPS.remove(player.getScoreboardName());
    }

    @ProtocolHandler.MinecraftRegister(channelId = {"main"})
    public static void onXaeroMinimapRegister(ServerPlayer player, String channelId) {
        LOGGER.info("Player " + player.getScoreboardName() + " has Xaero's Minimap/Worldmap installed");
        PLAYERS_WITH_MINIMAPS.put(player.getScoreboardName(), "Xaero's Map");

        if (HorizonConfig.blockXaeromap) {
            player.connection.disconnect(Component.literal(KICK_MESSAGE), PlayerKickEvent.Cause.KICK_COMMAND);
            LOGGER.info("Player " + player.getScoreboardName() + " was kicked for using Xaero's Minimap/Worldmap");
        }
    }

    @ProtocolHandler.MinecraftRegister(channelId = {"perm_req"})
    public static void onJourneyMapRegister(ServerPlayer player, String channelId) {
        LOGGER.info("Player " + player.getScoreboardName() + " has JourneyMap installed");
        PLAYERS_WITH_MINIMAPS.put(player.getScoreboardName(), "JourneyMap");

        if (HorizonConfig.blockJourneyMap) {
            player.connection.disconnect(Component.literal(KICK_MESSAGE), PlayerKickEvent.Cause.KICK_COMMAND);
            LOGGER.info("Player " + player.getScoreboardName() + " was kicked for using JourneyMap");
        }
    }

    public static void sendXaeroServerInfo(@NotNull ServerPlayer player) {
        if (HorizonConfig.xaeroMapEnable && !HorizonConfig.blockXaeromap) {
            ProtocolUtils.sendPayloadPacket(player, XAERO_MINIMAP_KEY, buf -> {
                buf.writeByte(0);
                buf.writeInt(HorizonConfig.xaeroMapServerID);
            });
            ProtocolUtils.sendPayloadPacket(player, XAERO_WORLDMAP_KEY, buf -> {
                buf.writeByte(0);
                buf.writeInt(HorizonConfig.xaeroMapServerID);
            });
            LOGGER.info("Sent Xaero's Map server ID to player " + player.getScoreboardName());
        }
    }

    public static boolean hasAnyMinimap(ServerPlayer player) {
        return PLAYERS_WITH_MINIMAPS.containsKey(player.getScoreboardName());
    }

    public static String getMinimapType(ServerPlayer player) {
        return PLAYERS_WITH_MINIMAPS.get(player.getScoreboardName());
    }
}