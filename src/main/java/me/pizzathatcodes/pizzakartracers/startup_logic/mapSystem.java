package me.pizzathatcodes.pizzakartracers.startup_logic;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import static me.pizzathatcodes.pizzakartracers.utils.worldUtils.*;

public class mapSystem {

    public String map;

    public String mapName;

    public String code;

    public boolean mapLoaded = false;
    public boolean isMapLoading = false;

    public ArrayList<String> validGameSpawnLocations = new ArrayList<>();

    public mapSystem() {

        ArrayList<String> validMaps = new ArrayList<>();
        for(String Maps : util.getMapFile().getConfig().getConfigurationSection("Maps").getKeys(false)) {
            if(!util.getMapFile().getConfig().getBoolean("Maps." + Maps + ".enabled")) return;
            validMaps.add(Maps);
        }

        Random r = new Random();
        int randomNum = r.nextInt((validMaps.size() + 1)-1) + 1;
        mapName = validMaps.get(randomNum-1);

        code = generateCode();
        map = mapName + "-" + code;
    }

    public String generateCode() {

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        return generatedString;
    }

    public void loadMap() {
        setMapLoading(true);

        File folder = new File("maps");

        copyWorld(
                Paths.get(folder.getPath(), util.getMapFile().getConfig().getString("Maps." + mapName + ".mapFileName")),
                Paths.get(mapName + "-" + code),
                new Runnable() {
                    @Override
                    public void run() {
                        WorldCreator worldCreator = new WorldCreator(mapName + "-" + code);
                        World w = worldCreator.createWorld();
                        w.setGameRuleValue("doMobSpawning", "false");
                        w.setGameRuleValue("doDaylightCycle", "false");
                        w.setGameRuleValue("doWeatherCycle", "false");
                        w.setGameRuleValue("doMobLoot", "false");
                        w.setGameRuleValue("announceAdvancements", "false");
                        util.runConsoleCommand("gamerule doMobSpawning false", w);
                        util.runConsoleCommand("gamerule doDaylightCycle false", w);
                        util.runConsoleCommand("gamerule doWeatherCycle false", w);
                        util.runConsoleCommand("gamerule doMobLoot false", w);
                        util.runConsoleCommand("gamerule announceAdvancements false", w);
                        util.runConsoleCommand("weather clear 1000000", w);

                        util.runConsoleCommand("rg flag __global__ -w " + w.getName() + " build deny", w);
                        util.runConsoleCommand("rg flag __global__ -w " + w.getName() + " block-break deny", w);
                        util.runConsoleCommand("rg flag __global__ -w " + w.getName() + " block-place deny", w);
                        util.runConsoleCommand("rg flag __global__ -w " + w.getName() + " pvp deny", w);
                        util.runConsoleCommand("rg flag __global__ -w " + w.getName() + " mob-spawning deny", w);
                        util.runConsoleCommand("rg flag __global__ -w " + w.getName() + " vine-growth deny", w);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (Bukkit.getWorld(mapName + "-" + code) == null) return;
                                Main.getInstance().getLogger().info("World Loaded! ");
                                setMapLoaded(true);
                                cancel();
                            }
                        }.runTaskTimer(Main.getInstance(), 0L, 20L);
                    }
                });
    }

    /**
     * Unload the map
     */
    public void unloadMap() {
        World world = Bukkit.getWorld(map);
        unloadWorld(world);
        deleteWorld(world.getWorldFolder());
    }


    /**
     * Check if the map is loaded
     * @return boolean if the map is loaded
     */
    public boolean isMapLoaded() {
        return mapLoaded;
    }

    /**
     * Set the map loaded status
     * @param isLoaded boolean if the map is loaded
     */
    public void setMapLoaded(boolean isLoaded) {
        this.mapLoaded = isLoaded;
    }

    /**
     * Check if the map is loading
     * @return boolean if the map is loading
     */
    public boolean isMapLoading() {
        return isMapLoading;
    }

    /**
     * Set the map loading status
     * @param isLoading boolean if the map is loading
     */
    public void setMapLoading(boolean isLoading) {
        this.isMapLoading = isLoading;
    }


    public void teleportPlayerToWaitingRoom(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {

                try {

                    ArrayList<String> validSpawnLocations = new ArrayList<>();
                    for(String spawnLocation : util.getMapFile().getConfig().getConfigurationSection("Maps." + mapName + ".queue.spawn-locations").getKeys(false)) {
                        validSpawnLocations.add(spawnLocation);
                    }
                    if (validSpawnLocations == null || validSpawnLocations.size() == 0 ) {
                        Main.getInstance().getLogger().warning("No spawn locations found for map: " + mapName);
                        return;
                    }



                    Random random = new Random();
                    int index = random.nextInt(validSpawnLocations.size());
                    String locationObj = validSpawnLocations.get(index);

                    double x = util.getMapFile().getConfig().getInt("Maps." + mapName + ".queue.spawn-locations." + locationObj + ".x");
                    double y = util.getMapFile().getConfig().getInt("Maps." + mapName + ".queue.spawn-locations." + locationObj + ".y");
                    double z = util.getMapFile().getConfig().getInt("Maps." + mapName + ".queue.spawn-locations." + locationObj + ".z");
                    float yaw = util.getMapFile().getConfig().getInt("Maps." + mapName + ".queue.spawn-locations." + locationObj + ".yaw");
                    float pitch = util.getMapFile().getConfig().getInt("Maps." + mapName + ".queue.spawn-locations." + locationObj + ".pitch");

                    Location location = new Location(Bukkit.getWorld(map), x, y, z, yaw, pitch);
                    player.teleport(location);
//                    player.sendMessage(ChatColor.GREEN + "You have been teleported to the waiting room!");
                } catch (Exception e) {
                    Main.getInstance().getLogger().severe("Error teleporting player to waiting room: " + e.getMessage());
                    e.printStackTrace();
                }


            }
        }.runTaskLater(Main.getInstance(), 1L);
    }

    public void teleportPlayerToGame(Player player) {
        for (String spawnLocation : util.getMapFile().getConfig().getConfigurationSection("Maps." + mapName + ".game.spawn-locations").getKeys(false)) {
            validGameSpawnLocations.add(spawnLocation);
        }

        if (validGameSpawnLocations == null || validGameSpawnLocations.isEmpty()) {
            Main.getInstance().getLogger().warning("No spawn locations found for map: " + mapName);
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Random random = new Random();
                    int index = random.nextInt(validGameSpawnLocations.size());
                    String locationObj = validGameSpawnLocations.get(index);

                    double x = util.getMapFile().getConfig().getInt("Maps." + mapName + ".game.spawn-locations." + locationObj + ".x");
                    double y = util.getMapFile().getConfig().getInt("Maps." + mapName + ".game.spawn-locations." + locationObj + ".y");
                    double z = util.getMapFile().getConfig().getInt("Maps." + mapName + ".game.spawn-locations." + locationObj + ".z");
                    float yaw = util.getMapFile().getConfig().getInt("Maps." + mapName + ".game.spawn-locations." + locationObj + ".yaw");
                    float pitch = util.getMapFile().getConfig().getInt("Maps." + mapName + ".game.spawn-locations." + locationObj + ".pitch");

                    GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUniqueId());
                    Location location = new Location(Bukkit.getWorld(map), x, y, z, yaw, pitch);

                    // Teleport the kart (ArmorStand)
                    gamePlayer.getKart().getKartEntity().teleport(location);

                    // Teleport the player
                    player.teleport(location);
                    gamePlayer.getKart().getKartEntity().remove();

                    // Delay the assignment of the player to the kart to ensure both are teleported
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gamePlayer.createKart();
                        }
                    }.runTaskLater(Main.getInstance(), 3L); // A slight delay to ensure teleport is complete

                    validGameSpawnLocations.remove(locationObj);

                } catch (Exception e) {
                    Main.getInstance().getLogger().severe("Error teleporting player to the game: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskLater(Main.getInstance(), 1L);
    }


}
