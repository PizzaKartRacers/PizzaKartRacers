package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class KartMovementRunnable extends BukkitRunnable {
    @Override
    public void run() {
        for(GamePlayer gamePlayer : Main.getGame().getPlayers()) {
            if (gamePlayer == null) {
                continue;
            }
            if (gamePlayer.getKart().getAcceleration() != 0) {
                gamePlayer.getKart().move();
            }
            int level = gamePlayer.getKart().getAcceleration() < 0 ? gamePlayer.getKart().getAcceleration() * -1 : gamePlayer.getKart().getAcceleration();
            Bukkit.getPlayer(gamePlayer.getUuid()).setLevel(level);
        }
    }
}
