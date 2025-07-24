package com.SmokeyMC.dungeon;

import com.SmokeyMC.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.bukkit.Bukkit.createChunkData;

public abstract class Dungeon {

    protected final Main plugin;
    protected final String id; //e.g dungeon_a
    protected final String worldTemplateName; //Folder in maps/
    protected final File configFile;
    protected FileConfiguration config;
    protected final Map<UUID, World> activeInstances = new HashMap<>();


    protected Dungeon(Main plugin, String id, String worldTemplateName){
        this.plugin = plugin;
        this.id = id;
        this.worldTemplateName = worldTemplateName;

        File dungeonConfigDir = new File(plugin.getDataFolder(), "dungeons/"+ id);
        if(!dungeonConfigDir.exists()) dungeonConfigDir.mkdir();

        this.configFile = new File(dungeonConfigDir, "config.yml");
        if(!configFile.exists()) plugin.saveResource("default_dungeon_config.yml", false); //Optional default

        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void startInstance(Player player) {
        String instanceWorldname = id + "_" + UUID.randomUUID();

        File source = new File(plugin.getDataFolder(), "dungeons/maps/" + worldTemplateName);
        File destination = new File(Bukkit.getWorldContainer(), instanceWorldname);

        try {
            copyWorldFolder(source.toPath(), destination.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        WorldCreator wc = new WorldCreator(instanceWorldname);
        wc.generator(new VoidGenerator());
        World world = Bukkit.createWorld(wc);

        if (world == null) {
            plugin.getLogger().severe("Could not create instance world: " + instanceWorldname);
            return;
        }

        // Disable auto-save for the world
        world.setAutoSave(false);

        plugin.debug("Successfully created temporary world: " + instanceWorldname);
        activeInstances.put(player.getUniqueId(), world);

        // Start any specific dungeon-related logic
        onStart(player, world);

        // Teleport the player to the instance
        player.teleport(new Location(world, 0, 100, 0));
    }

    public void endInstance(UUID playerId) {
        World world = activeInstances.remove(playerId);
        if (world == null) return;

        // Unload the world without saving
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.unloadWorld(world, false); // 'false' skips saving
            deleteWorldFolder(world.getWorldFolder());
            plugin.getLogger().info("Deleted temporary dungeon world: " + world.getName());
        });
    }

    protected void copyWorldFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(sourcePath -> {
            try {
                Path target = dest.resolve(src.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(target)) Files.createDirectory(target);
                } else {
                    Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //Delete the uid.dat to avoid duplicate world conflicts
        File uidFile = new File(dest.toFile(), "uid.dat");
        if (uidFile.exists()) uidFile.delete();

        //Remove untities to avoid UUID duplicatoin
        File entitiesFile = new File(dest.toFile(), "data/entities.dat");
        if (entitiesFile.exists()) entitiesFile.delete();
    }

    protected void deleteWorldFolder(File folder) {
        if (folder == null || !folder.exists()) return;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) deleteWorldFolder(f);
        }
        folder.delete();
    }

    public abstract void onStart(Player player, World instanceWorld);

    public abstract String getDisplayName(); // UI name

    // Optional: access config for particle locations, etc.
    public List<Location> getParticleLocations(World world) {
        List<Location> locs = new ArrayList<>();
        List<Map<?, ?>> rawList = config.getMapList("particles");
        for (Map<?, ?> map : rawList) {
            double x = (double) map.get("x");
            double y = (double) map.get("y");
            double z = (double) map.get("z");
            locs.add(new Location(world, x, y, z));
        }
        return locs;
    }

    public static class VoidGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biome) {
            return createChunkData(world); // Empty
        }
    }



}
