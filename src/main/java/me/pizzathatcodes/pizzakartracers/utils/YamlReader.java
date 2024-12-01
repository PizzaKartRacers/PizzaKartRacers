package me.pizzathatcodes.pizzakartracers.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class YamlReader {

    /**
     * Reads and parses a YAML file.
     *
     * @param filePath The path to the YAML file.
     * @return A Map representing the YAML data.
     */
    public static Map<String, Object> readYamlFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            return yaml.load(inputStream); // Load the YAML file into a Map
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}