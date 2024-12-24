package me.pizzathatcodes.pizzakartracers.runnables.game;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GameState;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;

import java.time.temporal.TemporalUnit;

public class GameStartRunnable {

    public static Component message = util.translate("&cReady?");
    static Task gameStartTask;

    public static void startTask() {
        gameStartTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (GamePlayer gamePlayer : Main.getGame().getPlayers()) {
                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid());
                util.sendTitle(player, message, Component.empty(), 10, 20, 10);
                if (message.equals(util.translate("&cReady?"))) {
                    util.playCustomSound(player, "minecraft:announcer.ready", Sound.Source.MASTER, 10F, 1F);
                } else if (message.equals(util.translate("&eSet!"))) {
                    util.playCustomSound(player, "minecraft:announcer.set", Sound.Source.MASTER, 10F, 1F);
                } else if (message.equals(util.translate("&aGo!"))) {
                    util.playCustomSound(player, "minecraft:announcer.go", Sound.Source.MASTER, 10F, 1F);
                }
            }
            if (message.equals(util.translate("&cReady?"))) {
                message = util.translate("&eSet!");
            } else if (message.equals(util.translate("&eSet!"))) {
                message = util.translate("&aGo!");
            } else if (message.equals(util.translate("&aGo!"))) {
                message = util.translate("&aGo!");
                Main.getGame().setStatus(GameState.IN_GAME);
                gameStartTask.cancel();
            }
        }).repeat(TaskSchedule.tick(50)).delay(TaskSchedule.duration(8, TimeUnit.SECOND)).schedule(); // Runs every 50 ticks with a delay of 8 seconds
    }

}
