package me.pizzathatcodes.pizzakartracers.game_logic.event;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.Kart;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;

public class DriftingHandler {

    private static final float XP_INCREMENT = 0.6f; // XP bar increment per drift
    private static final int TICKS_TO_ACCELERATION = 21; // Multiplier for acceleration
    private static final float MAX_XP = 1.0f; // Max XP bar value
    private static final int MAX_DRIFT_TICKS = 7; // Maximum allowable drift ticks

    public static void setupTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (GamePlayer gamePlayer : Main.getGame().getPlayers()) {
                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(gamePlayer.getUuid());
                if (player == null) continue;

                Kart kart = gamePlayer.getKart();
                if (kart.isDrifting()) {
                    // Handle drift tick charging
                    handleDriftCharging(player, kart);
                } else if (kart.getDriftTicks() > 0) {
                    // Handle release of shift (end of drift)
                    handleDriftRelease(player, kart);
                }
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    private static void handleDriftCharging(Player player, Kart kart) {
        if(kart.getAcceleration() > 0)
            kart.setAcceleration(kart.getAcceleration() - 2);

        if (kart.getDriftTicks() >= MAX_DRIFT_TICKS) {
            // If drift ticks reach the max, stop charging further
            return;
        }

        if ("left".equals(kart.turning) || "right".equals(kart.turning)) {
            // Increment drift ticks when turning
            kart.setDriftTicks(kart.getDriftTicks() + 1);
            kart.setHasChargedWhileStill(false); // Reset the still-charged flag
        } else if (!kart.hasChargedWhileStill()) {
            // Increment only once when standing still
            kart.setDriftTicks(Math.min(kart.getDriftTicks() + 1, MAX_DRIFT_TICKS));
            kart.setHasChargedWhileStill(true);
        }

        // Ensure drift ticks don't exceed the max
        if (kart.getDriftTicks() > MAX_DRIFT_TICKS) {
            kart.setDriftTicks(MAX_DRIFT_TICKS);
        }
    }

    private static void handleDriftRelease(Player player, Kart kart) {
        // Add XP bar increment
        float newXP = player.getExp() + XP_INCREMENT;

        if (newXP < MAX_XP) {
            // Below full XP bar: apply acceleration boost
            player.setExp(newXP);
            int boost = kart.getDriftTicks() * TICKS_TO_ACCELERATION;
            kart.additionalAcceleration = (kart.getAdditionalAcceleration() + boost);

            // Play boost feedback
            util.playCustomSound(player, "minecraft:entity.firework_rocket.launch", Sound.Source.MASTER, 10F, 1.0F);
//            util.spawnBoostParticles(kart.getKartEntity().getPosition());

            // Apply deceleration to stabilize speed
            applyDeceleration(kart, boost);

        } else {
            // Overheat: Trigger spinout
            player.sendMessage(util.translate("&cYour kart has overheated! You spun out!"));
            player.setExp(0.0f);
            kart.setSpunOut(true);
            triggerSpinout(player, kart);
        }

        // Reset drift ticks after release
        kart.setDriftTicks(0);
        kart.setHasChargedWhileStill(false); // Reset the still-charged flag
    }


    private static void triggerSpinout(Player player, Kart kart) {
        // Lock the movement direction
        if(kart.boostPadTask == null) {
            kart.setAcceleration(0); // Stop acceleration changes during spinout
            kart.setTurning("none"); // Disable turning
            kart.moving = "none"; // Disable movement inputs

            // Spinout parameters
            final int TOTAL_ROTATION = 360; // Total rotation for one spin
            final int NUM_SPINS = 3; // Number of full spins
            final int ROTATION_INCREMENT = 45; // Degrees per tick
            final long TICK_INTERVAL = 2; // Interval between ticks in game ticks

            // Start the spinout animation
            kart.spinout = MinecraftServer.getSchedulerManager().buildTask(new Runnable() {
                private int currentSpin = 0; // Track the current spin
                private int rotation = 0; // Track the rotation within one spin

                @Override
                public void run() {
                    if(kart.boostPadTask != null) {
                        kart.setSpunOut(false);
                        kart.spinout.cancel();
                        return;
                    }
                    // Update the visual spin only (yaw changes)
                    rotation += ROTATION_INCREMENT;
                    kart.getKartEntity().setView(kart.getKartEntity().getPosition().yaw() + ROTATION_INCREMENT, 0);

                    // Check if one full spin is complete
                    if (rotation >= TOTAL_ROTATION) {
                        currentSpin++;
                        rotation = 0; // Reset rotation for the next spin
                    }

                    // End the spinout after completing the defined number of spins
                    if (currentSpin >= NUM_SPINS) {
                        kart.setSpunOut(false); // End the spinout
                        kart.spinout.cancel(); // Stop the task
                    }
                    return;
                }
            }).repeat(TaskSchedule.tick((int) TICK_INTERVAL)).schedule();

        } else {
            kart.setSpunOut(false);
        }

        // Visual spinout feedback
        util.playCustomSound(player, "minecraft:block.anvil.land", Sound.Source.MASTER, 10F, 0.8F);
//        util.spawnSpinoutParticles(kart.getKartEntity().getPosition());
    }


    private static void applyDeceleration(Kart kart, int boost) {
        final int DECELERATION_STEP = 4; // Amount to decrease per tick
        final long TICK_INTERVAL = 5; // Interval between deceleration steps (in milliseconds)

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if(kart.additionalAcceleration < 0)
                kart.additionalAcceleration = 0;
            int currentAcceleration = kart.getAdditionalAcceleration();
            if(kart.boostPadTask != null) {
                kart.setSpunOut(false);
                kart.decelerationAmount = 0;
                return TaskSchedule.stop();
            }

            if (kart.decelerationAmount < boost) {
                // Decrease acceleration gradually
                if(kart.getAdditionalAcceleration() <= 0) {
                    kart.decelerationAmount = 0;
                    return TaskSchedule.stop();
                }

                kart.additionalAcceleration = (currentAcceleration - DECELERATION_STEP);
                kart.decelerationAmount += DECELERATION_STEP;
            } else {
                // Stop the task once the target is reached
                kart.decelerationAmount = 0;
                return TaskSchedule.stop();
            }
            return TaskSchedule.millis(TICK_INTERVAL);
        }, TaskSchedule.tick(1));
    }



}


