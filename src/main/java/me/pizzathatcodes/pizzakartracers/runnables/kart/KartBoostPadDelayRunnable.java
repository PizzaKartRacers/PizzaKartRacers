package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class KartBoostPadDelayRunnable extends BukkitRunnable {
    @Override
    public void run() {
        for(GamePlayer gamePlayer : Main.getGame().getPlayers()) {
            if (gamePlayer.getKart().boostPadDelay > 0)
                gamePlayer.getKart().boostPadDelay--;
        }
    }
}
