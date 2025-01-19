package dev.gideonwhite1029.horizon;

import com.destroystokyo.paper.util.SneakyThrow;
import dev.gideonwhite1029.horizon.commands.GlobalConfigManager;
import dev.gideonwhite1029.horizon.commands.HorizonCommand;
import dev.gideonwhite1029.horizon.config.GlobalConfig;
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

}
