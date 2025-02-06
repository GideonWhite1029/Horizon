package dev.gideonwhite1029.horizon;

import com.destroystokyo.paper.util.SneakyThrow;
import dev.gideonwhite1029.horizon.commands.GlobalConfigManager;
import dev.gideonwhite1029.horizon.commands.HorizonCommand;
import dev.gideonwhite1029.horizon.config.ConfigVerify;
import dev.gideonwhite1029.horizon.config.GlobalConfig;
import dev.gideonwhite1029.horizon.region.EnumRegionFileExtension;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

public final class HorizonConfig {
    public static final String CONFIG_HEADER = "This is the main configuration file for Horizon.\n"
            + "# \n"
            + "# Created by GideonWhite1029\n"
            + "# Boosty: https://boosty.to/gideonwhite1029\n";
    public static final int CURRENT_CONFIG_VERSION = 4;

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

    private static class RegionFormatVerify extends ConfigVerify.EnumConfigVerify<dev.gideonwhite1029.horizon.region.EnumRegionFileExtension> {
    }

    @GlobalConfig(name = "flush-frequency", category = {"region", "linear"}, lock = true, verify = ConfigVerify.IntConfigVerify.class)
    public static int linearFlushFrequency = 10;

    @GlobalConfig(name = "throw-on-unknown-extension", category = {"region", "linear"})
    public static boolean throwOnUnknownExtension = true;

    @GlobalConfig(name = "flush-max-threads", category = {"region", "linear"}, lock = true, verify = ConfigVerify.IntConfigVerify.class)
    public static int linearFlushThreads = 1;

    public static int getLinearFlushThreads() {
        if (linearFlushThreads < 0) {
            return Math.max(Runtime.getRuntime().availableProcessors() + linearFlushThreads, 1);
        } else {
            return Math.max(linearFlushThreads, 1);
        }
    }

    @GlobalConfig(name = "compression-level", category = {"region", "linear"}, lock = true, verify = LinearCompressVerify.class)
    public static int linearCompressionLevel = 1;

    private static class LinearCompressVerify extends ConfigVerify.IntConfigVerify {
        @Override
        public String check(Integer old, Integer value) throws IllegalArgumentException {
            if (value < 1 || value > 22) {
                throw new IllegalArgumentException("linear.compression-level need between 1 and 22");
            }
            return null;
        }
    }
    // Horizon end - region

    @GlobalConfig(name = "configurable-mc-67", category = {"optimization"})
    public static boolean allowEntityPortalWithPassenger = true;

    @GlobalConfig(name = "horizon-packet-event", category = {"features"})
    public static boolean horizonPacketEvent = false;

}
