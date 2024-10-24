package me.pizzathatcodes.pizzakartracers.queue_logic;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.gameScoreboard;
import me.pizzathatcodes.pizzakartracers.queue_logic.classes.queueScoreboard;
import me.pizzathatcodes.pizzakartracers.queue_logic.events.PlayerJoinEvent;
import me.pizzathatcodes.pizzakartracers.queue_logic.events.PlayerLeaveEvent;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Random;

public class Queue {

    public ArrayList<Player> playerList = new ArrayList<>();
    public BukkitTask timerCountdownTask;
    public int timeWaitLeft;
    public String id;

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

    public Queue() {
        this.timeWaitLeft = 240;
        this.id = generateCode();

        Main.scoreboardTask = new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : getPlayers()) {
                    GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUniqueId());
                    if(gamePlayer == null) continue;
                    if(Main.getQueue() != null)
                        queueScoreboard.updateBoard(gamePlayer.getBoard());
                    else
                        gameScoreboard.updateBoard(gamePlayer.getBoard());
                }

            }
        }.runTaskTimer(Main.getInstance(), 0, 15L);

        timerCountdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                timeWaitLeft--;
                if(getPlayers().size() <= 1 && timeWaitLeft != 240) {
                    timeWaitLeft = 240;
                    return;
                }
                timer();

            }
        }.runTaskTimer(Main.getInstance(), 0, 20L);
    }

    public String getID() {
        return id;
    }

    public int getTimeWaitLeft() {
        return timeWaitLeft;
    }

    public void setTimeWaitLeft(int timeWaitLeft) {
        this.timeWaitLeft = timeWaitLeft;
    }

    public void removeTimeLeft(int removeTime) {
        timeWaitLeft -= removeTime;
    }

    public void addPlayer(Player player) {
        playerList.add(player);
    }

    public void removePlayer(Player player) {
        playerList.remove(player);
    }


    public void registerQueueEvents() {
        Main.getInstance().getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), Main.getInstance());
        Main.getInstance().getServer().getPluginManager().registerEvents(new PlayerJoinEvent(), Main.getInstance());
    }

    public ArrayList<Player> getPlayers() {
        return playerList;
    }

    public void timer() {

        if(playerList.size() == 4 && timeWaitLeft > 60) {
            timeWaitLeft = 60;
        }

        if(playerList.size() == 8 && timeWaitLeft > 30) {
            timeWaitLeft = 30;
        }

        if(playerList.size() == 12 && timeWaitLeft > 15) {
            timeWaitLeft = 15;
        }

        if (timeWaitLeft == 30) {
            for (Player player : playerList) {
                player.sendMessage(util.translate("&eThe game starts in &a30 &eseconds!"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
        }

        if (timeWaitLeft == 20) {
            for (Player player : playerList) {
                player.sendMessage(util.translate("&eThe game starts in &e20 &eseconds!"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
        }

        if (timeWaitLeft == 10) {
            for (Player player : playerList) {
                player.sendMessage(util.translate("&eThe game starts in &c10 &eseconds!"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
        }


        if (timeWaitLeft <= 5 && timeWaitLeft > 0) {
            for (Player player : playerList) {
                player.sendMessage(util.translate("&eThe game starts in &c" + timeWaitLeft + " &eseconds!"));
                player.sendTitle(util.translate("&c&l" + timeWaitLeft), "", 0, 20, 10);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
        }

        if (timeWaitLeft == 0) {

            if (playerList.size() < 2) {
                for (int i = 0; i < playerList.size(); i++) {
                    playerList.get(i).sendMessage(util.translate("Game didn't start due to not enough players!"));
                }
                timeWaitLeft = 240;
                return;
            }
            if (playerList.size() > 1) {

                Main.getGame().startGame();
                Main.setQueue(null);

            }
        }

    }
}
