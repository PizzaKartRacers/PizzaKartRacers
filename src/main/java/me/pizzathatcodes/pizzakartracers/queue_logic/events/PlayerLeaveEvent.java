package me.pizzathatcodes.pizzakartracers.queue_logic.events;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {

        Player player = e.getPlayer();

        GamePlayer queuePlayer = Main.getGame().getGamePlayer(player.getUniqueId());

        if(queuePlayer != null) {
            queuePlayer.getBoard().delete();
        }

        if(Main.getGame().getPlayers().contains(queuePlayer))
            Main.getGame().getPlayers().remove(queuePlayer);
        if(Main.getQueue().getPlayers().contains(player))
            Main.getQueue().getPlayers().remove(player);

        if(Main.getGame().getStatus().equals("waiting")) {
            e.setQuitMessage(util.translate(player.getName() + "&e has left (&b" + (Main.getQueue().getPlayers().size()) + "&e/&b12&e)"));
        }

    }

}
