package me.pizzathatcodes.pizzakartracers.startup_logic;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.utils.YamlReader;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;

import java.util.*;

public class mapSystem {

    MapInfo selectedMap;
    public String mapNameWithCode;
    public String code;
    public InstanceContainer mapInstance;

    public mapSystem() {

        List<MapInfo> maps = MapLoader.getMapData("maps.yml");
        ArrayList<MapInfo> validMaps = new ArrayList<>();
        for(MapInfo map : maps) {
            if(map.isEnabled()) {
                validMaps.add(map);
            }
        }
        code = generateCode();

        Random r = new Random();
        int randomNum = r.nextInt(validMaps.size());
        selectedMap = validMaps.get(randomNum);
        mapNameWithCode = selectedMap.getMapName() + "-" + code;
        mapInstance = MapLoader.loadMap("maps/" + selectedMap.getMapFileName());


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

    public InstanceContainer getMapInstance() {
        return mapInstance;
    }

    public void teleportPlayerToWaitingRoom(Player player) {
        Random random = new Random();
        int randomNum = random.nextInt(selectedMap.getQueueSpawnLocations().size());
        player.teleport(selectedMap.getQueueSpawnLocations().get(randomNum));
    }

    public Pos getWaitingRoomSpawnLocation() {
        Random random = new Random();
        int randomNum = random.nextInt(selectedMap.getQueueSpawnLocations().size());
        return selectedMap.getQueueSpawnLocations().get(randomNum);
    }

    List<Pos> validGameSpawnLocations = new ArrayList<>();
    public void teleportPlayerToGame(Player player) {
        if(validGameSpawnLocations.size() == 0) {
            validGameSpawnLocations.addAll(selectedMap.getGameSpawnLocations());
        }
        Random random = new Random();
        int randomNum = random.nextInt(validGameSpawnLocations.size());
        player.teleport(validGameSpawnLocations.get(randomNum));
        GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUuid());
        gamePlayer.getKart().getKartEntity().teleport(validGameSpawnLocations.get(randomNum));
        player.teleport(validGameSpawnLocations.get(randomNum));
        gamePlayer.getKart().getKartEntity().addPassenger(player);
        validGameSpawnLocations.remove(randomNum);
    }

}
