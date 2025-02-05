package dev.gideonwhite1029.horizon.protocol.core;

import io.papermc.paper.ServerBuildInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ProtocolUtils {

    public static String buildProtocolVersion(String protocol) {
        return protocol + "-horizon-" + ServerBuildInfo.buildInfo().asString(ServerBuildInfo.StringRepresentation.VERSION_SIMPLE);
    }

    public static void sendEmptyPayloadPacket(ServerPlayer player, ResourceLocation id) {
        player.connection.send(new ClientboundCustomPayloadPacket(new HorizonProtocolManager.EmptyPayload(id)));
    }

    @SuppressWarnings("all")
    public static void sendPayloadPacket(@NotNull ServerPlayer player, ResourceLocation id, Consumer<FriendlyByteBuf> consumer) {
        player.connection.send(new ClientboundCustomPayloadPacket(new HorizonCustomPayload() {
            @Override
            public void write(@NotNull FriendlyByteBuf buf) {
                consumer.accept(buf);
            }

            @Override
            @NotNull
            public ResourceLocation id() {
                return id;
            }
        }));
    }

    public static void sendPayloadPacket(ServerPlayer player, CustomPacketPayload payload) {
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }
}
