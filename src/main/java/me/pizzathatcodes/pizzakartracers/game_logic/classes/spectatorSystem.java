package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import me.pizzathatcodes.pizzakartracers.game_logic.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class spectatorSystem {

    UUID playerUUID;
    Game gameSystem;

    /**
     * Create a new spectatorSystem
     * @param player Player UUID
     * @param gameSystem gameSystem
     */
    public spectatorSystem(UUID player, Game gameSystem) {
        this.playerUUID = player;
        this.gameSystem = gameSystem;
    }

    /**
     * Get the player UUID
     * @return Player UUID
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Set the player UUID
     * @param playerUUID Player UUID
     */
    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    /**
     * Turn's on spectator mode
     */
    public void spectatorOn() {

        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUUID);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlying(true);
        player.setFlying(true);

        for(GamePlayer playerGameSystem : gameSystem.getPlayers()) {
//            if(!playerGameSystem.isOnline()) continue;
//            MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerGameSystem.getUuid()).hidePlayer(player);
            player.removeViewer(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerGameSystem.getUuid()));
        }
    }

    /**
     * Turn's off spectator mode
     */
    public void spectatorOff() {

        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUUID);
        player.setAllowFlying(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);

        for(GamePlayer playerGameSystem : gameSystem.getPlayers()) {
//            if(!playerGameSystem.isOnline()) continue;
//            Bukkit.getPlayer(playerGameSystem.getUuid()).showPlayer(Bukkit.getPlayer(playerUUID));
            player.addViewer(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerGameSystem.getUuid()));
        }
    }


}
