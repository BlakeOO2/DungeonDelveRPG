package com.SmokeyMC.party;

import com.SmokeyMC.Main;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyManager {
    private final Map<UUID, Party> playerParty = new HashMap<>();
    private final Set<UUID> partyChatToggled = new HashSet<>();
    private final Map<UUID, Long> lastSeenOffline = new HashMap<>();
    private final Main plugin;

    public PartyManager(Main plugin) {
        this.plugin = plugin;
    }

    public Party getParty(UUID player){
        return playerParty.get(player);
    }

    public void createParty(UUID leader){
        Party party = new Party(leader);
        playerParty.put(leader, party);
        Player leaderPlayer = plugin.getServer().getPlayer(leader);
        if (leaderPlayer != null) {
            leaderPlayer.sendMessage(plugin.getLanguageManager().getMessage("party.create.created_party"));
        }
    }

    public void invite(UUID inviter, UUID invitee){

        Party party = getParty(inviter);

        if (party == null){
            //TODO make this create a party if the inviter doesn't have one?
            plugin.getServer().getPlayer(inviter).sendMessage(plugin.getLanguageManager().getMessage("party.invite.no_party"));
            return;
        }

        if (playerParty.containsKey(invitee)){
            plugin.getServer().getPlayer(invitee).sendMessage(plugin.getLanguageManager().getMessage("party.invite.already_in_party"));
            return;
        }
        //Check to see if the party is full before sending the invite, gets the number from the config
        if (party.getMembers().size() >= plugin.getConfig().getInt("Party.maxSize")) {
            Player inviterPlayer = plugin.getServer().getPlayer(inviter);

            if (inviterPlayer != null && inviterPlayer.hasPermission("DDRPG.party.bypass.maxSize:")) {
                plugin.getServer().getPlayer(inviter).sendMessage(plugin.getLanguageManager().getMessage("party.invite.party_full", "max_size", plugin.getConfig().getInt("Party.maxSize") + ""));
                return;

            }

        }

        if (party.getInvited().contains(invitee)) {
            plugin.getServer().getPlayer(invitee).sendMessage(plugin.getLanguageManager().getMessage("party.invite.already_invited"));
            return;
        }

        party.getInvited().add(invitee);

        Player inviteePlayer = plugin.getServer().getPlayer(invitee);
        if (inviteePlayer != null) {
            inviteePlayer.sendMessage(plugin.getLanguageManager().getMessage("party.invite.from", "inviter", plugin.getServer().getPlayer(inviter).getName()));
            inviteePlayer.sendMessage(plugin.getLanguageManager().getMessage("party.invite.accept", "inviter", plugin.getServer().getPlayer(inviter).getName()));
            //inviteePlayer.sendMessage(plugin.getLanguageManager().getMessage("party.invite.decline", "command", "/party disband"));
        }

    }

    public void trackPlayerOffline(UUID playerID){
        if(getParty(playerID) != null){
            lastSeenOffline.put(playerID, System.currentTimeMillis());
        }
    }

    public void cleanupOfflineMembers() {
        long now = System.currentTimeMillis();
        plugin.debug("PartyManager: Cleanup: Now is " + now);
        long timeoutMillis = plugin.getConfig().getLong("Party.offlineTimeoutMinutes") * 1000L * 60L;
        plugin.debug("PartyManager: Cleanup: Timeout is " + timeoutMillis + "ms");
        Iterator<Map.Entry<UUID, Long>> iterator = lastSeenOffline.entrySet().iterator();

        while (iterator.hasNext()) {
            plugin.debug("PartyManager: Cleanup: Checking entry " + iterator.next());
            Map.Entry<UUID, Long> entry = iterator.next();
            plugin.debug("PartyManager: Cleanup: Checking entry " + entry.getKey() + " with value " + entry.getValue());
            UUID playerID = entry.getKey();
            plugin.debug("PartyManager: Cleanup: Checking entry " + playerID);
            Long offlineTime = entry.getValue();
            plugin.debug("PartyManager: Cleanup: Checking entry " + offlineTime);

            plugin.debug("PartyManager: Cleanup: Checking entry " + playerID + " with value " + offlineTime + " against now " + now);
            plugin.debug("now - offlinetime: " + (now - offlineTime) + " timeoutMillis: " + timeoutMillis + "");
            if (now - offlineTime >= timeoutMillis) {
                Party party = getParty(playerID);
                if (party != null) {
                    plugin.debug("PartyManager: Cleanup: Player " + playerID + " was offline for " + (now - offlineTime) + "ms, removing from party");
                    leaveParty(playerID); //Will run the logic for if its the leader will auto promote a new player
                }
                iterator.remove();  //Clean up the LastSeenOffline map
            }
        }
    }

    public void clearOfflineTrack(UUID playerID){
        lastSeenOffline.remove(playerID);
    }

    public void joinParty(UUID playerId, UUID targetParty) {
        //Check to see if the Player is in a party or not
        Party party = getParty(targetParty);

        if (playerParty.containsKey(playerId)) {
            plugin.getServer().getPlayer(playerId).sendMessage(plugin.getLanguageManager().getMessage("party.join.already_in_party"));
            return;
        }

        //Checks the size of the party before allowing the player to join, if it is greater then the max size it stop the joining event
        if(party.getMembers().size() >= plugin.getConfig().getInt("Party.maxSize")){
            Player joiningPlayer = plugin.getServer().getPlayer(playerId);

            //Check to see if the player is valid or has the bypass max size permission, if not then send a message to the player
            if(joiningPlayer != null && joiningPlayer.hasPermission("DDRPG.party.bypass.maxSize:")){
                plugin.getServer().getPlayer(targetParty).sendMessage(plugin.getLanguageManager().getMessage("party.join.party_full", "max_size", plugin.getConfig().getInt("Party.maxSize") + ""));

            }
        }

        if (getParty(targetParty).getInvited().contains(playerId)) {
            getParty(targetParty).getInvited().remove(playerId);
            getParty(targetParty).getMembers().add(playerId);
            playerParty.put(playerId, getParty(targetParty));

            Player joiningPlayer = plugin.getServer().getPlayer(playerId);
            //Send a message to the player that is new to the party
            if (joiningPlayer != null) {
                joiningPlayer.sendMessage((plugin.getLanguageManager().getMessage("party.join.joinedParty")));
            }

            //send a message to the rest of the members that joiningplayer joined their party
            for (UUID memberId : getParty(targetParty).getMembers()) {
                if (memberId.equals(playerId)) continue;
                Player member = plugin.getServer().getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(plugin.getLanguageManager().getMessage("party.join.another_join_party", "player", joiningPlayer.getName()));
                }
            }
            return;
        }
        plugin.getServer().getPlayer(playerId).sendMessage(plugin.getLanguageManager().getMessage("party.join.not_invited"));
    }

    public void leaveParty(UUID playerId) {
        Party party = getParty(playerId);

        if (party == null) {
            plugin.getServer().getPlayer(playerId).sendMessage(plugin.getLanguageManager().getMessage("party.leave.no_party"));
            return;
        }

        if (!party.getMembers().contains(playerId)) {
            plugin.getServer().getPlayer(playerId).sendMessage(plugin.getLanguageManager().getMessage("party.leave.not_in_party"));
            return;
        }

        party.getMembers().remove(playerId);
        playerParty.remove(playerId);

        Player player = plugin.getServer().getPlayer(playerId);
        String playerName = player != null ? player.getName() : "A Player";

        //Checks to see if the party leader left the party
        if (playerId.equals(party.getLeader())) {
            if (party.getMembers().isEmpty()) {
                if (player != null) {
                    //Sends a message saying that the party was disbanded just to the leader as they are the only one in the party
                    player.sendMessage(plugin.getLanguageManager().getMessage("party.leave.leader_left_disbanded_party"));
                }
                // No one left — disband
                return;
            }

            // Promote new leader
            UUID newLeader = party.getMembers().iterator().next(); // Just pick the first member
            party.setLeader(newLeader);

            //Lets the old party leader know that they have left the party if the party doesnt get disbanded and a new leader is placed
            if (player != null) {
                player.sendMessage(plugin.getLanguageManager().getMessage("party.leave.you_leader_left_party", "target", plugin.getServer().getPlayer(newLeader).getName()));
            }

            //Lets everyone know that the party leader has left the party and who the new leader is
            for (UUID memberId : party.getMembers()) {
                Player p = plugin.getServer().getPlayer(memberId);
                if (p != null) {
                    p.sendMessage(plugin.getLanguageManager().getMessage("party.leave.player_left_party", "player", playerName));
                    p.sendMessage(plugin.getLanguageManager().getMessage("party.leave.new_leader", "player", plugin.getServer().getPlayer(newLeader).getName()));
                }
            }
            return;
        }

        //Checks to see if another player left the party
        for (UUID memberId : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(memberId);
            if (p != null) {
                p.sendMessage(plugin.getLanguageManager().getMessage("party.leave.player_left_party", "player", playerName));
            }
        }

        if (player != null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("party.leave.you_left_party"));
        }
    }

    public void promoteMember(UUID oldLeaderId, UUID newLeaderId){
        Party party = getParty(oldLeaderId);

        //check to see if the player trying to promote has a party
        if(party == null){
            plugin.getServer().getPlayer(oldLeaderId).sendMessage(plugin.getLanguageManager().getMessage("party.promote.no_party"));
            return;
        }
        //the person that is trying to promote needs to be the leader of the party to do so
        if(!party.getLeader().equals(oldLeaderId)){
            plugin.getServer().getPlayer(oldLeaderId).sendMessage(plugin.getLanguageManager().getMessage("party.promote.not_leader"));
            return;
        }

        //check to see if the player trying to promote is in the party
        if (!party.getMembers().contains(newLeaderId)) {
            plugin.getServer().getPlayer(newLeaderId).sendMessage(plugin.getLanguageManager().getMessage("party.promote.not_in_party"));
            return;
        }

        //check to see if you are trying to promote yourself
        if (newLeaderId.equals(oldLeaderId)) {
            plugin.getServer().getPlayer(newLeaderId).sendMessage(plugin.getLanguageManager().getMessage("party.promote.same_player"));
        }

        //sets the new leader of the party
        party.setLeader(newLeaderId);

        Player newLeader = plugin.getServer().getPlayer(newLeaderId);
        Player oldLeader = plugin.getServer().getPlayer(oldLeaderId);

        for (UUID memberId : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(memberId);
            if (p != null) {
                p.sendMessage(plugin.getLanguageManager().getMessage("party.promote.player_promoted", "player", newLeader.getName()));
            }
        }
    }

    public void kickMemeber(UUID leaderID, UUID targetId){
        Party party = getParty(leaderID);

        //Checks to see if the player trying to kick has a party
        if (party == null) {
            plugin.getServer().getPlayer(leaderID).sendMessage(plugin.getLanguageManager().getMessage("party.kick.no_party"));
            return;
        }

        //Checks to see if someone other then the party leader is trying to kick someone else
        if (!party.getLeader().equals(leaderID)) {
            plugin.getServer().getPlayer(leaderID).sendMessage(plugin.getLanguageManager().getMessage("party.kick.not_leader"));
            return;
        }

        //Checks to see if the player trying to kick is in the party
        if (!party.getMembers().contains(targetId)) {
            plugin.getServer().getPlayer(leaderID).sendMessage(plugin.getLanguageManager().getMessage("party.kick.not_in_party"));
            return;
        }

        //Checks to see if the leader is trying to kick themselves
        if (targetId.equals(leaderID)) {
            plugin.getServer().getPlayer(leaderID).sendMessage(plugin.getLanguageManager().getMessage("party.kick.kick_self"));
            return;
        }

        party.getMembers().remove(targetId);
        playerParty.remove(targetId);

        Player target = plugin.getServer().getPlayer(targetId);
        String targetName = target != null ? target.getName() : "A Player";

        //Notify the player being kicked
        if (target != null) {
            target.sendMessage(plugin.getLanguageManager().getMessage("party.kick.player_kicked_self"));
        }

        //And now lets the rest of the party know that the player was kicked out
        for (UUID memberid : party.getMembers()) {
            Player member = plugin.getServer().getPlayer(memberid);
            if (member != null) {
                member.sendMessage(plugin.getLanguageManager().getMessage("party.kick.player_kicked", "player", targetName));
            }
        }
    }

    public void disbandParty(UUID leaderId){
        Party party = getParty(leaderId);

        if (party == null) {
            plugin.getServer().getPlayer(leaderId).sendMessage(plugin.getLanguageManager().getMessage("party.disband.no_party"));
            return;
        }

        if (!party.getLeader().equals(leaderId)) {
            plugin.getServer().getPlayer(leaderId).sendMessage(plugin.getLanguageManager().getMessage("party.disband.not_leader"));
            return;
        }

        for (UUID memberId : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(memberId);
            if (p != null) {
                p.sendMessage(plugin.getLanguageManager().getMessage("party.disband.party_disbanded"));
            }
        }

        playerParty.remove(leaderId);
        party.getMembers().clear();
        party.getInvited().clear();
    }

    public void togglePartyChat(UUID playerID){
        if(partyChatToggled.contains(playerID)){
            partyChatToggled.remove(playerID);
            Player player = plugin.getServer().getPlayer(playerID);
            if (player != null) {
                player.sendMessage(plugin.getLanguageManager().getMessage("party.chat.toggled_off"));
            }
            } else {
            partyChatToggled.add(playerID);
            Player player = plugin.getServer().getPlayer(playerID);
            if (player != null) {
                player.sendMessage(plugin.getLanguageManager().getMessage("party.chat.toggled_on"));
        }
        }
    }
    public boolean isPartyChatToggled(UUID playerID){
        return partyChatToggled.contains(playerID);
    }



    public void sendPartyMessage(UUID playerID, String message){
        Party party = getParty(playerID);
        if (party == null) {
            plugin.getServer().getPlayer(playerID).sendMessage(plugin.getLanguageManager().getMessage("party.chat.no_party"));
            return;
        }

        Player sender = plugin.getServer().getPlayer(playerID);
        String name = sender != null ? sender.getName() : "Unknown";

        for (UUID memberId : party.getMembers()) {
            Player member = plugin.getServer().getPlayer(memberId);
            if (member != null) {
                member.sendMessage(plugin.getLanguageManager().getMessage("party.chat.message", "player", name, "message", message));
            }
        }


    }

}
