package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;

public class KartMovementRunnable {

    public static void startTask() {
        SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();

        schedulerManager.buildTask(() -> {
            for (GamePlayer gamePlayer : Main.getGame().getPlayers()) {
                if (gamePlayer == null) {
                    continue;
                }

                // Handle kart movement
                if (gamePlayer.getKart().getAcceleration() != 0) {
                    gamePlayer.getKart().move();
                }

                // Update player "level" equivalent (can be simulated via custom logic)
                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid());
                if (player != null) {
                    int level = Math.abs(gamePlayer.getKart().getAcceleration());
                    player.setLevel(level); // Example: show acceleration in XP bar
//                    player.sendActionBar(Component.text("Speed Level: " + level)); // Example: show acceleration in ActionBar
                }
            }
        }).repeat(TaskSchedule.tick(1)).schedule(); // Run every tick (adjust as needed)
    }

}
