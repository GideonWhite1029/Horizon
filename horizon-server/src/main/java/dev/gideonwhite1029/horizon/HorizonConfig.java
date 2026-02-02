package dev.gideonwhite1029.horizon;

import com.destroystokyo.paper.util.SneakyThrow;
import dev.gideonwhite1029.horizon.commands.GlobalConfigManager;
import dev.gideonwhite1029.horizon.commands.HorizonCommand;
import dev.gideonwhite1029.horizon.config.ConfigVerify;
import dev.gideonwhite1029.horizon.config.GlobalConfig;
import dev.gideonwhite1029.horizon.config.RemovedConfig;
import dev.gideonwhite1029.horizon.protocol.syncmatica.SyncmaticaProtocol;
import dev.gideonwhite1029.horizon.region.EnumRegionFileExtension;
import dev.gideonwhite1029.horizon.region.HorizonRegionFile;
import dev.gideonwhite1029.horizon.util.sentry.SentryManager;
import dev.gideonwhite1029.horizon.yggdrasil.HorizonMinecraftSessionService;
import io.papermc.paper.configuration.GlobalConfiguration;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;

public final class HorizonConfig {
    public static final List<String> CONFIG_HEADER = List.of(
            "This is the main configuration file for Horizon.",
            "",
            "Created by GideonWhite1029",
            "Boosty: https://boosty.to/gideonwhite1029"
    );
    public static final int CURRENT_CONFIG_VERSION = 6;

    private static File configFile;
    public static YamlConfiguration config;
    private static int configVersion;
    public static boolean createWorldSections = true;

    public static void init(final File file) {
        HorizonConfig.configFile = file;
        config = new YamlConfiguration();
        config.options().setHeader(CONFIG_HEADER);
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
        name = name.toLowerCase(Locale.ENGLISH).trim();
        MinecraftServer.getServer().server.getCommandMap().getKnownCommands().remove(name);
        MinecraftServer.getServer().server.getCommandMap().getKnownCommands().remove("horizon:" + name);
        MinecraftServer.getServer().server.syncCommands();
    }

    @GlobalConfig(name = "no-chat-reports", category = {"features"})
    public static boolean noChatReports = true;

    @GlobalConfig(name = "vanilla-hopper", category = {"features"})
    public static boolean vanillaHopper = false;

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

    // @GlobalConfig(name = "fasterChunkSerialization", category = {"optimization"})
    // public static boolean fasterChunkSerialization = false;

    @RemovedConfig(name = "fasterChunkSerialization", category = {"optimization"})

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
    public static EnumRegionFileExtension regionFormat = EnumRegionFileExtension.MCA;

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

    @RemovedConfig(name = "jade-enable", category = {"protocols", "jade"})

    // REI
    @GlobalConfig(name = "rei-enable", category = {"protocols", "rei"})
    public static boolean reiEnable = false;

    @GlobalConfig(name = "maps-block-message", category = {"protocols"})
    public static String blockMinimapsMessage = "On the server banned these mods (Xaero's Minimap/Xaero's World Map/JourneyMap)!";

    // XaeroMap
    @GlobalConfig(name = "xaeromap-enable", category = {"protocols", "xaeromap"})
    public static boolean xaeroMapEnable = false;

    @GlobalConfig(name = "xaeromap-block-enable", category = {"protocols", "xaeromap"})
    public static boolean blockXaeromap = false;

    @GlobalConfig(name = "xaero-map-server-id", category = {"protocols", "xaeromap"})
    public static int xaeroMapServerID = new Random().nextInt();

    // JourneyMap
    @GlobalConfig(name = "journeymap-block-enable", category = {"protocols", "journeymap"})
    public static boolean blockJourneyMap = false;

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
                SyncmaticaProtocol.init(true);
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

    @GlobalConfig(name = "sentry-dsn", category = {"features", "sentry"}, verify = SentryDsnVerify.class)
    public static String sentryDsn = "";

    @GlobalConfig(name = "sentry-only-log-thrown", category = {"features", "sentry"})
    public static boolean onlyLogThrown = false;

    public static class SentryDsnVerify extends ConfigVerify.StringConfigVerify {
        @Override
        public String check(String old, String value) {
            String sentryEnvironment = System.getenv("SENTRY_DSN");
            String finalDsn = sentryEnvironment != null ? sentryEnvironment : value;
            sentryDsn = finalDsn;
            if (finalDsn != null && !finalDsn.isBlank()) {
                try {
                    SentryManager.init(org.apache.logging.log4j.Level.WARN);
                    HorizonLogger.LOGGER.info("Sentry initialized with DSN");
                } catch (Exception ex) {
                    HorizonLogger.LOGGER.warning("Failed to initialize Sentry", ex);
                    return "Failed to initialize Sentry: " + ex.getMessage();
                }
            } else {
                HorizonLogger.LOGGER.info("Sentry disabled (no DSN provided)");
            }
            return null;
        }
    }

    @GlobalConfig(name = "enable", category = {"security", "obfuscation-detection"})
    public static boolean obfuscationDetectionEnabled = true;

    @GlobalConfig(name = "suspicious-name-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double suspiciousNameThreshold = 0.30;

    @GlobalConfig(name = "short-name-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double shortNameThreshold = 0.20;

    @GlobalConfig(name = "advanced-obfuscation-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double advancedObfuscationThreshold = 0.10;

    @GlobalConfig(name = "encrypted-string-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double encryptedStringThreshold = 0.15;

    @GlobalConfig(name = "anti-debug-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double antiDebugThreshold = 0.05;

    @GlobalConfig(name = "access-obfuscation-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double accessObfuscationThreshold = 0.25;

    @GlobalConfig(name = "arithmetic-obfuscation-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double arithmeticObfuscationThreshold = 0.20;

    @GlobalConfig(name = "crypto-usage-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double cryptoUsageThreshold = 0.10;

    @GlobalConfig(name = "small-plugin-short-name-threshold", category = {"security", "obfuscation-detection"}, verify = ThresholdVerify.class)
    public static double smallPluginShortNameThreshold = 0.40;

    @GlobalConfig(name = "minimum-classes-for-large-plugin", category = {"security", "obfuscation-detection"}, verify = ConfigVerify.IntConfigVerify.class)
    public static int minimumClassesForLargePlugin = 10;

    public static class ThresholdVerify extends ConfigVerify.DoubleConfigVerify {
        @Override
        public String check(Double old, Double value) {
            if (value < 0.0 || value > 1.0) {
                HorizonLogger.LOGGER.warning("Obfuscation detection threshold must be between 0.0 and 1.0. Falling back to default.");
                return "Threshold must be between 0.0 and 1.0";
            }
            return null;
        }
    }

}
