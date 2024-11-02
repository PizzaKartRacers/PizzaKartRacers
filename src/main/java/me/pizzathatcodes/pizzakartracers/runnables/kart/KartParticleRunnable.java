package me.pizzathatcodes.pizzakartracers.runnables.kart;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class KartParticleRunnable extends BukkitRunnable {
    @Override
    public void run() {
        for(GamePlayer gamePlayer : Main.getGame().getPlayers()) {
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
}
