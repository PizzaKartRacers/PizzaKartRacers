package me.pizzathatcodes.pizzakartracers.runnables.game;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GameStartRunnable extends BukkitRunnable {
    public String message = util.translate("&cReady?");
    @Override
    public void run() {
        for(GamePlayer gamePlayer : Main.getGame().getPlayers()) {
            Player player = Bukkit.getPlayer(gamePlayer.getUuid());
//            player.sendMessage("Kart's velocity: " + gamePlayer.getKart().getKartEntity().getVelocity());
//            player.sendMessage("Kart's acceleration: " + gamePlayer.getKart().getAcceleration());
//            player.sendMessage("Kart's moving: " + gamePlayer.getKart().moving);
//            player.sendMessage("Kart's turning: " + gamePlayer.getKart().turning);
            player.sendTitle(message, "", 10, 20, 10);
            if(message.equals(util.translate("&cReady?"))) {
                player.playSound(player.getLocation(), "announcer.ready", 10, 1);
            } else if (message.equals(util.translate("&eSet!"))) {
                player.playSound(player.getLocation(), "announcer.set", 10, 1);
            } else if (message.equals(util.translate("&aGo!"))) {
                player.playSound(player.getLocation(), "announcer.go", 10, 1);

            }
        }
        if(message.equals(util.translate("&cReady?"))) {
            message = util.translate("&eSet!");
        } else if(message.equals(util.translate("&eSet!"))) {
            message = util.translate("&aGo!");
        } else if(message.equals(util.translate("&aGo!"))) {
            message = util.translate("&aGo!");
            Main.getGame().setStatus("started");
            cancel();
        }
    }
}
