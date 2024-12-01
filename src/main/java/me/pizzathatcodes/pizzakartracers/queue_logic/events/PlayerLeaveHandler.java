package me.pizzathatcodes.pizzakartracers.queue_logic.events;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;

public class PlayerLeaveHandler {

    public static void handlePlayerLeave(PlayerDisconnectEvent event) {
        Player player = event.getPlayer();

        GamePlayer queuePlayer = Main.getGame().getGamePlayer(player.getUuid());
        if(queuePlayer == null) return;

        queuePlayer.getKart().getKartEntity().remove();
        Main.getGame().getPlayers().remove(queuePlayer);

        if(Main.getGame().getStatus().equals("waiting")) {
            if(Main.getQueue().getPlayers().contains(player))
                Main.getQueue().getPlayers().remove(player);
            for(GamePlayer p : Main.getGame().getPlayers()) {
                Player player1 = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(p.getUuid());
                player1.sendMessage(util.translate(player.getUsername() + "&e has left (&b" + (Main.getQueue().getPlayers().size()) + "&e/&b12&e)"));
            }
        }

    }

}
