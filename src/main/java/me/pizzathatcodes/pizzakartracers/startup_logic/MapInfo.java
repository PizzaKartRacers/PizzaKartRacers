package me.pizzathatcodes.pizzakartracers.startup_logic;

import net.minestom.server.coordinate.Pos;

import java.util.List;

public class MapInfo {

    String mapName;
    String mapFileName;
    boolean enabled;
    List<Pos> queueSpawnLocations;
    List<Pos> gameSpawnLocations;

    /**
     * Constructor for MapInfo
     * @param mapName The name of the map
     * @param mapFileName The name of the file that the map is stored in
     * @param enabled Whether the map is enabled
     * @param queueSpawnLocations The spawn locations for the queue
     * @param gameSpawnLocations The spawn locations for the game
     */
    public MapInfo(String mapName, String mapFileName, boolean enabled, List<Pos> queueSpawnLocations, List<Pos> gameSpawnLocations) {
        this.mapName = mapName;
        this.mapFileName = mapFileName;
        this.enabled = enabled;
        this.queueSpawnLocations = queueSpawnLocations;
        this.gameSpawnLocations = gameSpawnLocations;
    }

    /**
     * Gets the name of the map
     * @return The name of the map
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * Gets the name of the file that the map is stored in
     * @return The name of the file that the map is stored in
     */
    public String getMapFileName() {
        return mapFileName;
    }

    /**
     * Gets whether the map is enabled
     * @return Whether the map is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the spawn locations for the queue
     * @return The spawn locations for the queue
     */
    public List<Pos> getQueueSpawnLocations() {
        return queueSpawnLocations;
    }

    /**
     * Gets the spawn locations for the game
     * @return The spawn locations for the game
     */
    public List<Pos> getGameSpawnLocations() {
        return gameSpawnLocations;
    }

    /**
     * Sets the name of the map
     * @param mapName The name of the map
     */
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    /**
     * Sets the name of the file that the map is stored in
     * @param mapFileName The name of the file that the map is stored in
     */
    public void setMapFileName(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    /**
     * Sets whether the map is enabled
     * @param enabled Whether the map is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the spawn locations for the queue
     * @param queueSpawnLocations The spawn locations for the queue
     */
    public void setQueueSpawnLocations(List<Pos> queueSpawnLocations) {
        this.queueSpawnLocations = queueSpawnLocations;
    }

    /**
     * Sets the spawn locations for the game
     * @param gameSpawnLocations The spawn locations for the game
     */
    public void setGameSpawnLocations(List<Pos> gameSpawnLocations) {
        this.gameSpawnLocations = gameSpawnLocations;
    }


}
