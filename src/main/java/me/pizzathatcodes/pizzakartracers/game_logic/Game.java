package me.pizzathatcodes.pizzakartracers.game_logic;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.runnables.game.GameStartRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartAccelerationRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartBoostPadDelayRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartMovementRunnable;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartParticleRunnable;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Game {

    private ArrayList<GamePlayer> players;
    private String status;

    public Game() {
        players = new ArrayList<>();
        status = "waiting";
    }



    public ArrayList<GamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(GamePlayer player) {
        players.add(player);
    }

    public void removePlayer(GamePlayer player) {
        players.remove(player);
    }

    public GamePlayer getGamePlayer(UUID uuid) {
        return players.stream().filter(gamePlayer -> gamePlayer.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public GamePlayer findGamePlayerFromKart(Kart kart) {
        return players.stream().filter(gamePlayer -> gamePlayer.getKart().equals(kart)).findFirst().orElse(null);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void startGame() {

        // TODO: Add wait logic so people can't drive off when the game starts
        setStatus("starting");
        gameStartingLogic();

        for(GamePlayer gamePlayer : players) {
            Player player = Bukkit.getPlayer(gamePlayer.getUuid());
            gamePlayer.getKart().moving = "none";
            gamePlayer.getKart().turning = "none";
            gamePlayer.getKart().setAcceleration(0);
            gamePlayer.getKart().getKartEntity().setVelocity(new Vector());
            Main.map.teleportPlayerToGame(player);
            player.sendMessage(util.translate("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            player.sendMessage(util.translate("&f&l                       Pizza Kart Racers                       "));
            player.sendMessage(util.translate("&f&l                                                               "));
            player.sendMessage(util.translate("&f&l             &e&lRace your friends to the finish line.             "));
            player.sendMessage(util.translate("&f&l              &e&lGrab items on the way and use them               "));
            player.sendMessage(util.translate("&f&l                 &e&lto get ahead of everyone else!                 "));
            player.sendMessage(util.translate("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        }


    }


    public BukkitTask kartMovementTask;
    public BukkitTask kartBoostPadDelayTask;
    public BukkitTask kartParticleTask;
    public BukkitTask kartAccelerationTask;
    public void setupTasks() {
        // This handles the kart movement
        kartMovementTask = new KartMovementRunnable().runTaskTimer(Main.getInstance(), 0, 1);
        // This Handles the boost pad delay
        kartBoostPadDelayTask = new KartBoostPadDelayRunnable().runTaskTimer(Main.getInstance(), 0, 20);
        // This handles the kart particle effects
        kartParticleTask = new KartParticleRunnable().runTaskTimer(Main.getInstance(), 0, 5);
        // This handles the kart acceleration
        kartAccelerationTask = new KartAccelerationRunnable().runTaskTimer(Main.getInstance(), 0, 1);

    }


    public void gameStartingLogic() {
        new GameStartRunnable().runTaskTimer(Main.getInstance(), 20 * 8,50);
    }

}
