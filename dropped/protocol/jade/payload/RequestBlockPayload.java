package dev.gideonwhite1029.horizon.protocol.jade.payload;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.BlockAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.BlockAccessorImpl;
import dev.gideonwhite1029.horizon.protocol.jade.provider.IServerDataProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol.blockDataProviders;

public record RequestBlockPayload(BlockAccessorImpl.SyncData data, List<@Nullable IServerDataProvider<BlockAccessor>> dataProviders) implements HorizonCustomPayload {

    @ID
    private static final ResourceLocation PACKET_REQUEST_BLOCK = JadeProtocol.id("request_block");

    @Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, RequestBlockPayload> CODEC = StreamCodec.composite(
            BlockAccessorImpl.SyncData.STREAM_CODEC,
            RequestBlockPayload::data,
            ByteBufCodecs.<ByteBuf, IServerDataProvider<BlockAccessor>>list()
                    .apply(ByteBufCodecs.idMapper(
                            $ -> Objects.requireNonNull(blockDataProviders.idMapper()).byId($),
                            $ -> Objects.requireNonNull(blockDataProviders.idMapper()).getIdOrThrow($))),
            RequestBlockPayload::dataProviders,
            RequestBlockPayload::new);
}