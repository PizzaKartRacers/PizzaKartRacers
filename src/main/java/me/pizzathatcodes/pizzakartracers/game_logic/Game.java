package me.pizzathatcodes.pizzakartracers.game_logic;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Game {

    private ArrayList<GamePlayer> players;
    private String status;

    public Game() {
        players = new ArrayList<>();
        status = "waiting";
    }



    public ArrayList<GamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(GamePlayer player) {
        players.add(player);
    }

    public void removePlayer(GamePlayer player) {
        players.remove(player);
    }

    public GamePlayer getGamePlayer(UUID uuid) {
        return players.stream().filter(gamePlayer -> gamePlayer.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public GamePlayer findGamePlayerFromKart(Kart kart) {
        return players.stream().filter(gamePlayer -> gamePlayer.getKart().equals(kart)).findFirst().orElse(null);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void startGame() {

        // TODO: Add wait logic so people can't drive off when the game starts
        setStatus("starting");
        gameStartingLogic();

        for(GamePlayer gamePlayer : players) {
            Player player = Bukkit.getPlayer(gamePlayer.getUuid());
            Main.map.teleportPlayerToGame(player);
            gamePlayer.getKart().setAcceleration(0);
            player.sendMessage(util.translate("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            player.sendMessage(util.translate("&f&l                       Pizza Kart Racers                       "));
            player.sendMessage(util.translate("&f&l                                                               "));
            player.sendMessage(util.translate("&f&l             &e&lRace your friends to the finish line.             "));
            player.sendMessage(util.translate("&f&l              &e&lGrab items on the way and use them               "));
            player.sendMessage(util.translate("&f&l                 &e&lto get ahead of everyone else!                 "));
            player.sendMessage(util.translate("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        }


    }


    public BukkitTask kartMovementTask;
    public BukkitTask kartBoostPadDelayTask;
    public BukkitTask kartParticleTask;
    public void setupTasks() {
        // This handles the kart movement
        kartMovementTask = new BukkitRunnable() {
            @Override
            public void run() {

                for(GamePlayer gamePlayer : players) {
                    if (gamePlayer == null) {
                        continue;
                    }
                    if (gamePlayer.getKart().getAcceleration() != 0) {
                        gamePlayer.getKart().move();
                    }
                    int level = gamePlayer.getKart().getAcceleration() < 0 ? gamePlayer.getKart().getAcceleration() * -1 : gamePlayer.getKart().getAcceleration();
                    Bukkit.getPlayer(gamePlayer.getUuid()).setLevel(level);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);

        // This Handles the boost pad delay
        kartBoostPadDelayTask = new BukkitRunnable() {
            @Override
            public void run() {
                for(GamePlayer gamePlayer : players) {
                    if (gamePlayer.getKart().boostPadDelay > 0)
                        gamePlayer.getKart().boostPadDelay--;
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

        // This handles the kart particle effects
        kartParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for(GamePlayer gamePlayer : players) {
                    if (gamePlayer.getKart().getKartEntity() == null) {
                        continue;
                    }

                    Location loc = gamePlayer.getKart().getKartEntity().getLocation();
                    World world = loc.getWorld();

                    // Clone location and adjust it to be behind the entity
                    Location behind = loc.clone();

                    // Get the yaw in radians
                    double yaw = Math.toRadians(loc.getYaw());

                    // Calculate the offset behind the player using trigonometry
                    double xOffset = -Math.sin(yaw) * -1.7;
                    double zOffset = Math.cos(yaw) * -1.7;

                    // Apply the offset
                    behind.add(xOffset, 0, zOffset);

                    // Spawn the particles
                    for (int i = 0; i < 5; i++) {
                        world.spawnParticle(Particle.EXPLOSION_NORMAL, behind.clone().add(0, 2, 0), 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 5);

        kartMovementTask = new BukkitRunnable() {
            private final int MAX_ACCELERATION = 65;
            private final int MIN_ACCELERATION = -65;
            private final int ACCELERATION_INCREMENT = 2;
            private final int BRAKING_FORCE = 5;
            private final int DECELERATION_RATE = 3;
            private final int TURN_DECELERATION_RATE = 1;
            private final int TICKS_TO_TURN_DECELERATION = 10;
            private final int MIN_TURNING_SPEED = 57;
            private final int ACCELERATION_SLOWDOWN_AMOUNT = 57;

            private HashMap<GamePlayer, Integer> TICKS_TO_NEXT_TURN_DECELERATION = new HashMap<>();
            private HashMap<GamePlayer, Integer> TICKS_TO_NEXT_SPEED_CHANGE = new HashMap<>();
            private final int TICKS_TO_SPEED_CHANGE = 10;

            @Override
            public void run() {
                for (GamePlayer gamePlayer : players) {
                    Kart kart = gamePlayer.getKart();
                    String moving = kart.moving;
                    String turning = kart.turning;

                    boolean isTurning = "left".equals(turning) || "right".equals(turning);

                    // Initialize tick maps for players if they don't already exist
                    TICKS_TO_NEXT_TURN_DECELERATION.putIfAbsent(gamePlayer, 0);
                    TICKS_TO_NEXT_SPEED_CHANGE.putIfAbsent(gamePlayer, 0);

                    // Step 1: Handle movement and turning with speed restrictions
                    if (isTurning) {
                        // Gradually reduce speed to the minimum turning speed (57) if it's above that
                        if (kart.getAcceleration() > MIN_TURNING_SPEED || kart.getAcceleration() < -MIN_TURNING_SPEED) {
                            if (TICKS_TO_NEXT_TURN_DECELERATION.get(gamePlayer) >= TICKS_TO_TURN_DECELERATION) {
                                TICKS_TO_NEXT_TURN_DECELERATION.put(gamePlayer, 0);

                                // Decelerate the kart while turning and moving
                                if (kart.getAcceleration() > MIN_TURNING_SPEED) {
                                    kart.acceleration = Math.max(MIN_TURNING_SPEED, kart.getAcceleration() - TURN_DECELERATION_RATE);
                                } else if (kart.getAcceleration() < -MIN_TURNING_SPEED) {
                                    kart.acceleration = Math.min(-MIN_TURNING_SPEED, kart.getAcceleration() + TURN_DECELERATION_RATE);
                                }
                            } else {
                                TICKS_TO_NEXT_TURN_DECELERATION.put(gamePlayer, TICKS_TO_NEXT_TURN_DECELERATION.get(gamePlayer) + 1);
                            }
                        }
                    }

                    // Step 2: Handle acceleration and braking based on movement
                    if (!"none".equals(moving)) {
                        if ("forward".equals(moving)) {
                            // If moving forward, apply acceleration but respect the turning speed limit
                            if (kart.getAcceleration() < 0) {
                                kart.acceleration += BRAKING_FORCE; // Braking if switching from reverse to forward
                            } else if (kart.getAcceleration() < MAX_ACCELERATION) {
                                if (!isTurning || kart.getAcceleration() < MIN_TURNING_SPEED) {  // Only accelerate if not turning or below turning speed
                                    if(kart.getAcceleration() >= ACCELERATION_SLOWDOWN_AMOUNT) {
                                        if(TICKS_TO_NEXT_SPEED_CHANGE.get(gamePlayer) == TICKS_TO_SPEED_CHANGE) {
                                            TICKS_TO_NEXT_SPEED_CHANGE.put(gamePlayer, 0);
                                            kart.acceleration += 1;
                                        } else {
                                            TICKS_TO_NEXT_SPEED_CHANGE.put(gamePlayer, TICKS_TO_NEXT_SPEED_CHANGE.get(gamePlayer) + 1);
                                        }
                                    } else {
                                        kart.acceleration += ACCELERATION_INCREMENT;
                                    }
                                }
                            }
                        } else if ("backward".equals(moving)) {
                            // If moving backward, apply reverse acceleration but respect the turning speed limit
                            if (kart.getAcceleration() > 0) {
                                kart.acceleration -= BRAKING_FORCE;  // Braking if switching from forward to reverse
                            } else if (kart.getAcceleration() > MIN_ACCELERATION) {
                                if (!isTurning || kart.getAcceleration() > -MIN_TURNING_SPEED) {  // Only accelerate in reverse if not turning or above reverse turning speed
                                    if(kart.getAcceleration() <= -ACCELERATION_SLOWDOWN_AMOUNT) {
                                        if(TICKS_TO_NEXT_SPEED_CHANGE.get(gamePlayer) == TICKS_TO_SPEED_CHANGE) {
                                            TICKS_TO_NEXT_SPEED_CHANGE.put(gamePlayer, 0);
                                            kart.acceleration -= 1;
                                        } else {
                                            TICKS_TO_NEXT_SPEED_CHANGE.put(gamePlayer, TICKS_TO_NEXT_SPEED_CHANGE.get(gamePlayer) + 1);
                                        }
                                    } else {
                                        kart.acceleration -= ACCELERATION_INCREMENT;
                                    }
                                }
                            }
                        }
                    } else {
                        // Decelerate when no input is given
                        if (kart.getAcceleration() > 0) {
                            kart.acceleration = Math.max(0, kart.getAcceleration() - DECELERATION_RATE);
                        } else if (kart.getAcceleration() < 0) {
                            kart.acceleration = Math.min(0, kart.getAcceleration() + DECELERATION_RATE);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);


    }


    public void gameStartingLogic() {

        // TODO: Handle the announcer and the
        // &cReady? &eSet! &aGo!

        new BukkitRunnable() {
            public String message = util.translate("&cReady?");
            @Override
            public void run() {
                for(GamePlayer gamePlayer : players) {
                    Player player = Bukkit.getPlayer(gamePlayer.getUuid());
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
                    setStatus("started");
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20 * 8,50);

    }

}
