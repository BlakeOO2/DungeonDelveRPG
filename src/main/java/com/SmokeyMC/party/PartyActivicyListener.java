package com.SmokeyMC.party;

import com.SmokeyMC.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PartyActivicyListener implements Listener {
    private final Main plugin;

    public PartyActivicyListener(Main plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        plugin.debug("Player " + event.getPlayer().getName() + " has quit. and has been added to the timeout checker for party.");
        UUID playerID = event.getPlayer().getUniqueId();
        plugin.getPartyManager().trackPlayerOffline(playerID);

        Party party = plugin.getPartyManager().getParty(playerID);
        String playerName = event.getPlayer().getName();
        for (UUID memberId : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(memberId);
            if (p != null) {
                p.sendMessage(plugin.getLanguageManager().getMessage("party.leave.player_has_gone_offline", "player", playerName, "timeLeft", plugin.getConfig().getInt("Party.offlineTimeoutMinutes") + ""));
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        plugin.getPartyManager().clearOfflineTrack(event.getPlayer().getUniqueId());

        UUID playerID = event.getPlayer().getUniqueId();
        Party party = plugin.getPartyManager().getParty(playerID);
        String playerName = event.getPlayer().getName();
        if (party != null) {
            for (UUID memberId : party.getMembers()) {
                Player p = plugin.getServer().getPlayer(memberId);
                if (p != null) {
                    p.sendMessage(plugin.getLanguageManager().getMessage("party.leave.rejoined_party", "player", playerName));
                }
            }
        }
    }
}
