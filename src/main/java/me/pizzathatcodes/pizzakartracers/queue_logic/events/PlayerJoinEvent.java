package me.pizzathatcodes.pizzakartracers.queue_logic.events;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import fr.mrmicky.fastboard.FastBoard;
import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.slimeworksapi.database.model.Games_Running;
import org.bukkit.GameMode;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;

public class PlayerJoinEvent implements Listener {

    // Cache Games_Running data to avoid redundant database hits
    private Games_Running cachedGameRunning;

    @EventHandler
    public void onPrePlayerJoin(AsyncPlayerPreLoginEvent event) {
        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();

        // Load the game data asynchronously to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            cachedGameRunning = Main.getSlimeworksAPI().getGameRunningDatabase().findGameDataByID(server.getName());
        }).thenRun(() -> {
            if (cachedGameRunning.getStatus().equalsIgnoreCase("running") && !cachedGameRunning.getPlayers().contains(event.getUniqueId())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "You do not have permission to join!");
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        Player player = e.getPlayer();
        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();
        player.setInvulnerable(true);

        // Process game data asynchronously
        CompletableFuture.runAsync(() -> {
            cachedGameRunning = Main.getSlimeworksAPI().getGameRunningDatabase().findGameDataByID(server.getName());

            if (cachedGameRunning.getStatus().equalsIgnoreCase("waiting")) {
                // Safely update player list on the main thread
                Main.getQueue().addPlayer(player);
                cachedGameRunning.addPlayer(player.getUniqueId());
                Main.getSlimeworksAPI().getGameRunningDatabase().updateInformation(cachedGameRunning);

                // Teleport player to the waiting room
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.map.teleportPlayerToWaitingRoom(player);
                    }
                }.runTask(Main.getInstance());

                // Initialize game player and kart asynchronously to reduce delay
                CompletableFuture.runAsync(() -> {
                    FastBoard board = new FastBoard(player) {
                        @Override
                        public boolean hasLinesMaxLength() {
                            return Via.getAPI().getPlayerVersion(getPlayer()) < ProtocolVersion.v1_13.getVersion();
                        }
                    };
                    board.updateTitle(util.translate("&e&lPizza Kart Racers"));

                    GamePlayer gamePlayer = new GamePlayer(
                            player.getUniqueId(),
                            new Kart(0, 0, 0),
                            board
                    );

                    Main.getGame().addPlayer(gamePlayer);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gamePlayer.createKart();
                        }
                    }.runTaskLater(Main.getInstance(), 5L);

//                    player.playSound(player.getLocation(), "minecraft:announcer.welcome", SoundCategory.MASTER, 10F, 1F);

                });
            }
        });

        // Set the player's join message
        e.setJoinMessage(util.translate(player.getName() + "&e has joined (&b" + (Main.getQueue().getPlayers().size() + 1) + "&e/&b12&e)"));
        player.sendTitle(util.translate("&6&lPizza Kart Racers"), util.translate("&f&lby &e&lPizzaThatCodes"), 10, 70, 20);
        player.setGameMode(GameMode.ADVENTURE);


        util.sendCustomSound(player, "minecraft:announcer.welcome");
    }
}
