package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.runnables.kart.KartAccelerationRunnable;
import me.pizzathatcodes.pizzakartracers.utils.util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.component.ItemBlockState;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Kart {

    LivingEntity kartEntity;
    public int acceleration;
    public int additionalAcceleration = 0;


    double handling;
    public int boostPadDelay = 0;
    public boolean drifting;
    public int driftTicks = 0;
    public int driftDelay = 0;
    public boolean spunOut = false;
    public float yaw;

    public String moving;
    public String turning;

    public Task boostPadTask;
    public Task decelerationTask;
    public Task driftDeaccelerationTask;
    public Task bounceTask;
    public Task tiltTask;
    private boolean hasChargedWhileStill = false;


    public Kart(int accelerationvar, double handling) {
        this.acceleration = accelerationvar;
        this.handling = handling;
        this.yaw = 0;
        moving = "none";
        turning = "none";
        this.drifting = false;
//        bounceTask = MinecraftServer.getSchedulerManager().buildTask(new Runnable() {
//                    int bounceTicks = 0;
//                    @Override
//                    public void run() {
//                        // Retrieve current velocity
//                        Vec currentVelocity = kartEntity.getVelocity();
//
//                        // Gradually go up and down using a sine wave to simulate smooth bouncing
//                        double bounceHeight = -0.5; // Max bounce height (0.5 total up and down movement)
//                        double frequency = 0.1;     // How fast the bounce oscillates
//
//                        // Calculate the new Y-velocity based on the sine wave
//                        double yVelocity = Math.sin(bounceTicks * frequency) * bounceHeight;
//
//                        // Set the new Y-velocity while keeping the horizontal velocity unchanged
//                        Vec newVelocity = currentVelocity.withY(yVelocity);
//                        kartEntity.setVelocity(newVelocity);
//
//                        KartAccelerationRunnable.handleBlockInteractions(Kart.this);
//
//                        // Increment the bounce ticks for the next step
//                        bounceTicks++;
//                    }
//                }).repeat(TaskSchedule.tick(1)).schedule(); // Run every tick (20 times per second)

    }

    /**
     * Set the acceleration
     * @param acceleration the acceleration to set
     */
    public void setAcceleration(int acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Set the handling
     * @param handling the handling to set
     */
    public void setHandling(double handling) {
        this.handling = handling;
    }

    /**
     * @return the acceleration
     */
    public int getAcceleration() {
        return acceleration;
    }

    /**
     * @return the handling
     */
    public double getHandling() {
        return handling;
    }

    /**
     * @return the kartEntity
     */
    public LivingEntity getKartEntity() {
        return kartEntity;
    }

    /**
     * @return the driftTicks
     */
    public int getDriftTicks() {
        return driftTicks;
    }

    /**
     * Set the kartEntity
     * @param kartEntity the kartEntity to set
     */
    public void setKartEntity(LivingEntity kartEntity) {
        this.kartEntity = kartEntity;
    }

    /**
     * Set the drifting state
     * @param drifting the drifting to set
     */
    public void setDrifting(boolean drifting) {
        this.drifting = drifting;
    }

    /**
     * @return if the kart is drifting
     */
    public boolean isDrifting() {
        return drifting;
    }

    /**
     * Set the drift ticks
     * @param driftTicks the driftTicks to set
     */
    public void setDriftTicks(int driftTicks) {
        this.driftTicks = driftTicks;
    }

    /**
     * Set the drift delay
     * @param driftDelay the driftDelay to set
     */
    public void setDriftDelay(int driftDelay) {
        this.driftDelay = driftDelay;
    }

    /**
     * @return if the kart is spun out
     */
    public boolean isSpunOut() {
        return spunOut;
    }

    /**
     * Set the spun out state
     * @param spunOut the spunOut to set
     */
    public void setSpunOut(boolean spunOut) {
        this.spunOut = spunOut;
    }

    public int getDriftDelay() {
        return driftDelay;
    }

    public void setTurning(String turning) {
        this.turning = turning;
    }

    public boolean hasChargedWhileStill() {
        return hasChargedWhileStill;
    }

    public void setHasChargedWhileStill(boolean hasChargedWhileStill) {
        this.hasChargedWhileStill = hasChargedWhileStill;
    }

    public int getAdditionalAcceleration() {
        return additionalAcceleration;
    }

    public int getTotalAcceleration() {
        return acceleration + additionalAcceleration;
    }

    /**
     * Move the kart based on acceleration and direction
     */
    public void move() {
        LivingEntity armorStand = getKartEntity();
        Pos loc = armorStand.getPosition();

        // Get the yaw (horizontal rotation) of the ArmorStand
        float yaw = loc.yaw();  // Yaw is in degrees

        // Convert yaw to radians for trigonometry calculations
        double radiansYaw = Math.toRadians(yaw);

        // Calculate the forward direction vector based on yaw
        Vec direction = new Vec(-Math.sin(radiansYaw), 0, Math.cos(radiansYaw));

//        GamePlayer gamePlayer = Main.getGame().findGamePlayerFromKart(this);
//        Player player = Bukkit.getPlayer(gamePlayer.getUuid());

        // Normalize the direction to ensure consistent movement speed
        direction.normalize();

        // Multiply the direction by acceleration to determine the velocity
        Vec velocity = direction.mul((acceleration + additionalAcceleration) * 0.28); // Adjust this factor to control speed

        Instance instance = kartEntity.getInstance(); // Replace with your instance source


        // Handle vertical movement (falling or staying on the ground)

        Point blockBelowPosition = util.copyPosition(loc).add(0, -1, 0);
        Block blockBelow = instance.getBlock(blockBelowPosition);

// Check if the block is a Brown Mushroom Block
        if (blockBelow.compare(Block.BROWN_MUSHROOM_BLOCK)) {
            // Retrieve block states (similar to faces in MultipleFacing)
            String south = blockBelow.getProperty("south");
            String north = blockBelow.getProperty("north");
            String west = blockBelow.getProperty("west");
            String east = blockBelow.getProperty("east");
            String up = blockBelow.getProperty("up");

            if ("true".equals(south) && ("true".equals(west) || "true".equals(east)) && "true".equals(up)) {
                // Boost acceleration for SOUTH
                if (boostPadDelay == 0) {
                    boostAcceleration("SOUTH");
                }
            }

            if ("true".equals(north) && ("true".equals(west) || "true".equals(east)) && "true".equals(up)) {
                // Boost acceleration for NORTH
                if (boostPadDelay == 0) {
                    boostAcceleration("NORTH");
                }
            }
        }

        // Set the final velocity to the kart entity
        armorStand.setVelocity(velocity);
    }





    /**
     * Boost the acceleration for a short duration
     * @param type the type of boost (e.g., "SOUTH" or "NORTH")
     */
    public void boostAcceleration(String type) {
        if (boostPadTask != null) {
            return;
        }

        boostPadDelay = 2;

        // Check if kart is drifting and spun out
        if (drifting && spunOut) {
            // Cancel spinout state
            setSpunOut(false);

            // Reset drift ticks or delay
            driftTicks = 0;
            driftDelay = 0;

            // Apply a small velocity boost to simulate stabilization
            Vec currentVelocity = kartEntity.getVelocity();
            Vec forwardBoost = currentVelocity.normalize().mul(5); // Adjust boost multiplier as needed
            kartEntity.setVelocity(forwardBoost);

            // Optionally spawn special particles for heat resetting
            spawnBoostParticles();

            return; // Exit early since we've handled the heat reset
        }

        // Original boost handling logic
        int accelerationIncrement, maxAccelerationBoost, decelerationAmount, finalAccelerationLimit;

        switch (type) {
            case "SOUTH":
                accelerationIncrement = 3;
                maxAccelerationBoost = 40;
                decelerationAmount = 2;
                finalAccelerationLimit = 65;
                break;
            case "NORTH":
                accelerationIncrement = 10;
                maxAccelerationBoost = 90;
                decelerationAmount = 3;
                finalAccelerationLimit = 65;
                break;
            default:
                System.out.println("Invalid boost type: " + type);
                return;
        }

        // Schedule the boost task
        boostPadTask = MinecraftServer.getSchedulerManager().buildTask(new Runnable() {
            boolean finished = false;

            @Override
            public void run() {
                if (!finished && (additionalAcceleration < maxAccelerationBoost)) {
                    additionalAcceleration += accelerationIncrement;

                    if (additionalAcceleration >= maxAccelerationBoost) {
                        finished = true;

                        // Start the deceleration task
                        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                            if (getTotalAcceleration() >= finalAccelerationLimit) {
                                additionalAcceleration -= decelerationAmount;
                                if (additionalAcceleration >= 80 || type.equals("SOUTH")) {
                                    spawnBoostParticles();
                                }
                            } else {
                                if(additionalAcceleration < 0)
                                    additionalAcceleration = 0;
                                return TaskSchedule.stop();
                            }
                            return TaskSchedule.millis(25);
                        }, TaskSchedule.millis(1));
                        boostPadTask.cancel();
                        boostPadTask = null;
                    }
                } else {
                    // Reset the boost task and state
                    boostPadTask.cancel();
                    boostPadTask = null;
                }
                return;
            }
        }).repeat(TaskSchedule.tick(2) ).schedule();

    }




    boolean spawningBoostParticles = false;

    /**
     * Spawn particles to indicate a speed boost
     */
    public void spawnBoostParticles() {
        if(spawningBoostParticles) {
            return;
        }
        spawningBoostParticles = true;
        Pos loc = kartEntity.getPosition();

        // Clone location and adjust it to be behind the entity
        Pos behind = util.copyPosition(loc);

        // Get the yaw in radians
        double yaw = Math.toRadians(loc.yaw());

        // Calculate the offset behind the player using trigonometry
        double xOffset = -Math.sin(yaw) * -1.7;
        double zOffset = Math.cos(yaw) * -1.7;

        // Apply the offset
        behind.add(xOffset, 0, zOffset);

        // Spawn the particles
        Random random = new Random();

        for (int i = 0; i < 30; i++) {
            // Generate random RGB values between 0 and 255
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);

            int xoffset = random.nextInt(3) - 1;
            int zoffset = random.nextInt(3) - 1;

            // Create DustOptions with random color and size
//            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), 1);

            // Spawn the particle at the given location
//            world.spawnParticle(Particle.REDSTONE, util.copyPosition(behind).add(xoffset, 2, zoffset), 50, 3, 3, 3, dustOptions);
        }
        spawningBoostParticles = false;
    }

    /**
     * Stop all tasks related to the kart
     */
    public void stopAllTasks() {
        if(decelerationTask != null) {
            decelerationTask.cancel();
            decelerationTask = null;
        }
        if(boostPadTask != null) {
            boostPadTask.cancel();
            boostPadTask = null;
        }
        if(bounceTask != null) {
            bounceTask.cancel();
            bounceTask = null;
        }
        if(tiltTask != null) {
            tiltTask.cancel();
            tiltTask = null;
        }
    }

    public Task spinout;
    public int decelerationAmount;







}
