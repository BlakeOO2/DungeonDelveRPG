package com.SmokeyMC.dungeon.DungeonMaps;

import com.SmokeyMC.Main;
import com.SmokeyMC.dungeon.Dungeon;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class FireDungeon extends Dungeon {

    public FireDungeon(Main plugin){
        super(plugin, "fire_dungeon", "FireDungeon");
    }

    @Override
    public void onStart(Player player, World instanceWorld){
        plugin.debug("Starting FireDungeon for player " + player.getName());

        for (Location loc : getParticleLocations(instanceWorld) ) {
            instanceWorld.spawnParticle(org.bukkit.Particle.FLAME, loc, 20, 0.5, 1, 0.5);
        }
    }

    @Override
    public String getDisplayName() {
        return plugin.getLanguageManager().getMessage("dungeon.fire_dungeon.name");
    }

}
