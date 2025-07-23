package com.SmokeyMC.AdminCommands;

import com.SmokeyMC.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class gamemode implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public gamemode(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        plugin.debug("Gamemode executed from:" + sender.getName() + " with args: " + label.toString() );

        if(!sender.hasPermission("DDRPG.admin.gamemode")){
            sender.sendMessage(plugin.getLanguageManager().getMessage("no_permission"));
            return true;
        }

        //TODO need to finish the gamemode command
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
