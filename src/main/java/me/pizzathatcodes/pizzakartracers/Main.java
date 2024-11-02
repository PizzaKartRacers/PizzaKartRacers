package me.pizzathatcodes.pizzakartracers;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.objects.ServerGroupObject;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.pizzathatcodes.pizzakartracers.commands.setTimer;
import me.pizzathatcodes.pizzakartracers.events.DriftHandler;
import me.pizzathatcodes.pizzakartracers.events.PlayerLeaveEvent;
import me.pizzathatcodes.pizzakartracers.game_logic.Game;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.queue_logic.Queue;
import me.pizzathatcodes.pizzakartracers.queue_logic.classes.queueScoreboard;
import me.pizzathatcodes.pizzakartracers.startup_logic.mapSystem;
import me.pizzathatcodes.pizzakartracers.utils.configManager;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.slimeworksapi.SlimeWorksAPI;
import net.slimeworksapi.database.model.Games_Running;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static Game game;

    private static boolean disabling = false;

    private static SlimeWorksAPI slimeworksAPI;

    public static BukkitTask scoreboardTask;

    /**
     * @return The Slimeworks API instance
     */
    public static SlimeWorksAPI getSlimeworksAPI() {
        return slimeworksAPI;
    }

    public static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    public static Main getInstance() {
        return instance;
    }

    public static Game getGame() {
        return game;
    }

    public static Queue queue;

    public static Queue getQueue() {
        return queue;
    }

    public static void setQueue(Queue queue) {
        Main.queue = queue;
    }

    public static mapSystem map;

    @Override
    public void onEnable() {
        instance = this;

        util.setConfigFile(new configManager("config.yml"));
        if(!util.getConfigFile().getConfigFile().exists())
            Main.getInstance().saveResource("config.yml", false);

        util.getConfigFile().updateConfig(Arrays.asList());
        util.getConfigFile().saveConfig();
        util.getConfigFile().reloadConfig();

        util.setMessageFile(new configManager("messages.yml"));
        if(!util.getMessageFile().getConfigFile().exists())
            Main.getInstance().saveResource("messages.yml", false);

        util.getMessageFile().updateConfig(Arrays.asList());
        util.getMessageFile().saveConfig();
        util.getMessageFile().reloadConfig();

        util.setMapFile(new configManager("maps.yml"));
        if(!util.getMapFile().getConfigFile().exists())
            Main.getInstance().saveResource("maps.yml", false);

        util.getMapFile().updateConfig(Arrays.asList());
        util.getMapFile().saveConfig();
        util.getMapFile().reloadConfig();

        slimeworksAPI = new SlimeWorksAPI(this);

        getCommand("settimer").setExecutor(new setTimer());


        getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);
        getServer().getPluginManager().registerEvents(new DriftHandler(), this);

        queue = new Queue();
        getQueue().registerQueueEvents();

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if(game.getStatus().equalsIgnoreCase("starting")) return;
                Player player = event.getPlayer();
                if (player.getVehicle() instanceof ArmorStand) {
                    float forward = event.getPacket().getFloat().read(1);
                    float sideways = event.getPacket().getFloat().read(0);
                    GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUniqueId());
                    if(gamePlayer == null) return;
                    if(sideways != 0) {
                        float newSideways = sideways < 0 ? -1f : 1f;
                        float newYaw = gamePlayer.getKart().getKartEntity().getLocation().getYaw() + (newSideways * -7);  // Adjust yaw based on sideways input
                        gamePlayer.getKart().yaw = newYaw;

                        gamePlayer.getKart().getKartEntity().setRotation(gamePlayer.getKart().yaw, 0);  //
                    }
                    util.handleSidewayMovement(player, sideways);

                    if(forward != 0) {
                        if(forward > 0) {
                            gamePlayer.getKart().moving = "forward";
                        } else {
                            gamePlayer.getKart().moving = "backward";
                        }
                    } else {
                        gamePlayer.getKart().moving = "none";
                    }

                    if(sideways != 0) {
                        if(sideways > 0) {
                            gamePlayer.getKart().turning = "right";
                        } else {
                            gamePlayer.getKart().turning = "left";
                        }
                    } else {
                        gamePlayer.getKart().turning = "none";
                    }

                }
            }
        });


        game = new Game();
        game.setupTasks();

        if(map == null || !map.isMapLoading()) {
            map = new mapSystem();
            map.loadMap();
        }

        getLogger().info("FormulaKartRacers has been enabled!");


        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();

        Games_Running games_running = new Games_Running(
                server.getName(),
                "pizzakartracers",
                new ArrayList<>(),
                "waiting"
        );

        getSlimeworksAPI().getGameRunningDatabase().createInformation(games_running);
        getLogger().info("Created game data for server " + server.getName());

    }

    public static boolean isDisabling() {
        return disabling;
    }

    @Override
    public void onDisable() {
        disabling = true;
        while (Main.getGame().getPlayers().size() > 0) {
            GamePlayer gamePlayer = Main.getGame().getPlayers().get(0);
            Bukkit.getPlayer(gamePlayer.getUuid()).eject();
            Bukkit.getPlayer(gamePlayer.getUuid()).leaveVehicle();
            gamePlayer.getKart().getKartEntity().remove();
            gamePlayer.getKart().stopAllTasks();
            Main.getGame().removePlayer(gamePlayer);
            getLogger().info("Removed player " + gamePlayer.getUuid());
        }

        for(Player player : Bukkit.getOnlinePlayers()) {
            for(ServerObject goober_lobby : TimoCloudAPI.getUniversalAPI().getServerGroup("Lobby").getServers()) {
                if(goober_lobby.getOnlinePlayerCount() < goober_lobby.getMaxPlayerCount()) {
                    TimoCloudAPI.getUniversalAPI().getPlayer(player.getUniqueId()).sendToServer(goober_lobby);
                    break;
                }
            }
        }

        ServerObject server = TimoCloudAPI.getBukkitAPI().getThisServer();
        Games_Running games_running = getSlimeworksAPI().getGameRunningDatabase().findGameDataByID(server.getName());
        if(games_running != null) {
            getSlimeworksAPI().getGameRunningDatabase().deleteInformation(games_running);
        }
        ServerGroupObject pizzakartracers = TimoCloudAPI.getUniversalAPI().getServerGroup("pizzakartracers");
        if(pizzakartracers.getOnlineAmount() > 0)
            pizzakartracers.setOnlineAmount(pizzakartracers.getOnlineAmount() - 1);


        getLogger().info("FormulaKartRacers has been disabled!");
        game = null;



    }
}
