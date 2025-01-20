package dev.gideonwhite1029.horizon.commands.subcommands;

import dev.gideonwhite1029.horizon.HorizonConfig;
import dev.gideonwhite1029.horizon.commands.HorizonSubcommand;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class ReloadCommand implements HorizonSubcommand {
    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        MinecraftServer server = MinecraftServer.getServer();
        HorizonConfig.init((File) server.options.valueOf("horizon-settings"));
        Command.broadcastCommandMessage(sender, text("Horizon config reload complete.", GREEN));
        return false;
    }
}
