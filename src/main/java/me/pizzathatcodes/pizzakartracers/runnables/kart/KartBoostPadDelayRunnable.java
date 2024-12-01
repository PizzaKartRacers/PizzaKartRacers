package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

public class KartBoostPadDelayRunnable {

    public static void startTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {

            for(GamePlayer gamePlayer : Main.getGame().getPlayers()) {
                if (gamePlayer.getKart().boostPadDelay > 0)
                    gamePlayer.getKart().boostPadDelay--;
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

}
