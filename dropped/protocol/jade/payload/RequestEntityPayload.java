package dev.gideonwhite1029.horizon.protocol.jade.payload;

import dev.gideonwhite1029.horizon.protocol.core.HorizonCustomPayload;
import dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessorImpl;
import dev.gideonwhite1029.horizon.protocol.jade.provider.IServerDataProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static dev.gideonwhite1029.horizon.protocol.jade.JadeProtocol.entityDataProviders;

public record RequestEntityPayload(EntityAccessorImpl.SyncData data, List<@Nullable IServerDataProvider<EntityAccessor>> dataProviders) implements HorizonCustomPayload {

    @ID
    private static final ResourceLocation PACKET_REQUEST_ENTITY = JadeProtocol.id("request_entity");

    @Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, RequestEntityPayload> CODEC = StreamCodec.composite(
            EntityAccessorImpl.SyncData.STREAM_CODEC,
            RequestEntityPayload::data,
            ByteBufCodecs.<ByteBuf, IServerDataProvider<EntityAccessor>>list()
                    .apply(ByteBufCodecs.idMapper(
                            $ -> Objects.requireNonNull(entityDataProviders.idMapper()).byId($),
                            $ -> Objects.requireNonNull(entityDataProviders.idMapper()).getIdOrThrow($)
                    )),
            RequestEntityPayload::dataProviders,
            RequestEntityPayload::new);
}