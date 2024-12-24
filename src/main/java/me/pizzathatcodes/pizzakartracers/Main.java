package me.pizzathatcodes.pizzakartracers;

import me.pizzathatcodes.pizzakartracers.commands.pingComamnd;
import me.pizzathatcodes.pizzakartracers.commands.setTimerCommand;
import me.pizzathatcodes.pizzakartracers.commands.stopCommand;
import me.pizzathatcodes.pizzakartracers.game_logic.Game;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GameState;
import me.pizzathatcodes.pizzakartracers.queue_logic.Queue;
import me.pizzathatcodes.pizzakartracers.startup_logic.mapSystem;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.packet.client.play.ClientInputPacket;
import net.minestom.server.timer.SchedulerManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public static void main(String[] args) throws IOException {
        // Initialization
        instance = MinecraftServer.init();
        scheduler = MinecraftServer.getSchedulerManager();

        MinecraftServer.getCommandManager().register(new setTimerCommand());
        MinecraftServer.getCommandManager().register(new pingComamnd());

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
        String content = new String(Files.readAllBytes(Paths.get("bungeeGuardToken.yml")));
        BungeeCordProxy.setBungeeGuardTokens(Set.of(content));

        queue = new Queue();
        getQueue().registerQueueEvents();

        MinecraftServer.getCommandManager().register(new stopCommand());


        game = new Game();
        game.setupTasks();


        MinecraftServer.getPacketListenerManager().setListener(ClientInputPacket.class, (packet, player) -> {
            if(game.getStatus().equals(GameState.STARTING)) return;
            GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUuid());
            if(gamePlayer == null) return;
            if(gamePlayer.getKart().isSpunOut()) return;
            if(packet.left() || packet.right()) {
//                float newSideways = packet.sideways() < 0 ? -1f : 1f;
                float newSideways = packet.left() ? -1f : 1f;
                float newYaw = gamePlayer.getKart().getKartEntity().getPosition().yaw() + (newSideways * -6);  // Adjust yaw based on sideways input


                gamePlayer.getKart().getKartEntity().setView(newYaw, 0);
            }
            util.handleSidewayMovement(player, packet);

            if(packet.forward() || packet.backward()) {
                // TODO: Properly handle the movement of the karts

                if(packet.forward()) {
                    gamePlayer.getKart().moving = "forward";
                } else {
                    gamePlayer.getKart().moving = "backward";
                }
            } else {
                gamePlayer.getKart().moving = "none";
            }

            if (packet.shift()) {
                gamePlayer.getKart().drifting = true;
            } else {
                gamePlayer.getKart().drifting = false;
            }


            if(packet.left() || packet.right()) {
                if(gamePlayer.getKart().getDriftTicks() < 7 && gamePlayer.getKart().isDrifting()) {
                    if(gamePlayer.getKart().driftDelay == 0) {
                        gamePlayer.getKart().driftDelay++;
                        gamePlayer.getKart().setDriftTicks(gamePlayer.getKart().getDriftTicks() + 1);
                        util.playCustomSound(player, "minecraft:block.stone_pressure_plate.click_on", Sound.Source.MASTER, 10F, 1F);
                    }
                }
                if(packet.right()) {
                    gamePlayer.getKart().turning = "right";
                } else {
                    gamePlayer.getKart().turning = "left";
                }
            } else {
                gamePlayer.getKart().turning = "none";
            }


        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            MinecraftServer.stopCleanly();
        }));

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            System.out.println("shutdown hook");
        });


        // Start the server on port 25565
        instance.start("0.0.0.0", 25565);
    }

}
