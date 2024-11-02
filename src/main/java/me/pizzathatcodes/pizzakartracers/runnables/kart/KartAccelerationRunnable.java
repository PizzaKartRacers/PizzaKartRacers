package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class KartAccelerationRunnable extends BukkitRunnable {

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
        for (GamePlayer gamePlayer : Main.getGame().getPlayers()) {
            Kart kart = gamePlayer.getKart();
            String moving = kart.moving;
            String turning = kart.turning;

            ArmorStand armorStand = gamePlayer.getKart().getKartEntity();
            Location loc = armorStand.getLocation();
            float yaw = loc.getYaw();
            double radiansYaw = Math.toRadians(yaw);
            Vector direction = new Vector(-Math.sin(radiansYaw), 0, Math.cos(radiansYaw));


            // TODO: This code generally works, just has the issue with being able to climb up walls
            Block blockBelow = loc.clone().add(0, -0.5, 0).getBlock();
            double blockBelowHeight = blockBelow.getBoundingBox().getHeight();

            Location blockInFrontLoc = loc.clone().add(direction.clone().multiply(1));
            Block blockInFront = blockInFrontLoc.getBlock();
            double blockInFrontHeight = blockInFront.getBoundingBox().getHeight();
            Vector velocity = gamePlayer.getKart().getKartEntity().getVelocity();

            if (blockInFront.getType().isSolid()) {
                // If there is a slab or stair directly in front, climb up slightly
                if (blockInFrontHeight < 1.0) {
                    velocity.setY(velocity.getY() + 0.5);
                }
                // If on a stair/slab and a full block is in front, climb the full block
                else if (blockBelowHeight < 1.0 && blockInFrontHeight >= 1.0) {
                    velocity.setY(velocity.getY() + 1.0);
                }
            } else if (blockBelow.getType().isAir()) {
                // Simulate falling if there's no block below
                velocity.setY(velocity.getY() - 2);
            }


            // Update the velocity of the kart
            gamePlayer.getKart().getKartEntity().setVelocity(velocity);


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

}
