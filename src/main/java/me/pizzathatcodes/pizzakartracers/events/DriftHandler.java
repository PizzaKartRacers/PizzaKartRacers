package me.pizzathatcodes.pizzakartracers.events;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class DriftHandler implements Listener {

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUniqueId());
        if (gamePlayer == null) return;
        event.setCancelled(true);

        if(event.isSneaking()) {
            gamePlayer.getKart().setDrifting(true);
        } else {
            gamePlayer.getKart().setDrifting(false);
        }

    }

}
