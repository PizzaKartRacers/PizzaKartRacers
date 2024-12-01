package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.HashMap;

public class KartAccelerationRunnable {

    private static final int MAX_ACCELERATION = 65;
    private static final int MIN_ACCELERATION = -65;
    private static final int ACCELERATION_INCREMENT = 2;
    private static final int BRAKING_FORCE = 5;
    private static final int DECELERATION_RATE = 3;
    private static final int TURN_DECELERATION_RATE = 1;
    private static final int TICKS_TO_TURN_DECELERATION = 10;
    private static final int MIN_TURNING_SPEED = 57;
    private static final int ACCELERATION_SLOWDOWN_AMOUNT = 57;

    private static final HashMap<GamePlayer, Integer> ticksToNextTurnDeceleration = new HashMap<>();
    private static final HashMap<GamePlayer, Integer> ticksToNextSpeedChange = new HashMap<>();
    private static final int TICKS_TO_SPEED_CHANGE = 10;

    public static void startTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (GamePlayer gamePlayer : Main.getGame().getPlayers()) {
                Kart kart = gamePlayer.getKart();
                if (kart == null || kart.getKartEntity() == null) {
                    continue;
                }

                String moving = kart.moving;
                String turning = kart.turning;

                Entity kartEntity = kart.getKartEntity();
                Pos kartPosition = util.copyPosition(kartEntity.getPosition());

                // Handling block interactions
                Block blockBelow = kartEntity.getInstance().getBlock(util.copyPosition(kartPosition).add(0, -0.5, 0));

                if (blockBelow.isAir()) {
                    kartEntity.setVelocity(kartEntity.getVelocity().add(0, -2, 0)); // Fall
                }

                // Turning logic
                boolean isTurning = "left".equals(turning) || "right".equals(turning);
                ticksToNextTurnDeceleration.putIfAbsent(gamePlayer, 0);
                ticksToNextSpeedChange.putIfAbsent(gamePlayer, 0);

                if (isTurning) {
                    handleTurning(gamePlayer, kart, ticksToNextTurnDeceleration);
                }

                // Handle acceleration and braking
                handleAcceleration(gamePlayer, kart, moving, isTurning);
            }
        }).repeat(TaskSchedule.tick(1)).schedule(); // Runs every tick
    }

    private static void handleTurning(GamePlayer gamePlayer, Kart kart, HashMap<GamePlayer, Integer> turnDecelerationMap) {
        if (kart.getAcceleration() > MIN_TURNING_SPEED || kart.getAcceleration() < -MIN_TURNING_SPEED) {
            if (turnDecelerationMap.get(gamePlayer) >= TICKS_TO_TURN_DECELERATION) {
                turnDecelerationMap.put(gamePlayer, 0);

                if (kart.getAcceleration() > MIN_TURNING_SPEED) {
                    kart.acceleration = Math.max(MIN_TURNING_SPEED, kart.getAcceleration() - TURN_DECELERATION_RATE);
                } else if (kart.getAcceleration() < -MIN_TURNING_SPEED) {
                    kart.acceleration = Math.min(-MIN_TURNING_SPEED, kart.getAcceleration() + TURN_DECELERATION_RATE);
                }
            } else {
                turnDecelerationMap.put(gamePlayer, turnDecelerationMap.get(gamePlayer) + 1);
            }
        }
    }

    private static void handleAcceleration(GamePlayer gamePlayer, Kart kart, String moving, boolean isTurning) {
        if (!"none".equals(moving)) {
            if ("forward".equals(moving)) {
                if (kart.getAcceleration() < 0) {
                    kart.acceleration += BRAKING_FORCE; // Braking when switching directions
                } else if (kart.getAcceleration() < MAX_ACCELERATION) {
                    accelerateKart(gamePlayer, kart, isTurning, true);
                }
            } else if ("backward".equals(moving)) {
                if (kart.getAcceleration() > 0) {
                    kart.acceleration -= BRAKING_FORCE; // Braking when switching directions
                } else if (kart.getAcceleration() > MIN_ACCELERATION) {
                    accelerateKart(gamePlayer, kart, isTurning, false);
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

    private static void accelerateKart(GamePlayer gamePlayer, Kart kart, boolean isTurning, boolean forward) {
        if (!isTurning || Math.abs(kart.getAcceleration()) < MIN_TURNING_SPEED) {
            if (kart.getAcceleration() >= ACCELERATION_SLOWDOWN_AMOUNT) {
                if (ticksToNextSpeedChange.get(gamePlayer) == TICKS_TO_SPEED_CHANGE) {
                    ticksToNextSpeedChange.put(gamePlayer, 0);
                    kart.acceleration += forward ? 1 : -1;
                } else {
                    ticksToNextSpeedChange.put(gamePlayer, ticksToNextSpeedChange.get(gamePlayer) + 1);
                }
            } else {
                kart.acceleration += forward ? ACCELERATION_INCREMENT : -ACCELERATION_INCREMENT;
            }
        }
    }
}
