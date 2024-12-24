package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;

public class KartDriftXPBarRunnable {

    public static void startTask() {
        SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();

        schedulerManager.buildTask(() -> {
            for(GamePlayer gamePlayer : Main.getGame().getPlayers()) {
                if(gamePlayer.getKart().driftDelay > 0) {
                    gamePlayer.getKart().driftDelay--;
                }
            }
        }).repeat(TaskSchedule.tick(3)).schedule();

        schedulerManager.buildTask(() -> {
            for (GamePlayer gamePlayer : Main.getGame().getPlayers()) {
                if (gamePlayer == null) {
                    continue;
                }

                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid());
                if(player == null) {
                    continue;
                }
                if(player.getExp() > 0) {
                    if(player.getExp() - 0.02f < 0) {
                        player.setExp(0);
                    } else {
                        player.setExp(player.getExp() - 0.02f);
                    }
                }
            }
        }).repeat(TaskSchedule.tick(5)).schedule(); // Run every tick (adjust as needed)
    }

}
