package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.timer.TaskSchedule;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreboardManager {

    public static void startTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (GamePlayer player : Main.getGame().getPlayers()) {
                updatePlayerScoreboard(player);
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    public static void updatePlayerScoreboard(GamePlayer gamePlayer) {

        Sidebar sidebar = gamePlayer.getSidebar();
        if (sidebar == null) {
            sidebar = new Sidebar(util.translate("&e&lPizza Kart Racers"));
            gamePlayer.setSidebar(sidebar);
        }

        if(Main.getQueue() != null) {
            // TODO: setup queue lines
            handleQueueScoreboard(gamePlayer);
        } else {
            // TODO: setup game lines

        }

    }

    public static void handleQueueScoreboard(GamePlayer gamePlayer) {
        Sidebar sidebar = gamePlayer.getSidebar();
        sidebar.setTitle(util.translate("&e&lPizza Kart Racers"));

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String formattedDate = formatter.format(date);

        Sidebar.ScoreboardLine dateLine = new Sidebar.ScoreboardLine(
                "date",
                util.translate("&7" + formattedDate + " &8" + Main.getMapSystem().selectedMap.getMapName()),
                8
        );

        Sidebar.ScoreboardLine emptyLine = new Sidebar.ScoreboardLine(
                "empty",
                util.translate(""),
                7
        );

        Sidebar.ScoreboardLine mapLine = new Sidebar.ScoreboardLine(
                "map",
                util.translate("&fMap: &7" + Main.getMapSystem().selectedMap.getMapName()),
                6
        );

        Sidebar.ScoreboardLine playersLine = new Sidebar.ScoreboardLine(
                "players",
                util.translate("&fPlayers: &7" + Main.getQueue().getPlayers().size() + "/8"),
                5
        );

        Sidebar.ScoreboardLine emptyLine2 = new Sidebar.ScoreboardLine(
                "empty2",
                util.translate(""),
                4
        );

        if(Main.getQueue().getPlayers().size() < 4) {

            int neededAmountOfPlayers = 4 - Main.getQueue().getPlayers().size();
            Sidebar.ScoreboardLine startingInLine = new Sidebar.ScoreboardLine(
                    "startingIn",
                    util.translate("&fStarting in &a" + ((Main.getQueue().timeWaitLeft / 60) < 10 ? "0" + (Main.getQueue().timeWaitLeft / 60) : (Main.getQueue().timeWaitLeft / 60)) + ":" + ((Main.getQueue().timeWaitLeft % 60) < 10 ? "0" + (Main.getQueue().timeWaitLeft % 60) : (Main.getQueue().timeWaitLeft % 60)) + " &fif "),
                    3
            );

            Sidebar.ScoreboardLine allowTimeLine = new Sidebar.ScoreboardLine(
                    "allowTime",
                    util.translate("&a" + neededAmountOfPlayers + " &fmore " + (neededAmountOfPlayers > 1 ? "players join" : "player joins")),
                    2
            );

            Sidebar.ScoreboardLine emptyLine3 = new Sidebar.ScoreboardLine(
                    "empty3",
                    util.translate(""),
                    1
            );

            Sidebar.ScoreboardLine slimeworksLine = new Sidebar.ScoreboardLine(
                    "slimeworks",
                    util.translate("&eslimeworks.net"),
                    0
            );

            sidebar.createLine(dateLine);
            sidebar.createLine(emptyLine);
            sidebar.createLine(mapLine);
            sidebar.createLine(playersLine);
            sidebar.createLine(emptyLine2);
            sidebar.createLine(startingInLine);
            sidebar.createLine(allowTimeLine);
            sidebar.createLine(emptyLine3);
            sidebar.createLine(slimeworksLine);

        }

        sidebar.addViewer(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid()));
    }

}
