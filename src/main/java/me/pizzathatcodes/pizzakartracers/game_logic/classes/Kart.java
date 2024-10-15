package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import me.pizzathatcodes.pizzakartracers.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

public class Kart {

    ArmorStand kartEntity;
    double speed;
    public int acceleration;
    double handling;
    public BukkitTask accelerationTask;
    public BukkitTask decelerationTask;
    public BukkitTask boostPadTask;
    public BukkitTask speedTask;
    public BukkitTask boostPadDelayTask;
    public int boostPadDelay = 0;
    public BukkitTask bounceTask;
    public BukkitTask tiltTask;
    public int tiltDelay = 0;

    public BukkitTask rotationTask;
    public float yaw;

    public BukkitTask kartDriveParticle;

    public Kart(double speed, int accelerationvar, double handling) {
        this.speed = speed;
        this.acceleration = accelerationvar;
        this.handling = handling;
        this.yaw = 0;

        Kart kart = this;
        // TODO - This code is for the acceleration mechanics, but it's disabled for now
        speedTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(acceleration != 0) {
                    move();
                }
                GamePlayer gamePlayer = Main.getGame().findGamePlayerFromKart(kart);
                int level = acceleration < 0 ? acceleration * -1 : acceleration;
                Bukkit.getPlayer(gamePlayer.getUuid()).setLevel(level);
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);

        boostPadDelayTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(boostPadDelay > 0)
                    boostPadDelay--;
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

        kartDriveParticle = new BukkitRunnable() {
            @Override
            public void run() {
                if (kartEntity == null) {
                    return;
                }

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
                for (int i = 0; i < 5; i++) {
                    world.spawnParticle(Particle.EXPLOSION_NORMAL, behind.clone().add(0, 2, 0), 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 5);


        // TODO: This code is for bounce mechanics, but it causes the karts to not detect slabs in front of them, so it's disabled for now while I work on more important things
//        bounceTask = new BukkitRunnable() {
//            int bounceTicks = 0;
//            @Override
//            public void run() {
//                if (getKartEntity() == null) {
//                    cancel(); // Stop task if kart doesn't exist
//                    bounceTask = null;
//                    return;
//                }
//
//                Location loc = getKartEntity().getLocation();
//                Vector currentVelocity = getKartEntity().getVelocity();
//
//                // Gradually go up and down using a sine wave to simulate smooth bouncing
//                double bounceHeight = 0.025; // Max bounce height (0.5 total up and down movement)
//                double frequency = 0.1;     // How fast the bounce oscillates
//
//                // Calculate the new Y-velocity based on the sine wave
//                double yVelocity = Math.sin(bounceTicks * frequency) * bounceHeight;
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
     * Set the speed
     * @param speed the speed to set
     */
    public void setSpeed(double speed) {
        this.speed = speed;
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
     * @return the speed
     */
    public double getSpeed() {
        return speed;
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

//        player.sendMessage("Direction: " + direction);
//        player.sendMessage("Yaw: " + armorStand.getLocation().getYaw());

        // Multiply the direction by acceleration to determine the velocity
        Vector velocity = direction.multiply(acceleration * 0.015); // Adjust this factor to control speed

//        player.sendMessage("Velocity: " + velocity);



        // Get the block directly under the kart (for falling/climbing checks)
        Block blockBelow = loc.clone().add(0, -1, 0).getBlock();

        // Handle vertical movement (falling or staying on the ground)
        if (blockBelow.getType().isAir()) {
            velocity.setY(-0.5); // Simulate falling if there's no block below
        } else {
            velocity.setY(armorStand.getVelocity().getY()); // Keep the current Y velocity (e.g., natural gravity)
        }

        if(blockBelow.getType() == Material.BROWN_MUSHROOM_BLOCK) {
            // Get the BlockData for the brown mushroom block
            BlockData data = blockBelow.getBlockData();
            if(data instanceof MultipleFacing) {
                Main.getInstance().getLogger().info("Found a mushroom block!");
                MultipleFacing mushroomData = (MultipleFacing) data;
                // Check for the SOUTH_WEST or SOUTH_EAST varient
                Main.getInstance().getLogger().info("Faces: " + mushroomData.getFaces());
                if(!mushroomData.getFaces().contains(BlockFace.SOUTH) && (!mushroomData.getFaces().contains(BlockFace.WEST) || !mushroomData.getFaces().contains(BlockFace.EAST))) {
                    Main.getInstance().getLogger().info("Boosting acceleration!");
                    // Boost the player's acceleration if the conditions are met
                    if(boostPadDelay == 0)
                        boostAcceleration("NORTH");
                }
                if(!mushroomData.getFaces().contains(BlockFace.NORTH) && (!mushroomData.getFaces().contains(BlockFace.WEST) || !mushroomData.getFaces().contains(BlockFace.EAST))) {
                    Main.getInstance().getLogger().info("Boosting acceleration!");
                    // Boost the player's acceleration if the conditions are met
                    if(boostPadDelay == 0)
                        boostAcceleration("SOUTH");
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

        switch (type) {
            case "SOUTH": {
                Kart kart = this;
                boostPadTask = new BukkitRunnable() {
                    boolean finished = false;
                    int additionalAcceleration = 0;

                    @Override
                    public void run() {
                        if (finished) {
                            cancel(); // Stop the current task after acceleration finishes
                            return;
                        }

                        // Boost the acceleration
                        if ((additionalAcceleration) < 50) {
                            acceleration += 3;
                            additionalAcceleration += 3;
                            Main.getInstance().getLogger().info("Acceleration: " + acceleration);

                            spawnBoostParticles();

                            if (additionalAcceleration >= 50) {
                                Main.getInstance().getLogger().info("Finished boosting acceleration! At: " + acceleration);
                                finished = true;

                                // Start the deceleration task
                                decelerationTask = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Main.getInstance().getLogger().info("Decelerating... from: " + acceleration);

                                        spawnBoostParticles();

                                        if (acceleration >= 65) {
                                            acceleration -= 2;
                                            Main.getInstance().getLogger().info("Decelerating... to: " + acceleration);
                                        } else {
                                            Main.getInstance().getLogger().info("Deceleration complete at: " + acceleration);
                                            cancel(); // Stop the deceleration task
                                            decelerationTask = null;
                                        }
                                    }
                                }.runTaskTimer(Main.getInstance(), 0, 2); // Run deceleration every 3 ticks
                            }
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 0, 2); // Run acceleration every tick
                break;
            }

            case "NORTH": {
                Kart kart = this;
                boostPadTask = new BukkitRunnable() {
                    boolean finished = false;
                    int lastAcceleration = acceleration;
                    int additionalAcceleration = 0;

                    @Override
                    public void run() {
                        if (finished) {
                            cancel(); // Stop the current task after acceleration finishes
                            return;
                        }

                        // Boost the acceleration
                        if ((lastAcceleration + additionalAcceleration) <= 158) {
                            acceleration += 10;
                            additionalAcceleration += 10;
                            Main.getInstance().getLogger().info("Acceleration: " + acceleration);

                            if(acceleration >= 80)
                                spawnBoostParticles();

                            if ((lastAcceleration + additionalAcceleration) >= 154) {
                                Main.getInstance().getLogger().info("Finished boosting acceleration! At: " + acceleration);
                                finished = true;

                                // Start the deceleration task
                                decelerationTask = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Main.getInstance().getLogger().info("Decelerating... from: " + acceleration);

                                        if(acceleration >= 80)
                                            spawnBoostParticles();

                                        if (acceleration >= 65) {
                                            acceleration -= 3;
                                            Main.getInstance().getLogger().info("Decelerating... to: " + acceleration);
                                        } else {
                                            Main.getInstance().getLogger().info("Deceleration complete at: " + acceleration);
                                            cancel(); // Stop the deceleration task
                                            decelerationTask = null;
                                        }
                                    }
                                }.runTaskTimer(Main.getInstance(), 0, 2); // Run deceleration every 3 ticks
                            }
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 0, 2); // Run acceleration every tick
                break;
            }

            default:
                break;
        }

    }

    /**
     * Spawn particles to indicate a speed boost
     */
    public void spawnBoostParticles() {
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
    }

    /**
     * Stop all tasks related to the kart
     */
    public void stopAllTasks() {
        if(accelerationTask != null) {
            accelerationTask.cancel();
            accelerationTask = null;
        }
        if(decelerationTask != null) {
            decelerationTask.cancel();
            decelerationTask = null;
        }
        if(boostPadTask != null) {
            boostPadTask.cancel();
            boostPadTask = null;
        }
        if(speedTask != null) {
            speedTask.cancel();
            speedTask = null;
        }
        if(boostPadDelayTask != null) {
            boostPadDelayTask.cancel();
            boostPadDelayTask = null;
        }
        if(bounceTask != null) {
            bounceTask.cancel();
            bounceTask = null;
        }
        if(tiltTask != null) {
            tiltTask.cancel();
            tiltTask = null;
        }
        if(kartDriveParticle != null) {
            kartDriveParticle.cancel();
            kartDriveParticle = null;
        }
    }



}
