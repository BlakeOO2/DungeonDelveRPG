package com.SmokeyMC;

import com.SmokeyMC.AdminCommands.gamemode;
import com.SmokeyMC.dungeon.Dungeon;
import com.SmokeyMC.dungeon.DungeonMaps.FireDungeon;
import com.SmokeyMC.language.LanguageManager;
import com.SmokeyMC.party.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends JavaPlugin {
    private static Main instance;
    private LanguageManager languageManager;
    private PartyManager partyManager;
    private List<Dungeon> registeredDungeons;


    public void onEnable(){
        saveDefaultConfig();

        instance = this;
        languageManager = new LanguageManager(this);
        partyManager = new PartyManager(this);
        RegisterCommands();
        RegisterListeners();
        clearnUpOfflinePartyMembers();
        registeredDungeons = List.of(
                new FireDungeon(this)
        );


    }

    public void onDisable(){

    }


    public void reload(){
        reloadConfig();
    }

    public void RegisterCommands(){
        //getCommand("gamemode").setExecutor(new gamemode(this));
        getCommand("party").setExecutor(new PartyCommands(this));
        getCommand("pc").setExecutor(new PartyChatCommand(this));

        getCommand("startdungeon").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            registeredDungeons.get(0).startInstance(player); // For example
            return true;
        });

        getCommand("enddungeon").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            registeredDungeons.get(0).endInstance(player.getUniqueId());
            return true;
        });
    }

    public void RegisterListeners(){
        getServer().getPluginManager().registerEvents(new PartyChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PartyActivicyListener(this), this);
    }


    public void debug(String message){
        if(getConfig().getBoolean("debug")){
            getLogger().info("[DEBUG] "+message);
        }
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public int getMaxPartySize(){
        return getConfig().getInt("Party.maxsize", 6);
    }

    private void clearnUpOfflinePartyMembers(){
        getServer().getScheduler().runTaskTimer(this, () -> {
            getPartyManager().cleanupOfflineMembers();
        }, 20L * 60, 20L * 60);
    }



}