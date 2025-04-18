package dev.gideonwhite1029.horizon;

import com.destroystokyo.paper.util.SneakyThrow;
import dev.gideonwhite1029.horizon.commands.GlobalConfigManager;
import dev.gideonwhite1029.horizon.commands.HorizonCommand;
import dev.gideonwhite1029.horizon.config.ConfigVerify;
import dev.gideonwhite1029.horizon.config.GlobalConfig;
import dev.gideonwhite1029.horizon.region.EnumRegionFileExtension;
import dev.gideonwhite1029.horizon.region.HorizonRegionFile;
import dev.gideonwhite1029.horizon.yggdrasil.HorizonMinecraftSessionService;
import io.papermc.paper.configuration.GlobalConfiguration;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public final class HorizonConfig {
    public static final String CONFIG_HEADER = "This is the main configuration file for Horizon.\n"
            + "# \n"
            + "# Created by GideonWhite1029\n"
            + "# Boosty: https://boosty.to/gideonwhite1029\n";
    public static final int CURRENT_CONFIG_VERSION = 5;

    private static File configFile;
    public static YamlConfiguration config;
    private static int configVersion;
    public static boolean createWorldSections = true;

    public static void init(final File file) {
        HorizonConfig.configFile = file;
        config = new YamlConfiguration();
        config.options().setHeader(Collections.singletonList(CONFIG_HEADER));
        config.options().copyDefaults(true);

        if (!file.exists()) {
            try {
                boolean is = file.createNewFile();
                if (!is) {
                    throw new IOException("Can't create file");
                }
            } catch (final Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failure to create horizon config", ex);
            }
        } else {
            try {
                config.load(file);
            } catch (final Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failure to load horizon config", ex);
                SneakyThrow.sneaky(ex);
                throw new RuntimeException(ex);
            }
        }

        HorizonConfig.configVersion = HorizonConfig.config.getInt("config-version", CURRENT_CONFIG_VERSION);
        HorizonConfig.config.set("config-version", CURRENT_CONFIG_VERSION);

        GlobalConfigManager.init();

        registerCommand("horizon", new HorizonCommand("horizon"));
    }

    public static void save() {
        try {
            config.save(HorizonConfig.configFile);
        } catch (final Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to save horizon config", ex);
        }
    }

    public static void registerCommand(String name, Command command) {
        MinecraftServer.getServer().server.getCommandMap().register(name, "horizon", command);
        MinecraftServer.getServer().server.syncCommands();
    }

    public static void unregisterCommand(String name) {
        name = name.toLowerCase(java.util.Locale.ENGLISH).trim();
        MinecraftServer.getServer().server.getCommandMap().getKnownCommands().remove(name);
        MinecraftServer.getServer().server.getCommandMap().getKnownCommands().remove("horizon:" + name);
        MinecraftServer.getServer().server.syncCommands();
    }

    @GlobalConfig(name = "no-chat-reports", category = {"features"})
    public static boolean noChatReports = true;

    // @GlobalConfig(name = "vanilla-hopper", category = {"features"})
    // public static boolean vanillaHopper = false;

    @GlobalConfig(name = "spectator-dont-get-advancement", category = {"features"})
    public static boolean spectatorDontGetAdvancement = false;

    @GlobalConfig(name = "use_virtual_thread_for_async_scheduler", category = {"utils"})
    public static boolean useVirtualThreadForAsyncScheduler = false;

    @GlobalConfig(name = "copper-bulb-1-gt-delay", category = {"features"})
    public static boolean copperBulb1gt = false;

    @GlobalConfig(name = "crafter-1-gt-delay", category = {"features"})
    public static boolean crafter1gt = false;

    @GlobalConfig(name = "disable-packet-limit", category = {"features"})
    public static boolean disablePacketLimit = false;

    @GlobalConfig(name = "fasterChunkSerialization", category = {"optimization"})
    public static boolean fasterChunkSerialization = false;

    @GlobalConfig(name = "disableMovedWronglyThreshold", category = {"features"})
    public static boolean disableMovedWronglyThreshold = false;

    @GlobalConfig(name = "async-player-data-saving", category = {"optimization"})
    public static boolean asyncPlayerDataSaving = false;

    @GlobalConfig(name = "use_virtual_thread_for_chat_executor", category = {"utils"})
    public static boolean useVirtualThreadForChatExecutor = false;

    @GlobalConfig(name = "secure-seed", category = {"features"})
    public static boolean secureSeed = false;

    @GlobalConfig(name = "use_virtual_thread_for_user_authenticator", category = {"utils"})
    public static boolean useVirtualThreadForUserAuthenticator = false;

    @GlobalConfig(name = "tt20-lag-compensation", category = {"optimization", "tt20"})
    public static boolean tt20LagCompensation = false;

    @GlobalConfig(name = "enable-for-water", category = {"optimization", "tt20"})
    public static boolean tt20LagCompensationWater = false;

    @GlobalConfig(name = "enable-for-lava", category = {"optimization", "tt20"})
    public static boolean tt20LagCompensationLava = false;

    @GlobalConfig(name = "no-chunk-load", category = {"features", "elytra-aeronautics"})
    public static boolean elytraAeronauticsNoChunk = false;

    @GlobalConfig(name = "no-chunk-height", category = {"features", "elytra-aeronautics"})
    public static double elytraAeronauticsNoChunkHeight = 500.0D;

    @GlobalConfig(name = "no-chunk-speed", category = {"features", "elytra-aeronautics"})
    public static double elytraAeronauticsNoChunkSpeed = -1.0D;

    @GlobalConfig(name = "message", category = {"features", "elytra-aeronautics"})
    public static boolean elytraAeronauticsNoChunkMes = true;

    @GlobalConfig(name = "message-start", category = {"features", "elytra-aeronautics"})
    public static String elytraAeronauticsNoChunkStartMes = "Flight enter cruise mode";

    @GlobalConfig(name = "message-end", category = {"features", "elytra-aeronautics"})
    public static String elytraAeronauticsNoChunkEndMes = "Flight exit cruise mode";

    // Horizon start - region
    @GlobalConfig(name = "format", category = "region", lock = true, verify = RegionFormatVerify.class)
    public static dev.gideonwhite1029.horizon.region.EnumRegionFileExtension regionFormat = EnumRegionFileExtension.MCA;

    private static class RegionFormatVerify extends ConfigVerify.EnumConfigVerify<EnumRegionFileExtension> {
        @Override
        public String check(EnumRegionFileExtension old, EnumRegionFileExtension value) throws IllegalArgumentException {
            if (value == null) {
                throw new RuntimeException("Invalid region format: " + regionFormat);
            }
            if (regionFormat == EnumRegionFileExtension.LINEAR) {
                HorizonRegionFile.SAVE_DELAY_MS = linearIoFlushDelayMs;
                HorizonRegionFile.SAVE_THREAD_MAX_COUNT = linearIoThreadCount;
                HorizonRegionFile.USE_VIRTUAL_THREAD = linearUseVirtualThread;
            }
            return null;
        }
    }

    @GlobalConfig(name = "compression-level", category = {"region", "linear"}, lock = true, verify = LinearCompressVerify.class)
    public static int linearCompressionLevel = 1;

    private static class LinearCompressVerify extends ConfigVerify.IntConfigVerify {
        @Override
        public String check(Integer old, Integer value) throws IllegalArgumentException {
            if (value < 1 || value > 23) {
                MinecraftServer.LOGGER.error("Linear region compression level should be between 1 and 22 in config: {}", linearCompressionLevel);
                MinecraftServer.LOGGER.error("Falling back to compression level 1.");
                linearCompressionLevel = 1;
            }
            return null;
        }
    }

    @GlobalConfig(name = "io-thread-count", category = {"region", "linear"}, lock = true, verify = ConfigVerify.IntConfigVerify.class)
    public static int linearIoThreadCount = 6;

    @GlobalConfig(name = "io-flush-delay-ms", category = {"region", "linear"}, lock = true, verify = ConfigVerify.IntConfigVerify.class)
    public static int linearIoFlushDelayMs = 100;

    @GlobalConfig(name = "use-virtual-thread", category = {"region", "linear"})
    public static boolean linearUseVirtualThread = true;

    @GlobalConfig(name = "flush-max-threads", category = {"region", "linear"}, lock = true, verify = ConfigVerify.IntConfigVerify.class)
    public static int linearFlushThreads = 1;

    public static int getLinearFlushThreads() {
        if (linearFlushThreads < 0) {
            return Math.max(Runtime.getRuntime().availableProcessors() + linearFlushThreads, 1);
        } else {
            return Math.max(linearFlushThreads, 1);
        }
    }
    // Horizon end - region

    @GlobalConfig(name = "configurable-mc-67", category = {"optimization"})
    public static boolean allowEntityPortalWithPassenger = true;

    @GlobalConfig(name = "horizon-packet-event", category = {"features"})
    public static boolean horizonPacketEvent = false;

    // Horizon start - protocols

    // AppleSkin
    @GlobalConfig(name = "appleskin-enable", category = {"protocols", "appleskin"})
    public static boolean appleskinEnable = false;

    @GlobalConfig(name = "sync-tick-interval", category = {"protocols", "appleskin"})
    public static int syncTickInterval = 20;

    // Jade
    // @GlobalConfig(name = "jade-enable", category = {"protocols", "jade"})
    // public static boolean jadeEnable = false;

    // REI
    // @GlobalConfig(name = "rei-enable", category = {"protocols", "rei"})
    // public static boolean reiEnable = false;

    // XaeroMap
    @GlobalConfig(name = "xaeromap-enable", category = {"protocols", "xaeromap"})
    public static boolean xaeroMapEnable = false;

    @GlobalConfig(name = "xaero-map-server-id", category = {"protocols", "xaeromap"})
    public static int xaeroMapServerID = new Random().nextInt();

    // Carpet
    @GlobalConfig(name = "carpet-enable", category = {"protocols", "carpet"})
    public static boolean carpetEnable = false;

    // Syncmatica
    @GlobalConfig(name = "syncmatica-enable", category = {"protocols", "syncmatica"}, verify = SyncmaticaVerify.class)
    public static boolean syncmaticaProtocol = false;

    @GlobalConfig(name = "quota", category = {"protocols", "syncmatica"})
    public static boolean syncmaticaQuota = false;

    @GlobalConfig(name = "quota-limit", category = {"protocols", "syncmatica"}, verify = ConfigVerify.IntConfigVerify.class)
    public static int syncmaticaQuotaLimit = 40000000;

    public static class SyncmaticaVerify extends ConfigVerify.BooleanConfigVerify {
        @Override
        public String check(Boolean old, Boolean value) {
            if (value) {
                dev.gideonwhite1029.horizon.protocol.syncmatica.SyncmaticaProtocol.init();
            }
            return null;
        }
    }
    // Horizon end - protocols

    @GlobalConfig(name = "extra-yggdrasil-service-enable", category = {"features", "yggdrasil"}, verify = ExtraYggdrasilServiceVerify.class)
    public static boolean extraYggdrasilService = false;

    public static class ExtraYggdrasilServiceVerify extends ConfigVerify.BooleanConfigVerify {
        @Override
        public String check(Boolean old, Boolean value) {
            if (value) {
                HorizonLogger.LOGGER.warning("extra-yggdrasil-service is an unofficial support. Enabling it may cause data security problems!");
                GlobalConfiguration.get().unsupportedSettings.performUsernameValidation = true; // always check username
            }
            return null;
        }
    }

    @GlobalConfig(name = "login-protect", category = {"features", "yggdrasil"})
    public static boolean loginProtect = false;

    @GlobalConfig(name = "urls", category = {"features", "yggdrasil"}, lock = true, verify = ExtraYggdrasilUrlsValidator.class)
    public static List<String> serviceList = List.of("https://url.with.authlib-injector-yggdrasil");

    public static class ExtraYggdrasilUrlsValidator extends ConfigVerify.ListConfigVerify {
        @Override
        public String check(List<?> old, List<?> value) {
            HorizonMinecraftSessionService.initExtraYggdrasilList(serviceList);
            return null;
        }
    }

    @GlobalConfig(name = "async-chunk-send", category = {"optimization"})
    public static boolean AsyncChunkSend = false;

    // Horizon start - AsyncPacketSending
    @GlobalConfig(name = "async-packet-sending", category = {"optimization", "packet-sending"})
    public static boolean asyncPacketSending = false;

    @GlobalConfig(name = "thread-pool-size", category = {"optimization", "packet-sending"})
    public static int threadPoolSize = 4;

    @GlobalConfig(name = "queue-capacity", category = {"optimization", "packet-sending"})
    public static int queueCapacity = 4096;

    @GlobalConfig(name = "prioritize-movement-packets", category = {"optimization", "packet-sending"})
    public static boolean prioritizeMovementPackets = true;

    @GlobalConfig(name = "prioritize-chat-packets", category = {"optimization", "packet-sending"})
    public static boolean prioritizeChatPackets = true;

    @GlobalConfig(name = "spin-wait-for-ready-packets", category = {"optimization", "packet-sending"})
    public static boolean spinWaitForReadyPackets = true;

    @GlobalConfig(name = "spin-time-nanos", category = {"optimization", "packet-sending"})
    public static long spinTimeNanos = 1000;

    @GlobalConfig(name = "batch-processing", category = {"optimization", "packet-sending"})
    public static boolean batchProcessing = true;

    @GlobalConfig(name = "batch-size", category = {"optimization", "packet-sending"})
    public static int batchSize = 128;
    // Horizon end - AsyncPacketSending

    @GlobalConfig(name = "instant-block-updater-reintroduced", category = {"old-minecraft"})
    public static boolean instantBlockUpdaterReintroduced = false;

    @GlobalConfig(name = "redstone-dont-cant-on-trapdoor", category = {"old-minecraft"})
    public static boolean redstoneDontCantOnTrapDoor = false;

    @GlobalConfig(name = "cce-update-suppression", category = {"old-minecraft"})
    public static boolean cceUpdateSuppression = false;

    @GlobalConfig(name = "update-suppression-crash-fix", category = {"old-minecraft"})
    public static boolean updateSuppressionCrashFix = false;

    @GlobalConfig(name = "old-block-entity-behaviour", category = {"old-minecraft"})
    public static boolean oldBlockEntityBehaviour = false;

}
