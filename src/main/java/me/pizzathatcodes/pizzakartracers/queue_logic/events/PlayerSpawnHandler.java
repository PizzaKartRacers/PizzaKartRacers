package me.pizzathatcodes.pizzakartracers.queue_logic.events;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.spectatorSystem;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;

import javax.sound.sampled.FloatControl;

public class PlayerSpawnHandler {

    public static void handlePlayerSpawn(PlayerSpawnEvent event) {

        Player player = event.getPlayer();
        player.setInvulnerable(true);

        if(!Main.getGame().getStatus().equalsIgnoreCase("started")) {
            // TODO: Teleport player to the waiting room
//            Main.getMapSystem().teleportPlayerToWaitingRoom(player);
            Main.getQueue().addPlayer(player);

            GamePlayer gamePlayer = new GamePlayer(
                    player.getUuid(),
                    new Kart(0, 0)
            );
            Main.getGame().addPlayer(gamePlayer);

            gamePlayer.createKart();

            for(GamePlayer p : Main.getGame().getPlayers()) {
                Player player1 = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(p.getUuid());
                player1.sendMessage(util.translate(player.getUsername() + "&e has joined (&b" + (Main.getQueue().getPlayers().size()) + "&e/&b12&e)"));
            }
            util.sendTitle(player, util.translate("&6&lPizza Kart Racers"), util.translate("&f&lby &e&lPizzaThatCodes"), 10, 70, 20);
            player.setGameMode(GameMode.ADVENTURE);
            util.playCustomSound(player, "minecraft:announcer.welcome", Sound.Source.MASTER, 10F, 1F);


//            util.sendCustomSound(player, "minecraft:announcer.welcome");

        } else {
            // Put player spectating
            player.sendMessage(util.translate("&cThe game has already started! You are now a spectator."));
            spectatorSystem spectatorSystem = new spectatorSystem(player.getUuid(), Main.getGame());
            spectatorSystem.spectatorOn();
            Main.getGame().getSpectators().add(spectatorSystem);
        }

    }

}
