package me.pizzathatcodes.pizzakartracers;

import me.pizzathatcodes.pizzakartracers.game_logic.Game;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.queue_logic.Queue;
import me.pizzathatcodes.pizzakartracers.startup_logic.mapSystem;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket;
import net.minestom.server.timer.SchedulerManager;

import java.util.Set;

public final class Main {

    private static MinecraftServer instance;
    private static Game game;
    private static Queue queue;
    private static mapSystem map;

    private static SchedulerManager scheduler;
    private static GlobalEventHandler globalEventHandler;

    /**
     * Get the server instance
     * @return The server instance
     */
    public static MinecraftServer getInstance() {
        return instance;
    }

    /**
     * Get the scheduler manager
     * @return The scheduler manager
     */
    public static SchedulerManager getScheduler() {
        return scheduler;
    }

    /**
     * Get the map system
     * @return The map system
     */
    public static mapSystem getMapSystem() {
        return map;
    }

    public static Game getGame() {
        return game;
    }

    public static Queue getQueue() {
        return queue;
    }
    public static void setQueue(Queue queue) {
        Main.queue = queue;
    }

    public static GlobalEventHandler getGlobalEventHandler() {
        return globalEventHandler;
    }

    /**
     * Initialize the server
     * @return The server instance
     */
    public static void main(String[] args) {
        // Initialization
        instance = MinecraftServer.init();
        scheduler = MinecraftServer.getSchedulerManager();

        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Set the ChunkGenerator
        map = new mapSystem();
//        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        // Add an event callback to specify the spawning instance (and the spawn position)
        globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(map.getMapInstance());
            player.setRespawnPoint(getMapSystem().getWaitingRoomSpawnLocation());
        });

//        MojangAuth.init();
        BungeeCordProxy.enable();
        BungeeCordProxy.setBungeeGuardTokens(Set.of("BUNGEEGUARDTOKEN"));

        queue = new Queue();
        getQueue().registerQueueEvents();


        game = new Game();
        game.setupTasks();



        MinecraftServer.getPacketListenerManager().setListener(ClientSteerVehiclePacket.class, (packet, player) -> {
            if(game.getStatus().equalsIgnoreCase("starting")) return;
            GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUuid());
            if(gamePlayer == null) return;
            if(packet.sideways() != 0) {
                float newSideways = packet.sideways() < 0 ? -1f : 1f;
                float newYaw = gamePlayer.getKart().getKartEntity().getPosition().yaw() + (newSideways * -7);  // Adjust yaw based on sideways input


                gamePlayer.getKart().getKartEntity().setView(newYaw, 0);
            }
            util.handleSidewayMovement(player, packet.sideways());

            if(packet.forward() != 0) {
                // TODO: Properly handle the movement of the karts

                if(packet.forward() > 0) {
                    gamePlayer.getKart().moving = "forward";
                } else {
                    gamePlayer.getKart().moving = "backward";
                }
            } else {
                gamePlayer.getKart().moving = "none";
            }

            if(packet.sideways() != 0) {
                if(packet.sideways() > 0) {
                    gamePlayer.getKart().turning = "right";
                } else {
                    gamePlayer.getKart().turning = "left";
                }
            } else {
                gamePlayer.getKart().turning = "none";
            }

        });



        // Start the server on port 25565
        instance.start("0.0.0.0", 25565);
    }

}
