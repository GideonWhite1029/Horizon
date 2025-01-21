package dev.gideonwhite1029.horizon.util;

import ca.spottedleaf.moonrise.common.util.ChunkSystemHooks;
import dev.gideonwhite1029.horizon.HorizonConfig;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ElytraAeronauticsHelper {

    public static void flightBehaviour(Player player, Vec3 velocity) {
        if (HorizonConfig.elytraAeronauticsNoChunk) {
            if ((HorizonConfig.elytraAeronauticsNoChunkSpeed <= 0.0D || velocity.horizontalDistanceSqr() >= HorizonConfig.elytraAeronauticsNoChunkSpeed)
                    && (HorizonConfig.elytraAeronauticsNoChunkHeight <= 0.0D || player.getY() >= HorizonConfig.elytraAeronauticsNoChunkHeight)) {
                if (!player.elytraAeronauticsNoChunk) {
                    player.elytraAeronauticsNoChunk = true;
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    if (HorizonConfig.elytraAeronauticsNoChunkMes) {
                        serverPlayer.sendSystemMessage(Component.literal(HorizonConfig.elytraAeronauticsNoChunkStartMes), true);
                    }
                    ca.spottedleaf.moonrise.common.PlatformHooks.get().removePlayerFromDistanceMaps((ServerLevel) serverPlayer.level(), serverPlayer);
                    ((ServerLevel) serverPlayer.level()).chunkSource.chunkMap.getDistanceManager().removePlayer(serverPlayer.getLastSectionPos(), serverPlayer);
                }
            } else {
                if (player.elytraAeronauticsNoChunk) {
                    player.elytraAeronauticsNoChunk = false;
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    if (HorizonConfig.elytraAeronauticsNoChunkMes) {
                        serverPlayer.sendSystemMessage(Component.literal(HorizonConfig.elytraAeronauticsNoChunkEndMes), true);
                    }
                    ca.spottedleaf.moonrise.common.PlatformHooks.get().addPlayerToDistanceMaps((ServerLevel) serverPlayer.level(), serverPlayer);
                    ((ServerLevel) serverPlayer.level()).chunkSource.chunkMap.getDistanceManager().addPlayer(SectionPos.of(serverPlayer), serverPlayer);
                }
            }
        }
    }
}