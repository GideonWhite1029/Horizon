package dev.gideonwhite1029.horizon.protocol.jade;

import dev.gideonwhite1029.horizon.HorizonConfig;
import dev.gideonwhite1029.horizon.HorizonLogger;
import dev.gideonwhite1029.horizon.protocol.core.HorizonProtocol;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolHandler;
import dev.gideonwhite1029.horizon.protocol.core.ProtocolUtils;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.BlockAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.accessor.EntityAccessor;
import dev.gideonwhite1029.horizon.protocol.jade.payload.*;
import dev.gideonwhite1029.horizon.protocol.jade.provider.*;
import dev.gideonwhite1029.horizon.protocol.jade.provider.block.*;
import dev.gideonwhite1029.horizon.protocol.jade.provider.entity.*;
import dev.gideonwhite1029.horizon.protocol.jade.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@HorizonProtocol(namespace = "jade")
public class JadeProtocol {

    public static PriorityStore<ResourceLocation, IJadeProvider> priorities;
    private static List<Block> shearableBlocks = null;

    public static final String PROTOCOL_ID = "jade";
    public static final String PROTOCOL_VERSION = "7";

    public static final HierarchyLookup<IServerDataProvider<EntityAccessor>> entityDataProviders = new HierarchyLookup<>(Entity.class);
    public static final PairHierarchyLookup<IServerDataProvider<BlockAccessor>> blockDataProviders = new PairHierarchyLookup<>(new HierarchyLookup<>(Block.class), new HierarchyLookup<>(BlockEntity.class));
    public static final WrappedHierarchyLookup<IServerExtensionProvider<ItemStack>> itemStorageProviders = WrappedHierarchyLookup.forAccessor();
    private static final Set<ServerPlayer> enabledPlayers = new HashSet<>();

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation mc_id(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    @ProtocolHandler.Init
    public static void init() {
        priorities = new PriorityStore<>(IJadeProvider::getDefaultPriority, IJadeProvider::getUid);

        // core plugin
        blockDataProviders.register(BlockEntity.class, ObjectNameProvider.ForBlock.INSTANCE);

        // universal plugin
        entityDataProviders.register(Entity.class, ItemStorageProvider.getEntity());
        blockDataProviders.register(Block.class, ItemStorageProvider.getBlock());

        itemStorageProviders.register(Object.class, ItemStorageExtensionProvider.INSTANCE);
        itemStorageProviders.register(Block.class, ItemStorageExtensionProvider.INSTANCE);

        // vanilla plugin
        entityDataProviders.register(Entity.class, AnimalOwnerProvider.INSTANCE);
        entityDataProviders.register(LivingEntity.class, StatusEffectsProvider.INSTANCE);
        entityDataProviders.register(AgeableMob.class, MobGrowthProvider.INSTANCE);
        entityDataProviders.register(Tadpole.class, MobGrowthProvider.INSTANCE);
        entityDataProviders.register(Animal.class, MobBreedingProvider.INSTANCE);
        entityDataProviders.register(Allay.class, MobBreedingProvider.INSTANCE);
        entityDataProviders.register(Mob.class, PetArmorProvider.INSTANCE);

        entityDataProviders.register(Chicken.class, NextEntityDropProvider.INSTANCE);
        entityDataProviders.register(Armadillo.class, NextEntityDropProvider.INSTANCE);

        entityDataProviders.register(ZombieVillager.class, ZombieVillagerProvider.INSTANCE);

        blockDataProviders.register(BrewingStandBlockEntity.class, BrewingStandProvider.INSTANCE);
        blockDataProviders.register(BeehiveBlockEntity.class, BeehiveProvider.INSTANCE);
        blockDataProviders.register(CommandBlockEntity.class, CommandBlockProvider.INSTANCE);
        blockDataProviders.register(JukeboxBlockEntity.class, JukeboxProvider.INSTANCE);
        blockDataProviders.register(LecternBlockEntity.class, LecternProvider.INSTANCE);

        blockDataProviders.register(ComparatorBlockEntity.class, RedstoneProvider.INSTANCE);
        blockDataProviders.register(HopperBlockEntity.class, HopperLockProvider.INSTANCE);
        blockDataProviders.register(CalibratedSculkSensorBlockEntity.class, RedstoneProvider.INSTANCE);

        blockDataProviders.register(AbstractFurnaceBlockEntity.class, FurnaceProvider.INSTANCE);
        blockDataProviders.register(ChiseledBookShelfBlockEntity.class, ChiseledBookshelfProvider.INSTANCE);
        blockDataProviders.register(TrialSpawnerBlockEntity.class, MobSpawnerCooldownProvider.INSTANCE);

        blockDataProviders.idMapped();
        entityDataProviders.idMapped();
        itemStorageProviders.register(CampfireBlock.class, CampfireProvider.INSTANCE);

        blockDataProviders.loadComplete(priorities);
        entityDataProviders.loadComplete(priorities);
        itemStorageProviders.loadComplete(priorities);

        rebuildShearableBlocks();
    }

    @ProtocolHandler.PayloadReceiver(payload = ClientHandshakePayload.class, payloadId = "client_handshake")
    public static void clientHandshake(ServerPlayer player, ClientHandshakePayload payload) {
        if (!HorizonConfig.jadeEnable) {
            return;
        }

        if (!payload.protocolVersion().equals(PROTOCOL_VERSION)) {
            player.sendSystemMessage(Component.literal("You are using a different version of Jade than the server. Please update Jade or report to the server operator").withColor(0xff0000));
            return;
        }
        ProtocolUtils.sendPayloadPacket(player, new ServerHandshakePayload(Collections.emptyMap(), shearableBlocks, blockDataProviders.mappedIds(), entityDataProviders.mappedIds()));
        enabledPlayers.add(player);
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLeave(ServerPlayer player) {
        enabledPlayers.remove(player);
    }

    @ProtocolHandler.PayloadReceiver(payload = RequestEntityPayload.class, payloadId = "request_entity")
    public static void requestEntityData(ServerPlayer player, RequestEntityPayload payload) {
        if (!HorizonConfig.jadeEnable) {
            return;
        }

        MinecraftServer.getServer().execute(() -> {
            EntityAccessor accessor = payload.data().unpack(player);
            if (accessor == null) {
                return;
            }

            Entity entity = accessor.getEntity();
            double maxDistance = Mth.square(player.entityInteractionRange() + 21);
            if (entity == null || player.distanceToSqr(entity) > maxDistance) {
                return;
            }

            List<IServerDataProvider<EntityAccessor>> providers = entityDataProviders.get(entity);
            if (providers.isEmpty()) {
                return;
            }

            CompoundTag tag = new CompoundTag();
            for (IServerDataProvider<EntityAccessor> provider : providers) {
                if (!payload.dataProviders().contains(provider)) {
                    continue;
                }
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    HorizonLogger.LOGGER.warning("Error while saving data for entity " + entity);
                }
            }
            tag.putInt("EntityId", entity.getId());

            ProtocolUtils.sendPayloadPacket(player, new ReceiveDataPayload(tag));
        });
    }

    @ProtocolHandler.PayloadReceiver(payload = RequestBlockPayload.class, payloadId = "request_block")
    public static void requestBlockData(ServerPlayer player, RequestBlockPayload payload) {
        if (!HorizonConfig.jadeEnable) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        server.execute(() -> {
            BlockAccessor accessor = payload.data().unpack(player);
            if (accessor == null) {
                return;
            }

            BlockPos pos = accessor.getPosition();
            Block block = accessor.getBlock();
            BlockEntity blockEntity = accessor.getBlockEntity();
            double maxDistance = Mth.square(player.blockInteractionRange() + 21);
            if (pos.distSqr(player.blockPosition()) > maxDistance || !accessor.getLevel().isLoaded(pos)) {
                return;
            }

            List<IServerDataProvider<BlockAccessor>> providers;
            if (blockEntity != null) {
                providers = blockDataProviders.getMerged(block, blockEntity);
            } else {
                providers = blockDataProviders.first.get(block);
            }

            if (providers.isEmpty()) {
                return;
            }

            CompoundTag tag = new CompoundTag();
            for (IServerDataProvider<BlockAccessor> provider : providers) {
                if (!payload.dataProviders().contains(provider)) {
                    continue;
                }
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    HorizonLogger.LOGGER.warning("Error while saving data for block " + accessor.getBlockState());
                }
            }
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putString("BlockId", BuiltInRegistries.BLOCK.getKey(block).toString());

            ProtocolUtils.sendPayloadPacket(player, new ReceiveDataPayload(tag));
        });
    }

    @ProtocolHandler.ReloadServer
    public static void onServerReload() {
        if (HorizonConfig.jadeEnable) {
            rebuildShearableBlocks();
            for (ServerPlayer player : enabledPlayers) {
                ProtocolUtils.sendPayloadPacket(player, new ServerHandshakePayload(Collections.emptyMap(), shearableBlocks, blockDataProviders.mappedIds(), entityDataProviders.mappedIds()));
            }
        }
    }

    private static void rebuildShearableBlocks() {
        try {
            shearableBlocks = Collections.unmodifiableList(LootTableMineableCollector.execute(
                    MinecraftServer.getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE),
                    Items.SHEARS.getDefaultInstance()
            ));
        } catch (Throwable ignore) {
            shearableBlocks = List.of();
            HorizonLogger.LOGGER.severe("Failed to collect shearable blocks");
        }
    }
}