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
    public static final int CURRENT_CONFIG_VERSION = 7;

    private static File configFile;
    public static YamlConfiguration config;
    private static int configVersion;
    public static boolean createWorldSections = true;

    public static void init(final File file) {
        HorizonConfig.configFile = file;
        config = new YamlConfiguration();
        config.options().setHeader(CONFIG_HEADER);
        config.options().copyDefaults(true);
        config.options().parseComments(true);

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

    @GlobalConfig(
        name = "no-chat-reports",
        category = {"features"},
        comment = {
            "Disables the Mojang chat reporting system.",
            "When enabled, players' chat messages are not forwarded to Mojang's servers for moderation.",
            "This prevents players from being able to report each other's messages.",
            "Recommended: true for private/community servers that handle moderation themselves."
        }
    )
    public static boolean noChatReports = true;

    @GlobalConfig(
        name = "vanilla-hopper",
        category = {"features"},
        comment = {
            "Use the vanilla hopper behavior instead of Paper's optimized implementation.",
            "Paper's hopper optimization reduces tick lag but can cause subtle differences",
            "in hopper timing compared to vanilla Minecraft.",
            "Enable this only if you rely on exact vanilla hopper mechanics (e.g. for farms)."
        }
    )
    public static boolean vanillaHopper = false;

    @GlobalConfig(
        name = "spectator-dont-get-advancement",
        category = {"features"},
        comment = {
            "Prevents players in spectator mode from triggering advancements.",
            "By default, spectators can still complete advancement criteria when flying through the world.",
            "Enable this to make spectator mode truly passive with respect to progression."
        }
    )
    public static boolean spectatorDontGetAdvancement = false;

    @GlobalConfig(
        name = "use_virtual_thread_for_async_scheduler",
        category = {"utils"},
        comment = {
            "Use Java virtual threads for the Bukkit async scheduler.",
            "Virtual threads are lightweight and can improve throughput for I/O-heavy async tasks.",
            "Disable if you encounter compatibility issues with plugins using the async scheduler."
        }
    )
    public static boolean useVirtualThreadForAsyncScheduler = false;

    @GlobalConfig(
        name = "copper-bulb-1-gt-delay",
        category = {"features"},
        comment = {
            "Restores the 1-game-tick delay for copper bulbs, matching Java Edition behavior.",
            "In vanilla Minecraft (Java Edition), copper bulbs have an inherent 1gt delay when toggling.",
            "Paper removed this delay. Enable to restore parity with vanilla redstone behavior."
        }
    )
    public static boolean copperBulb1gt = false;

    @GlobalConfig(
        name = "crafter-1-gt-delay",
        category = {"features"},
        comment = {
            "Restores the 1-game-tick delay for crafters (auto-crafting blocks), matching Java Edition behavior.",
            "Paper removed this delay as an optimization. Enable to restore vanilla crafter timing,",
            "which is important for redstone contraptions that depend on exact crafter pulse timing."
        }
    )
    public static boolean crafter1gt = false;

    @GlobalConfig(
        name = "disable-packet-limit",
        category = {"features"},
        comment = {
            "Disables the incoming packet rate limit for players.",
            "By default, Paper kicks players that send too many packets per second.",
            "Enable this on servers with high-latency players or if plugins send many packets on behalf of clients.",
            "Warning: disabling this can make the server more vulnerable to packet-flood attacks."
        }
    )
    public static boolean disablePacketLimit = false;

    // @GlobalConfig(name = "fasterChunkSerialization", category = {"optimization"})
    // public static boolean fasterChunkSerialization = false;

    @RemovedConfig(name = "fasterChunkSerialization", category = {"optimization"})

    @GlobalConfig(
        name = "disableMovedWronglyThreshold",
        category = {"features"},
        comment = {
            "Disables the 'moved wrongly' player movement check.",
            "Vanilla and Paper reject player position updates that deviate too far from the server's expected position.",
            "Disabling this threshold can help on servers with rubberbanding issues or custom movement mechanics,",
            "but may allow some movement-based cheats to go undetected."
        }
    )
    public static boolean disableMovedWronglyThreshold = false;

    @GlobalConfig(
        name = "async-player-data-saving",
        category = {"optimization"},
        comment = {
            "Saves player data (inventory, location, stats) asynchronously on the I/O thread.",
            "This reduces main thread stalls caused by player data writes, especially with many players.",
            "Warning: if the server crashes mid-save there is a small risk of data loss.",
            "Recommended: true for servers with 50+ concurrent players."
        }
    )
    public static boolean asyncPlayerDataSaving = false;

    @GlobalConfig(
        name = "use_virtual_thread_for_chat_executor",
        category = {"utils"},
        comment = {
            "Use Java virtual threads for the chat message executor.",
            "The chat executor processes and dispatches player chat messages.",
            "Virtual threads reduce overhead per chat message under high chat volume."
        }
    )
    public static boolean useVirtualThreadForChatExecutor = false;

    @GlobalConfig(
        name = "secure-seed",
        category = {"features"},
        comment = {
            "Hides the world seed from players and clients.",
            "When enabled, the /seed command returns a fake random seed and seed-revealing exploits are blocked.",
            "Useful for servers where the world seed should remain secret (e.g. competitive or adventure maps)."
        }
    )
    public static boolean secureSeed = false;

    @GlobalConfig(
        name = "use_virtual_thread_for_user_authenticator",
        category = {"utils"},
        comment = {
            "Use Java virtual threads for the user authentication service.",
            "Authentication involves network I/O to Mojang's session servers, making it a good candidate",
            "for virtual threads which park cheaply during blocking operations."
        }
    )
    public static boolean useVirtualThreadForUserAuthenticator = false;

    @GlobalConfig(
        name = "tt20-lag-compensation",
        category = {"optimization", "tt20"},
        comment = {
            "Enables TT20 (tick time 20ms) lag compensation for fluid and block updates.",
            "When the server TPS drops below 20, some game mechanics (fluids, redstone) slow down proportionally.",
            "This feature compensates by running extra ticks for those mechanics to maintain normal behavior.",
            "Enable sub-options (enable-for-water, enable-for-lava) to activate compensation per fluid type."
        }
    )
    public static boolean tt20LagCompensation = false;

    @GlobalConfig(
        name = "enable-for-water",
        category = {"optimization", "tt20"},
        comment = {
            "Apply TT20 lag compensation to water flow.",
            "When the server lags, water will still spread at a normal rate relative to real time.",
            "Requires tt20-lag-compensation to be enabled."
        }
    )
    public static boolean tt20LagCompensationWater = false;

    @GlobalConfig(
        name = "enable-for-lava",
        category = {"optimization", "tt20"},
        comment = {
            "Apply TT20 lag compensation to lava flow.",
            "When the server lags, lava will still spread at a normal rate relative to real time.",
            "Requires tt20-lag-compensation to be enabled."
        }
    )
    public static boolean tt20LagCompensationLava = false;

    @GlobalConfig(
        name = "no-chunk-load",
        category = {"features", "elytra-aeronautics"},
        comment = {
            "Enables the Elytra Aeronautics feature that suppresses chunk loading while flying at high altitude.",
            "Players flying above the configured height threshold will stop causing new chunks to load,",
            "significantly reducing server load during long elytra flights.",
            "Use no-chunk-height and no-chunk-speed to fine-tune when cruise mode activates."
        }
    )
    public static boolean elytraAeronauticsNoChunk = false;

    @GlobalConfig(
        name = "no-chunk-height",
        category = {"features", "elytra-aeronautics"},
        verify = ConfigVerify.DoubleConfigVerify.class,
        comment = {
            "Minimum altitude (Y coordinate) at which Elytra Aeronautics cruise mode can activate.",
            "Players must be flying above this height for chunk loading suppression to apply.",
            "Default: 500.0. Set higher to limit the feature to very high-altitude flight only."
        }
    )
    public static double elytraAeronauticsNoChunkHeight = 500.0D;

    @GlobalConfig(
        name = "no-chunk-speed",
        category = {"features", "elytra-aeronautics"},
        verify = ConfigVerify.DoubleConfigVerify.class,
        comment = {
            "Minimum horizontal speed (blocks/tick) required for Elytra Aeronautics cruise mode to activate.",
            "Set to -1.0 to disable the speed check and rely solely on altitude.",
            "Setting a positive value ensures only fast-moving players suppress chunk loading."
        }
    )
    public static double elytraAeronauticsNoChunkSpeed = -1.0D;

    @GlobalConfig(
        name = "message",
        category = {"features", "elytra-aeronautics"},
        comment = {
            "Whether to send a chat message to the player when Elytra Aeronautics cruise mode starts or ends.",
            "The message text is configured via message-start and message-end."
        }
    )
    public static boolean elytraAeronauticsNoChunkMes = true;

    @GlobalConfig(
        name = "message-start",
        category = {"features", "elytra-aeronautics"},
        verify = ConfigVerify.StringConfigVerify.class,
        comment = {
            "Message shown to the player when they enter Elytra Aeronautics cruise mode.",
            "Cruise mode suppresses chunk loading for high-altitude, high-speed elytra flight.",
            "Requires the 'message' option to be true."
        }
    )
    public static String elytraAeronauticsNoChunkStartMes = "Flight enter cruise mode";

    @GlobalConfig(
        name = "message-end",
        category = {"features", "elytra-aeronautics"},
        verify = ConfigVerify.StringConfigVerify.class,
        comment = {
            "Message shown to the player when they exit Elytra Aeronautics cruise mode.",
            "Cruise mode ends when the player descends below the height threshold or slows down.",
            "Requires the 'message' option to be true."
        }
    )
    public static String elytraAeronauticsNoChunkEndMes = "Flight exit cruise mode";

    // Horizon start - region
    @GlobalConfig(
        name = "format",
        category = "region",
        lock = true,
        verify = RegionFormatVerify.class,
        comment = {
            "Region file format used to store world chunk data.",
            "MCA      - Standard Minecraft Anvil format (.mca). Compatible with all tools and editors.",
            "LINEAR   - Custom linear format (.linear). Uses ZSTD/LZ4 compression per chunk,",
            "           resulting in significantly smaller world files (typically 30-60% size reduction).",
            "BUFFERED - Buffered sector format (.b_linear). Append-only NIO writes with ZSTD compression",
            "           and XXHash32 per-chunk integrity checks. Auto-compacts on close and periodically.",
            "WARNING: Changing this value after world creation requires a full region file conversion.",
            "This setting is locked after the first server start."
        }
    )
    public static EnumRegionFileExtension regionFormat = EnumRegionFileExtension.MCA;

    private static class RegionFormatVerify extends ConfigVerify.EnumConfigVerify<EnumRegionFileExtension> {
        @Override
        public String check(EnumRegionFileExtension old, EnumRegionFileExtension value) throws IllegalArgumentException {
            if (value == null) {
                throw new RuntimeException("Invalid region format: " + regionFormat);
            }
            return null;
        }
    }

    @GlobalConfig(
        name = "compression-level",
        category = {"region", "linear"},
        lock = true,
        verify = LinearCompressVerify.class,
        comment = {
            "ZSTD compression level used when writing Linear region files.",
            "Valid range: 1 to 22. Higher values produce smaller files but require more CPU during writes.",
            "Level 1 (default) gives the best speed/size balance for real-time chunk saving.",
            "Levels 3-6 are a good compromise. Levels 15+ are very slow and only useful for archiving.",
            "This setting is locked after the first server start."
        }
    )
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

    @GlobalConfig(
        name = "io-thread-count",
        category = {"region", "linear"},
        lock = true,
        verify = ConfigVerify.IntConfigVerify.class,
        comment = {
            "Number of worker threads in the Linear I/O thread pool.",
            "These threads perform the actual disk writes for Linear region files.",
            "Recommended: set to the number of physical disk spindles, or 2-4 for SSDs.",
            "Higher values improve throughput on fast storage but add overhead on HDDs.",
            "This setting is locked after the first server start."
        }
    )
    public static int linearIoThreadCount = 6;

    @GlobalConfig(
        name = "io-flush-delay-ms",
        category = {"region", "linear"},
        lock = true,
        verify = ConfigVerify.IntConfigVerify.class,
        comment = {
            "Interval in milliseconds between flush checks by the Linear region flusher.",
            "The flusher periodically scans all open region files and writes any pending dirty data to disk.",
            "Lower values reduce the window for data loss on crash but increase I/O activity.",
            "Default: 100ms. Increase to 500-1000ms on HDDs to batch writes more aggressively.",
            "This setting is locked after the first server start."
        }
    )
    public static int linearIoFlushDelayMs = 100;

    @GlobalConfig(
        name = "use-virtual-thread",
        category = {"region", "linear"},
        comment = {
            "Legacy option — has no effect with the current centralized flusher implementation.",
            "Kept for configuration compatibility. May be repurposed or removed in a future version."
        }
    )
    public static boolean linearUseVirtualThread = true;

    @GlobalConfig(
        name = "flush-max-threads",
        category = {"region", "linear"},
        lock = true,
        verify = ConfigVerify.IntConfigVerify.class,
        comment = {
            "Maximum number of threads used concurrently to flush (write) Linear region files.",
            "Negative values are relative to the available processor count: e.g. -2 means (cpus - 2).",
            "0 or 1 means single-threaded flushing.",
            "Increasing this can improve flush throughput on multi-core systems with fast storage.",
            "This setting is locked after the first server start."
        }
    )
    public static int linearFlushThreads = 1;

    public static int getLinearFlushThreads() {
        if (linearFlushThreads < 0) {
            return Math.max(Runtime.getRuntime().availableProcessors() + linearFlushThreads, 1);
        } else {
            return Math.max(linearFlushThreads, 1);
        }
    }
    // Horizon end - region

    @GlobalConfig(
        name = "configurable-mc-67",
        category = {"optimization"},
        comment = {
            "Fix for MC-67: allow entities riding a vehicle (passengers) to use portals.",
            "In vanilla Minecraft, entities with passengers cannot use Nether/End portals.",
            "Enabling this (default: true) restores the ability for vehicles with passengers to teleport.",
            "Disable to enforce the vanilla restriction."
        }
    )
    public static boolean allowEntityPortalWithPassenger = true;

    @GlobalConfig(
        name = "horizon-packet-event",
        category = {"features"},
        comment = {
            "Enables the Horizon custom packet event API.",
            "When enabled, plugins can listen to raw incoming and outgoing packets via Horizon's API.",
            "Disable if no plugins use this API to avoid the minor overhead of firing packet events."
        }
    )
    public static boolean horizonPacketEvent = false;

    // Horizon start - protocols

    // AppleSkin
    @GlobalConfig(
        name = "appleskin-enable",
        category = {"protocols", "appleskin"},
        comment = {
            "Enable server-side support for the AppleSkin mod protocol.",
            "AppleSkin shows accurate hunger, saturation and exhaustion values in the HUD.",
            "When enabled, the server periodically sends food data to clients with the mod installed.",
            "Clients without the mod are unaffected."
        }
    )
    public static boolean appleskinEnable = false;

    @GlobalConfig(
        name = "sync-tick-interval",
        category = {"protocols", "appleskin"},
        verify = ConfigVerify.IntConfigVerify.class,
        comment = {
            "How often (in server ticks) the server sends food stats to AppleSkin clients.",
            "Default: 20 ticks (1 second). Lower values give clients more real-time data",
            "but increase network traffic. Values below 5 are not recommended."
        }
    )
    public static int syncTickInterval = 20;

    // Jade
    // @GlobalConfig(name = "jade-enable", category = {"protocols", "jade"})
    // public static boolean jadeEnable = false;

    @RemovedConfig(name = "jade-enable", category = {"protocols", "jade"})

    // REI
    @GlobalConfig(
        name = "rei-enable",
        category = {"protocols", "rei"},
        comment = {
            "Enable server-side support for the REI (Roughly Enough Items) mod protocol.",
            "REI is a client-side recipe viewer. This option enables protocol-level communication",
            "between the server and REI clients, allowing features like server-side recipe filtering.",
            "Clients without REI are unaffected."
        }
    )
    public static boolean reiEnable = false;

    @GlobalConfig(
        name = "maps-block-message",
        category = {"protocols"},
        verify = ConfigVerify.StringConfigVerify.class,
        comment = {
            "Message sent to players when their minimap mod is blocked by the server.",
            "Applies to mods blocked via xaeromap-block-enable or journeymap-block-enable.",
            "Supports plain text. The message is sent as a chat message to the player."
        }
    )
    public static String blockMinimapsMessage = "On the server banned these mods (Xaero's Minimap/Xaero's World Map/JourneyMap)!";

    // XaeroMap
    @GlobalConfig(
        name = "xaeromap-enable",
        category = {"protocols", "xaeromap"},
        comment = {
            "Enable server-side support for the Xaero's World Map and Xaero's Minimap protocol.",
            "When enabled, the server sends a unique server identifier to Xaero's clients so they",
            "can maintain separate map data per server (avoiding map data mix-ups on reconnect).",
            "Clients without Xaero's mods are unaffected."
        }
    )
    public static boolean xaeroMapEnable = false;

    @GlobalConfig(
        name = "xaeromap-block-enable",
        category = {"protocols", "xaeromap"},
        comment = {
            "Block players using Xaero's Minimap or Xaero's World Map from connecting.",
            "When enabled, clients running these mods will be disconnected with the maps-block-message.",
            "Use if your server prohibits minimap usage for fairness (e.g. PvP servers)."
        }
    )
    public static boolean blockXaeromap = false;

    @GlobalConfig(
        name = "xaero-map-server-id",
        category = {"protocols", "xaeromap"},
        verify = ConfigVerify.IntConfigVerify.class,
        comment = {
            "A unique integer identifier sent to Xaero's mod clients to distinguish this server.",
            "Xaero's mods use this ID to separate map data across different servers.",
            "Default: randomly generated at first startup. Change only if you need a stable, predictable ID.",
            "Requires xaeromap-enable to be true."
        }
    )
    public static int xaeroMapServerID = new Random().nextInt();

    // JourneyMap
    @GlobalConfig(
        name = "journeymap-block-enable",
        category = {"protocols", "journeymap"},
        comment = {
            "Block players using the JourneyMap minimap mod from connecting.",
            "When enabled, clients running JourneyMap will be disconnected with the maps-block-message.",
            "Use if your server prohibits minimap usage for fairness (e.g. PvP servers)."
        }
    )
    public static boolean blockJourneyMap = false;

    // Carpet
    @GlobalConfig(
        name = "carpet-enable",
        category = {"protocols", "carpet"},
        comment = {
            "Enable server-side support for the Carpet mod protocol.",
            "Carpet is a technical Minecraft mod providing debugging tools, tweaks and automation features.",
            "Enabling this allows Carpet clients to communicate with the server using the Carpet protocol.",
            "Clients without Carpet are unaffected."
        }
    )
    public static boolean carpetEnable = false;

    // Syncmatica
    @GlobalConfig(
        name = "syncmatica-enable",
        category = {"protocols", "syncmatica"},
        verify = SyncmaticaVerify.class,
        comment = {
            "Enable the Syncmatica protocol for sharing Litematica schematics between players.",
            "Syncmatica allows players to upload, download and manage shared schematics on the server.",
            "Requires clients to have the Syncmatica mod installed. Clients without it are unaffected.",
            "Enable quota and quota-limit to restrict schematic file sizes."
        }
    )
    public static boolean syncmaticaProtocol = false;

    @GlobalConfig(
        name = "quota",
        category = {"protocols", "syncmatica"},
        comment = {
            "Enable per-player storage quotas for Syncmatica schematic uploads.",
            "When enabled, each player is limited to quota-limit bytes of schematic storage.",
            "Requires syncmatica-enable to be true."
        }
    )
    public static boolean syncmaticaQuota = false;

    @GlobalConfig(
        name = "quota-limit",
        category = {"protocols", "syncmatica"},
        verify = ConfigVerify.IntConfigVerify.class,
        comment = {
            "Maximum number of bytes a single player can upload via Syncmatica.",
            "Default: 40000000 (40 MB). Adjust based on available disk space and player trust.",
            "Requires syncmatica-enable and quota to both be true."
        }
    )
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

    @GlobalConfig(
        name = "extra-yggdrasil-service-enable",
        category = {"features", "yggdrasil"},
        verify = ExtraYggdrasilServiceVerify.class,
        comment = {
            "Enable support for additional (non-Mojang) Yggdrasil authentication services.",
            "This allows players authenticated via authlib-injector or alternative login providers to join.",
            "WARNING: This is an unofficial feature. Enabling it may introduce data security risks.",
            "Always review the authentication URLs listed in 'urls' before enabling.",
            "Automatically forces username validation to be enabled when active."
        }
    )
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

    @GlobalConfig(
        name = "login-protect",
        category = {"features", "yggdrasil"},
        comment = {
            "Enables a login protection mechanism for players authenticated via extra Yggdrasil services.",
            "Prevents account takeover by requiring re-authentication on suspicious logins.",
            "Only meaningful when extra-yggdrasil-service-enable is true."
        }
    )
    public static boolean loginProtect = false;

    @GlobalConfig(
        name = "urls",
        category = {"features", "yggdrasil"},
        lock = true,
        verify = ExtraYggdrasilUrlsValidator.class,
        comment = {
            "List of authlib-injector-compatible Yggdrasil authentication service URLs.",
            "Players will be authenticated against these services in addition to (or instead of) Mojang.",
            "Each entry must be a valid HTTPS URL pointing to an authlib-injector Yggdrasil endpoint.",
            "Example: https://auth.example.com/",
            "Requires extra-yggdrasil-service-enable to be true.",
            "This setting is locked after the first server start."
        }
    )
    public static List<String> serviceList = List.of("https://url.with.authlib-injector-yggdrasil");

    public static class ExtraYggdrasilUrlsValidator extends ConfigVerify.ListConfigVerify {
        @Override
        public String check(List<?> old, List<?> value) {
            HorizonMinecraftSessionService.initExtraYggdrasilList(serviceList);
            return null;
        }
    }

    @GlobalConfig(
        name = "async-chunk-send",
        category = {"optimization"},
        comment = {
            "Send chunk data to players asynchronously from a separate I/O thread.",
            "Reduces main thread overhead when many players are loading chunks simultaneously",
            "(e.g. on join, teleport, or fast travel).",
            "May improve TPS on servers with many concurrent chunk loads.",
            "Disable if you encounter chunk desync issues or plugin compatibility problems."
        }
    )
    public static boolean AsyncChunkSend = false;

    @GlobalConfig(
        name = "sentry-dsn",
        category = {"features", "sentry"},
        verify = SentryDsnVerify.class,
        comment = {
            "Sentry Data Source Name (DSN) for automatic error reporting.",
            "When set, uncaught exceptions and warnings from the server are sent to your Sentry project.",
            "Leave empty to disable Sentry integration.",
            "You can also set the SENTRY_DSN environment variable, which takes priority over this value.",
            "Obtain a DSN from your Sentry project settings at sentry.io or your self-hosted instance."
        }
    )
    public static String sentryDsn = "";

    @GlobalConfig(
        name = "sentry-only-log-thrown",
        category = {"features", "sentry"},
        comment = {
            "Only send events to Sentry that include a thrown exception (stack trace).",
            "When false (default), all WARN-level log messages are forwarded to Sentry.",
            "Enable this to reduce noise in Sentry and focus on actionable errors only."
        }
    )
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

    @GlobalConfig(
        name = "enable",
        category = {"security", "obfuscation-detection"},
        comment = {
            "Enable the obfuscation detection system for loaded plugins.",
            "When enabled, Horizon analyzes plugin JAR files at load time and flags those",
            "that show signs of code obfuscation (suspicious class names, encrypted strings, anti-debug, etc.).",
            "Suspicious plugins are logged as warnings. Thresholds below control sensitivity."
        }
    )
    public static boolean obfuscationDetectionEnabled = true;

    @GlobalConfig(
        name = "suspicious-name-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Fraction of class names that must look 'suspicious' (short, random-looking) to flag the plugin.",
            "Range: 0.0 to 1.0. Lower = more sensitive. Default: 0.30 (30% of classes).",
            "Suspicious names are typically single letters or short random strings used by obfuscators."
        }
    )
    public static double suspiciousNameThreshold = 0.30;

    @GlobalConfig(
        name = "short-name-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Fraction of class names that are very short (1-2 characters) to trigger a warning.",
            "Range: 0.0 to 1.0. Default: 0.20 (20% of classes).",
            "A high proportion of very short class names is a strong indicator of name obfuscation."
        }
    )
    public static double shortNameThreshold = 0.20;

    @GlobalConfig(
        name = "advanced-obfuscation-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Threshold for detecting advanced obfuscation techniques (control-flow, string encryption combos).",
            "Range: 0.0 to 1.0. Default: 0.10 (10% of classes showing advanced patterns).",
            "Lower this value to catch more sophisticated obfuscators at the cost of more false positives."
        }
    )
    public static double advancedObfuscationThreshold = 0.10;

    @GlobalConfig(
        name = "encrypted-string-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Fraction of string constants that appear encrypted or encoded (base64, hex blobs, etc.).",
            "Range: 0.0 to 1.0. Default: 0.15 (15% of string literals look encrypted).",
            "String encryption is commonly used to hide malicious URLs, commands, or payloads."
        }
    )
    public static double encryptedStringThreshold = 0.15;

    @GlobalConfig(
        name = "anti-debug-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Fraction of classes containing anti-debugging or anti-analysis techniques.",
            "Range: 0.0 to 1.0. Default: 0.05 (5% of classes).",
            "Anti-debug code (JVM agent detection, timing checks) is rare in legitimate plugins",
            "and is a strong indicator of malicious intent. Keep this threshold low."
        }
    )
    public static double antiDebugThreshold = 0.05;

    @GlobalConfig(
        name = "access-obfuscation-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Fraction of fields/methods using unusual access modifier patterns typical of obfuscators.",
            "Range: 0.0 to 1.0. Default: 0.25 (25% of members).",
            "Obfuscators often make everything public or apply synthetic flags to confuse decompilers."
        }
    )
    public static double accessObfuscationThreshold = 0.25;

    @GlobalConfig(
        name = "arithmetic-obfuscation-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Fraction of methods containing arithmetic obfuscation (constants replaced with complex expressions).",
            "Range: 0.0 to 1.0. Default: 0.20 (20% of methods).",
            "Arithmetic obfuscation replaces simple values like '1' with chains of XOR/shift operations."
        }
    )
    public static double arithmeticObfuscationThreshold = 0.20;

    @GlobalConfig(
        name = "crypto-usage-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Fraction of classes referencing cryptographic APIs (javax.crypto, etc.) suspiciously.",
            "Range: 0.0 to 1.0. Default: 0.10 (10% of classes).",
            "Some crypto usage is legitimate (e.g. hashing passwords), but high ratios combined",
            "with other signals can indicate payload encryption or C2 communication."
        }
    )
    public static double cryptoUsageThreshold = 0.10;

    @GlobalConfig(
        name = "small-plugin-short-name-threshold",
        category = {"security", "obfuscation-detection"},
        verify = ThresholdVerify.class,
        comment = {
            "Short-name threshold applied specifically to small plugins (below minimum-classes-for-large-plugin).",
            "Range: 0.0 to 1.0. Default: 0.40 (40% of class names).",
            "Small plugins naturally have fewer classes, so a higher threshold prevents false positives",
            "on tiny plugins with a few short utility class names."
        }
    )
    public static double smallPluginShortNameThreshold = 0.40;

    @GlobalConfig(
        name = "minimum-classes-for-large-plugin",
        category = {"security", "obfuscation-detection"},
        verify = ConfigVerify.IntConfigVerify.class,
        comment = {
            "Minimum number of classes in a JAR for it to be treated as a 'large' plugin.",
            "Default: 10. Plugins with fewer classes use the small-plugin-short-name-threshold,",
            "while plugins with this many classes or more use the standard suspicious-name-threshold.",
            "Adjust if you load many tiny utility plugins that trigger false positives."
        }
    )
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
