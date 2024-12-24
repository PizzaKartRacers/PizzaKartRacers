package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.HashMap;

public class KartAccelerationRunnable {

    private static final int MAX_ACCELERATION = 65;
    private static final int MIN_ACCELERATION = -65;
    private static final int ACCELERATION_INCREMENT = 2;
    private static final int DECELERATION_RATE = 3;
    private static final int TURN_DECELERATION_RATE = 1;
    private static final int TICKS_TO_TURN_DECELERATION = 10;
    private static final int MIN_TURNING_SPEED = 57;

    private static final HashMap<GamePlayer, Integer> ticksToNextTurnDeceleration = new HashMap<>();
    public static ArrayList<GamePlayer> isTurningList = new ArrayList<>();

    public static void startTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (GamePlayer gamePlayer : Main.getGame().getPlayers()) {
//                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid());
//                player.sendMessage("Your current acceleration is: " + gamePlayer.getKart().getAcceleration());
                Kart kart = gamePlayer.getKart();
                if (kart == null || kart.getKartEntity() == null) continue;

                Entity kartEntity = kart.getKartEntity();
                String moving = kart.moving;
                String turning = kart.turning;

                if("left".equals(turning) || "right".equals(turning)) {
                    if(!isTurningList.contains(gamePlayer))
                        isTurningList.add(gamePlayer);
                } else {
                    if(isTurningList.contains(gamePlayer))
                        isTurningList.remove(gamePlayer);
                }


                handleBlockInteractions(kart);
                handleTurning(gamePlayer, kart, turning);
                handleAcceleration(kart, moving);
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    public static void handleBlockInteractions(Kart kart) {
        // Get the kart's current position
        var kartPosition = util.copyPosition(kart.getKartEntity().getPosition());
        var instance = kart.getKartEntity().getInstance();

        // Determine the block in front of the kart
        // Calculate the forward direction based on the kart's yaw
        double yawRadians = Math.toRadians(kartPosition.yaw());
        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);

        // Position of the block in front
        var blockInFrontPos = kartPosition.add(forwardX, 0, forwardZ);
        var blockInFront = instance.getBlock(blockInFrontPos);
        var BlockBelowPos = kartPosition.add(0, -0.5, 0);
        var blockBelow = instance.getBlock(BlockBelowPos);

        // Check if the block in front is a slab or full block
        if (blockInFront.name().contains("slab") && blockInFront.getProperty("type").equals("bottom")) {
            // If it's a slab, move the kart 0.5 blocks above
            kart.getKartEntity().setVelocity(kart.getKartEntity().getVelocity().add(0, 8, 0));
        } else if (blockInFront.isSolid()) {
            if(blockBelow.getProperty("type") == null)
                return;
            if(blockBelow.name().contains("slab") && blockBelow.getProperty("type").equals("bottom")) {
                kart.getKartEntity().setVelocity(kart.getKartEntity().getVelocity().add(0, 8, 0));
            }
        } else if (instance.getBlock(kartPosition.add(0, -0.5, 0)).isAir()) {
            // If no block below, simulate falling
            kart.getKartEntity().setVelocity(kart.getKartEntity().getVelocity().add(0, -12, 0));
        }
    }


    private static void handleTurning(GamePlayer gamePlayer, Kart kart, String turning) {
        boolean isTurning = "left".equals(turning) || "right".equals(turning);
        ticksToNextTurnDeceleration.putIfAbsent(gamePlayer, 0);

        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid());
        if (isTurning && (shouldBypassMinimumTurningSpeed(kart) == false)) {
            int ticks = ticksToNextTurnDeceleration.get(gamePlayer);
            if (ticks >= TICKS_TO_TURN_DECELERATION) {
                ticksToNextTurnDeceleration.put(gamePlayer, 0);
                applyTurnDeceleration(kart);
            } else {
                ticksToNextTurnDeceleration.put(gamePlayer, ticks + 1);
            }
        }
    }

    private static void handleAcceleration(Kart kart, String moving) {
        GamePlayer gamePlayer = Main.getGame().findGamePlayerFromKart(kart);
        if(kart.getAcceleration() > MIN_TURNING_SPEED)
            if(isTurningList.contains(gamePlayer) && shouldBypassMinimumTurningSpeed(kart) == false)
                return;
        if ("forward".equals(moving)) {
            if (kart.getAcceleration() < MAX_ACCELERATION) {
                kart.acceleration += ACCELERATION_INCREMENT;
            }
            else if (kart.getAcceleration() > MAX_ACCELERATION) {
                kart.acceleration = MAX_ACCELERATION;
            }
        } else if ("backward".equals(moving)) {
            if (kart.getAcceleration() > MIN_ACCELERATION) {
                kart.acceleration -= ACCELERATION_INCREMENT;
            } else if (kart.getAcceleration() < MIN_ACCELERATION) {
                kart.acceleration = MIN_ACCELERATION;
            }
        } else {
            applyDeceleration(kart);
        }
    }

    private static void applyTurnDeceleration(Kart kart) {
        if (kart.getTotalAcceleration() > MIN_TURNING_SPEED) {
            kart.acceleration = kart.getAcceleration() - TURN_DECELERATION_RATE;
        } else if (kart.getTotalAcceleration() < -MIN_TURNING_SPEED) {
            kart.acceleration = kart.getAcceleration() + TURN_DECELERATION_RATE;
        }
    }

    private static void applyDeceleration(Kart kart) {
        if (kart.getAcceleration() > 0) {
            kart.acceleration = Math.max(0, kart.getAcceleration() - DECELERATION_RATE);
        } else if (kart.getAcceleration() < 0) {
            kart.acceleration = Math.min(0, kart.getAcceleration() + DECELERATION_RATE);
        }

        if(kart.getAdditionalAcceleration() > 0) {
            kart.additionalAcceleration = Math.max(0, kart.getAdditionalAcceleration() - DECELERATION_RATE);
        } else if(kart.getAdditionalAcceleration() < 0) {
            kart.additionalAcceleration = Math.min(0, kart.getAdditionalAcceleration() + DECELERATION_RATE);
        }
    }

    private static boolean shouldBypassMinimumTurningSpeed(Kart kart) {
        // Add your condition here for bypassing the minimum turning speed.
        // Example: return kart.isSpecialModeEnabled();
        return kart.isDrifting();
    }
}
