package com.SmokeyMC.party;

import com.SmokeyMC.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyChatCommand implements CommandExecutor {
    private final Main plugin;

    public PartyChatCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player_only"));
            return true;
        }

        if (args.length == 0 ){
            //toggles party chat
            plugin.getPartyManager().togglePartyChat(player.getUniqueId());
        } else {
            String message = String.join(" ", args);
            plugin.getPartyManager().sendPartyMessage(player.getUniqueId(), message);
        }
        return true;
     }
}
