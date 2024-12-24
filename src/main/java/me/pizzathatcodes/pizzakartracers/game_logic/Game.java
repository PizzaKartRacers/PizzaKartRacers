package me.pizzathatcodes.pizzakartracers.game_logic;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.*;
import me.pizzathatcodes.pizzakartracers.game_logic.event.DriftingHandler;
import me.pizzathatcodes.pizzakartracers.runnables.game.GameStartRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartAccelerationRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartBoostPadDelayRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartDriftXPBarRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartMovementRunnable;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {

    private ArrayList<GamePlayer> players;
    private ArrayList<spectatorSystem> spectators = new ArrayList<>();
    private GameState status;

//    private ArrayList<spectatorSystem> spectators = new ArrayList<>();

    public Game() {
        players = new ArrayList<>();
        status = GameState.QUEUE;
    }


    public ArrayList<GamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(GamePlayer player) {
        players.add(player);
    }

    public void removePlayer(GamePlayer player) {
        for (GamePlayer gamePlayer : players) {
            if (gamePlayer.getUuid().equals(player.getUuid())) {
                players.remove(gamePlayer);
                break;
            }
        }
    }

    public GamePlayer getGamePlayer(UUID uuid) {
        return players.stream().filter(gamePlayer -> gamePlayer.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public GamePlayer findGamePlayerFromKart(Kart kart) {
        return players.stream().filter(gamePlayer -> gamePlayer.getKart().equals(kart)).findFirst().orElse(null);
    }

    public GameState getStatus() {
        return status;
    }

    public void setStatus(GameState status) {
        this.status = status;
    }

    /**
     * Get the spectators/dead players
     * @return the spectators/dead players
     */
    public List<spectatorSystem> getSpectators() {
        return spectators;
    }

    private boolean isPlayerSpectator(UUID playerUUID) {
        for (spectatorSystem spe : getSpectators()) {
            if (spe.getPlayerUUID().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    public void startGame() {

        // TODO: Add wait logic so people can't drive off when the game starts
        setStatus(GameState.STARTING);
        GameStartRunnable.startTask();

        for(GamePlayer gamePlayer : players) {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid());
            gamePlayer.getKart().moving = "none";
            gamePlayer.getKart().turning = "none";
            gamePlayer.getKart().setAcceleration(0);
            gamePlayer.getKart().getKartEntity().setVelocity(new Vec(0,0,0));
            Main.getMapSystem().teleportPlayerToGame(player);
            player.sendMessage(util.translate("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            player.sendMessage(util.translate("&f&l                       Pizza Kart Racers                       "));
            player.sendMessage(util.translate("&f&l                                                               "));
            player.sendMessage(util.translate("&f&l             &e&lRace your friends to the finish line.             "));
            player.sendMessage(util.translate("&f&l              &e&lGrab items on the way and use them               "));
            player.sendMessage(util.translate("&f&l                 &e&lto get ahead of everyone else!                 "));
            player.sendMessage(util.translate("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        }


    }

    public void setupTasks() {
        KartMovementRunnable.startTask();
        KartAccelerationRunnable.startTask();
        KartBoostPadDelayRunnable.startTask();
        KartDriftXPBarRunnable.startTask();
        ScoreboardManager.startTask();
        DriftingHandler.setupTask();
    }

}
