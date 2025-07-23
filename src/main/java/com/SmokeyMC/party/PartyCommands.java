package com.SmokeyMC.party;

import com.SmokeyMC.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class PartyCommands implements CommandExecutor {
    private final Main plugin;

    public PartyCommands(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, @NotNull String[] args) {
        plugin.debug("Party executed from:" + sender.getName() + " with args: " + label);

        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("player_only"));
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                showHelp(player);
                return true;
            }

            plugin.debug("Party executed with args: " + args[0]);

            switch (args[0].toLowerCase()) {
                case "help":
                    showHelp(player);
                    break;
                case "create":
                    plugin.getPartyManager().createParty(player.getUniqueId());
                    break;
                case "invite":
                    if (args.length < 2) { // Check if args[1] exists
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.usage.invite"));
                        break;
                    }
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target != null) {
                        plugin.getPartyManager().invite(player.getUniqueId(), target.getUniqueId());
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.invite.sent", "target", target.getName()));
                    } else {
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.invite.not_online"));
                    }
                    break;
                case "leave":
                    plugin.getPartyManager().leaveParty(player.getUniqueId());
                    break;
                case "list":
                    Party party = plugin.getPartyManager().getParty(player.getUniqueId());

                    if (party == null) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.list.no_party"));
                        break;
                    }

                    player.sendMessage(plugin.getLanguageManager().getMessage("party.list.top_of_party_member_list"));
                    //Sends the party leader first
                    String theLeader = plugin.getServer().getPlayer(party.getLeader()).getName();
                    player.sendMessage(plugin.getLanguageManager().getMessage("party.list.leader", "player", theLeader));


                    for (UUID memberId : party.getMembers()) {
                        Player member = plugin.getServer().getPlayer(memberId);
                        String name = member != null ? member.getName() : "Unknown";

                        if (memberId.equals(party.getLeader())) {
                            //This is where it would look for the party leader, if it is true it will skip this
                            //Changed how it got done because it would paste the leader in a random order, now it will do
                            // it at the top
                        } else {
                            player.sendMessage("§7- §f" + name);
                        }
                    }
                    break;
                case "promote":
                    if (args.length < 2) { // Check for args[1]
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.usage.promote"));
                        break;
                    }

                    Player newLeader = plugin.getServer().getPlayer(args[1]);
                    if (newLeader == null) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.promote.not_online"));
                        break;
                    }
                    plugin.getPartyManager().promoteMember(player.getUniqueId(), newLeader.getUniqueId());
                    break;
                case "kick":
                    if (args.length < 2) { // Make sure args[1] is available
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.usage.kick"));
                        break;
                    }
                    Player kickTarget = plugin.getServer().getPlayer(args[1]);
                    if (kickTarget == null) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.kick.not_online"));
                        break;
                    }
                    plugin.getPartyManager().kickMemeber(player.getUniqueId(), kickTarget.getUniqueId());
                    break;
                case "accept":
                    if (args.length < 2) { // Validate args[1] exists
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.usage.accept"));
                        break;
                    }
                    Player targetParty = plugin.getServer().getPlayer(args[1]);
                    if (targetParty != null) {
                        plugin.getPartyManager().joinParty(player.getUniqueId(), targetParty.getUniqueId());
                    } else {
                        player.sendMessage(plugin.getLanguageManager().getMessage("party.join.not_invited"));
                    }
                    break;
                case "disband":
                    plugin.getPartyManager().disbandParty(player.getUniqueId());
                    break;
                case "chat":
                    if (args.length == 1) {
                        plugin.getPartyManager().togglePartyChat(player.getUniqueId());
                    } else {
                        //One time message with /party chat {message}
                        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                        plugin.getPartyManager().sendPartyMessage(player.getUniqueId(), message);
                    }
                    break;
                default:
                    showHelp(player);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    private void showHelp(Player player) {
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.help"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.create"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.invite"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.leave"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.list"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.promote"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.kick"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.accept"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.disband"));
        player.sendMessage(plugin.getLanguageManager().getMessage("party.help.chat"));

    }
}

