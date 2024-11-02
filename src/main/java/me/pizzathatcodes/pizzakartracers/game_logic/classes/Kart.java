package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import me.pizzathatcodes.pizzakartracers.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

public class Kart {

    ArmorStand kartEntity;
    public int acceleration;
    double handling;
    public BukkitTask decelerationTask;
    public BukkitTask boostPadTask;
    public int boostPadDelay = 0;
    public BukkitTask bounceTask;
    public BukkitTask tiltTask;
    public boolean drifting;
    public float yaw;

    public String moving;
    public String turning;

    public Kart(int accelerationvar, double handling) {
        this.acceleration = accelerationvar;
        this.handling = handling;
        this.yaw = 0;
        moving = "none";
        turning = "none";
        this.drifting = false;


        // TODO: This code is for bounce mechanics, but it causes the karts to not detect slabs in front of them, so it's disabled for now while I work on more important things
//        bounceTask = new BukkitRunnable() {
//            int bounceTicks = 0;
//            @Override
//            public void run() {
//                if (getKartEntity() == null) {
//                    return;
//                }
//
//                Vector currentVelocity = getKartEntity().getVelocity();
//
//                // Gradually go up and down using a sine wave to simulate smooth bouncing
//                double bounceHeight = 0.05; // Max bounce height (0.5 total up and down movement)
//                double frequency = 0.1;     // How fast the bounce oscillates
//
//                // Calculate the new Y-velocity based on the sine wave
//                double yVelocity = Math.sin(bounceTicks * frequency) * bounceHeight;
//
//                Location loc = getKartEntity().getLocation();
//                float yaw = loc.getYaw();  // Yaw is in degrees
//
//                // Convert yaw to radians for trigonometry calculations
//                double radiansYaw = Math.toRadians(yaw);
//
//                Vector direction = new Vector(-Math.sin(radiansYaw), 0, Math.cos(radiansYaw));
//                direction.normalize();
//
//                Location blockInFrontLoc = loc.clone().add(direction.clone().multiply(1));
//                Block blockInFront = blockInFrontLoc.getBlock();
//
//                Block blockBelow = loc.clone().add(0, -1, 0).getBlock();
//
//                if(blockInFront.getType().isSolid() && blockInFront.getBoundingBox().getHeight() < 1.0) {
//                    yVelocity += 0.7; // Simulate climbing a slab
//                } else if (blockBelow.getType().isAir()) {
//                    yVelocity -= 1; // Simulate falling if there's no block below
//                }
//
//                // Set the new Y-velocity while keeping the horizontal velocity unchanged
//                Vector newVelocity = currentVelocity.clone().setY(yVelocity);
//                getKartEntity().setVelocity(newVelocity);
//
//                // Increment the bounce ticks for the next step
//                bounceTicks++;
//            }
//        }.runTaskTimer(Main.getInstance(), 0L, 1L); // Run every tick (20 times per second)
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
    public ArmorStand getKartEntity() {
        return kartEntity;
    }

    /**
     * Set the kartEntity
     * @param kartEntity the kartEntity to set
     */
    public void setKartEntity(ArmorStand kartEntity) {
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
     * Move the kart based on acceleration and direction
     */
    public void move() {
        ArmorStand armorStand = getKartEntity();
        Location loc = armorStand.getLocation();

        // Get the yaw (horizontal rotation) of the ArmorStand
        float yaw = loc.getYaw();  // Yaw is in degrees

        // Convert yaw to radians for trigonometry calculations
        double radiansYaw = Math.toRadians(yaw);

        // Calculate the forward direction vector based on yaw
        Vector direction = new Vector(-Math.sin(radiansYaw), 0, Math.cos(radiansYaw));

//        GamePlayer gamePlayer = Main.getGame().findGamePlayerFromKart(this);
//        Player player = Bukkit.getPlayer(gamePlayer.getUuid());

        // Normalize the direction to ensure consistent movement speed
        direction.normalize();

        // Multiply the direction by acceleration to determine the velocity
        Vector velocity = direction.multiply(acceleration * 0.015); // Adjust this factor to control speed

        Block blockBelow = loc.clone().add(0, -1, 0).getBlock();


        // Handle vertical movement (falling or staying on the ground)

        if(blockBelow.getType() == Material.BROWN_MUSHROOM_BLOCK) {
            // Get the BlockData for the brown mushroom block
            BlockData data = blockBelow.getBlockData();
            if(data instanceof MultipleFacing) {
//                Main.getInstance().getLogger().info("Found a mushroom block!");
                MultipleFacing mushroomData = (MultipleFacing) data;
                // Check for the SOUTH_WEST or SOUTH_EAST varient
//                Main.getInstance().getLogger().info("Faces: " + mushroomData.getFaces());
                if(mushroomData.getFaces().contains(BlockFace.SOUTH) && (mushroomData.getFaces().contains(BlockFace.WEST) || mushroomData.getFaces().contains(BlockFace.EAST)) && mushroomData.getFaces().contains(BlockFace.UP)) {
//                    Main.getInstance().getLogger().info("Boosting acceleration!");
                    // Boost the player's acceleration if the conditions are met
                    if(boostPadDelay == 0)
                        boostAcceleration("SOUTH");
                }
                if(mushroomData.getFaces().contains(BlockFace.NORTH) && (mushroomData.getFaces().contains(BlockFace.WEST) || mushroomData.getFaces().contains(BlockFace.EAST)) && mushroomData.getFaces().contains(BlockFace.UP)) {
//                    Main.getInstance().getLogger().info("Boosting acceleration!");
                    // Boost the player's acceleration if the conditions are met
                    if(boostPadDelay == 0)
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
        boostPadDelay = 2;
        if(boostPadTask != null) return;

        int accelerationIncrement, maxAcceleration, decelerationAmount;
        int finalAccelerationLimit;

        switch (type) {
            case "SOUTH":
                accelerationIncrement = 3;
                maxAcceleration = 50;
                decelerationAmount = 2;
                finalAccelerationLimit = 65;
                break;

            case "NORTH":
                accelerationIncrement = 10;
                maxAcceleration = 90;
                decelerationAmount = 3;
                finalAccelerationLimit = 65;
                break;

            default:
                return;
        }

        boostPadTask = new BukkitRunnable() {
            boolean finished = false;
            int additionalAcceleration = 0;

            @Override
            public void run() {
                if (finished) {
                    cancel();
                    boostPadTask = null;
                    return;
                }

                // Boost the acceleration
                if (additionalAcceleration < maxAcceleration) {
                    acceleration += accelerationIncrement;
                    additionalAcceleration += accelerationIncrement;
                    if (acceleration >= 80 || type.equals("SOUTH")) {
                        spawnBoostParticles();
                    }

                    if (additionalAcceleration >= maxAcceleration) {
                        finished = true;

                        // Start the deceleration task
                        decelerationTask = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (acceleration >= finalAccelerationLimit) {
                                    acceleration -= decelerationAmount;
                                    if (acceleration >= 80 || type.equals("SOUTH")) {
                                        spawnBoostParticles();
                                    }
                                } else {
                                    cancel();
                                    decelerationTask = null;
                                }
                            }
                        }.runTaskTimer(Main.getInstance(), 0, 2); // Decelerate every 3 ticks
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 2); // Accelerate every tick
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
        Location loc = kartEntity.getLocation();
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
        Random random = new Random();

        for (int i = 0; i < 30; i++) {
            // Generate random RGB values between 0 and 255
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);

            int xoffset = random.nextInt(3) - 1;
            int zoffset = random.nextInt(3) - 1;

            // Create DustOptions with random color and size
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), 1);

            // Spawn the particle at the given location
            world.spawnParticle(Particle.REDSTONE, behind.clone().add(xoffset, 2, zoffset), 50, 3, 3, 3, dustOptions);
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



}
