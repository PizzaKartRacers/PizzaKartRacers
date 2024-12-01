package me.pizzathatcodes.pizzakartracers.startup_logic;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapLoader {

    /**
     * Loads a map from a folder and returns the InstanceContainer.
     *
     * @param folderPath The path to the map folder.
     * @return The InstanceContainer representing the loaded map.
     */
    public static InstanceContainer loadMap(String folderPath) {
        File mapFolder = new File(folderPath);
        if (!mapFolder.exists() || !mapFolder.isDirectory()) {
            throw new IllegalArgumentException("Invalid map folder: " + folderPath);
        }

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer();
        instance.setChunkLoader(new AnvilLoader(mapFolder.toPath()));

        return instance;
    }

    /**
     * Parses the maps.yml file into a list of MapInfo objects.
     *
     * @param filePath The path to the maps.yml file.
     * @return A list of MapInfo objects.
     */
    public static List<MapInfo> getMapData(String filePath) {
        Yaml yaml = new Yaml();
        List<MapInfo> maps = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> data = yaml.load(inputStream);
            Map<String, Object> mapsSection = (Map<String, Object>) data.get("Maps");

            for (String mapName : mapsSection.keySet()) {
                Map<String, Object> mapData = (Map<String, Object>) mapsSection.get(mapName);

                String mapFileName = (String) mapData.get("mapFileName");
                boolean enabled = Boolean.TRUE.equals(mapData.get("enabled"));

                // Parse queue spawn locations
                List<Pos> queueSpawnLocations = parseSpawnLocations(
                        (Map<Object, Object>) ((Map<String, Object>) mapData.get("queue")).get("spawn-locations"));

                // Parse game spawn locations
                List<Pos> gameSpawnLocations = parseSpawnLocations(
                        (Map<Object, Object>) ((Map<String, Object>) mapData.get("game")).get("spawn-locations"));

                // Create MapInfo object
                maps.add(new MapInfo(mapName, mapFileName, enabled, queueSpawnLocations, gameSpawnLocations));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return maps;
    }

    /**
     * Parses spawn locations from a YAML section.
     *
     * @param locationsSection The YAML section containing spawn locations.
     * @return A map of spawn locations keyed by their index.
     */
    private static List<Pos> parseSpawnLocations(Map<Object, Object> locationsSection) {
        List<Pos> spawnLocations = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : locationsSection.entrySet()) {
            System.out.println("Processing entry: " + entry);

            Map<String, Object> locData = (Map<String, Object>) entry.getValue();

            // Safely cast each coordinate and rotation value to double or float
            double x = toDouble(locData.get("x"));
            double y = toDouble(locData.get("y"));
            double z = toDouble(locData.get("z"));
            float pitch = toFloat(locData.get("pitch"));
            float yaw = toFloat(locData.get("yaw"));

            Pos position = new Pos(x, y, z, yaw, pitch);
            System.out.println("Parsed Pos: " + position);

            spawnLocations.add(position);
        }

        return spawnLocations;
    }

    private static double toDouble(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else {
            throw new IllegalArgumentException("Invalid type for coordinate value: " + value.getClass());
        }
    }

    private static float toFloat(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else {
            throw new IllegalArgumentException("Invalid type for rotation value: " + value.getClass());
        }
    }


}
