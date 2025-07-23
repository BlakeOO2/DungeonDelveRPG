package com.SmokeyMC.party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private UUID leader;
    private Set<UUID> members = new HashSet<>();
    private Set<UUID> invited = new HashSet<>();

    public Party(UUID leader){
        this.leader = leader;
        members.add(leader);
    }

    //TODO /remover/kick/promote

    public Set<UUID> getInvited() {
        return invited;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader){
        this.leader = leader;
    }



}
