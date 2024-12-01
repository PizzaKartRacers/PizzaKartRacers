package me.pizzathatcodes.pizzakartracers.utils;

import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.game_logic.classes.GamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class util {

    public static Pos copyPosition(Pos original) {
        return new Pos(
                original.x(),
                original.y(),
                original.z(),
                original.yaw(),
                original.pitch()
        );
    }

    /**
     * Translates a string with legacy color codes (&) to an Adventure Component.
     * Supports hex colors in &#RRGGBB format.
     *
     * @param message The input string with legacy color codes and hex colors.
     * @return A Component representing the formatted message.
     */
    public static Component translate(String message) {
        // Use the LegacyComponentSerializer to handle both legacy and hex color codes
        return LegacyComponentSerializer.builder()
                .character('&') // Use '&' as the legacy color character
                .hexColors() // Enable support for &#RRGGBB hex colors
                .build()
                .deserialize(message);
    }

    public static void handleSidewayMovement(Player player, float sideways) {
        GamePlayer gamePlayer = Main.getGame().getGamePlayer(player.getUuid());
        if (gamePlayer == null) return;

        LivingEntity kartEntity = gamePlayer.getKart().getKartEntity(); // Replace ArmorStand with a generic Minestom entity
        ArmorStandMeta kartMeta = (ArmorStandMeta) kartEntity.getEntityMeta();
        Vec currentHeadPose = kartMeta.getHeadRotation(); // Get the current head rotation

        // Determine if player is turning
        if (sideways != 0) {
            float newSideways = sideways < 0 ? -1f : 1f;

            // Calculate the target tilt based on the direction
            double targetTilt = newSideways < 0 ? -15 : 15; // Left tilt or right tilt (in degrees)

            // Cancel any existing tilt task
            if (gamePlayer.getKart().tiltTask != null) {
                gamePlayer.getKart().tiltTask.cancel();
            }

            // Schedule a new tilt task
            gamePlayer.getKart().tiltTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
                final double increment = 2; // Control how fast the tilt changes
                double currentTilt = kartMeta.getHeadRotation().z(); // Start with the current tilt

                if (Math.abs(currentTilt - targetTilt) < 1) {
                    currentTilt = targetTilt; // Snap to target tilt when close enough
                    kartMeta.setHeadRotation(new Vec(0, 0, currentTilt));
                    kartEntity.sendPacketToViewersAndSelf(kartEntity.getMetadataPacket()); // Force packet update
                    gamePlayer.getKart().tiltTask.cancel(); // Stop the task
                } else {
                    // Move towards the target tilt gradually
                    if (currentTilt < targetTilt) {
                        currentTilt = Math.min(currentTilt + increment, targetTilt); // Increase tilt
                    } else {
                        currentTilt = Math.max(currentTilt - increment, targetTilt); // Decrease tilt
                    }
                    kartMeta.setHeadRotation(new Vec(0, 0, currentTilt));
                    kartEntity.sendPacketToViewersAndSelf(kartEntity.getMetadataPacket()); // Force packet update
                }
            }).repeat(TaskSchedule.tick(2)).schedule(); // Run every 2 ticks

        } else {
            // If sideways == 0 (no turning), start resetting the tilt back to 0
            if (gamePlayer.getKart().tiltTask != null) {
                gamePlayer.getKart().tiltTask.cancel(); // Cancel any existing tilt task
            }

            gamePlayer.getKart().tiltTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
                final double decrement = 2; // Control how fast the tilt returns to zero
                double currentTilt = kartMeta.getHeadRotation().z(); // Start with the current tilt

                if (Math.abs(currentTilt) < 1) {
                    kartMeta.setHeadRotation(new Vec(0, 0, 0)); // Reset to neutral position
                    kartEntity.sendPacketToViewersAndSelf(kartEntity.getMetadataPacket()); // Force packet update
                    gamePlayer.getKart().tiltTask.cancel(); // Stop the task once it reaches zero
                } else {
                    // Move back towards zero tilt
                    if (currentTilt > 0) {
                        currentTilt = Math.max(currentTilt - decrement, 0); // Gradually decrease positive tilt
                    } else {
                        currentTilt = Math.min(currentTilt + decrement, 0); // Gradually increase negative tilt
                    }
                    kartMeta.setHeadRotation(new Vec(0, 0, currentTilt));
                    kartEntity.sendPacketToViewersAndSelf(kartEntity.getMetadataPacket()); // Force packet update
                }
            }).repeat(TaskSchedule.tick(2)).schedule(); // Run every 2 ticks
        }
    }

    /**
     * Sends a title to the player.
     *
     * @param player The player to send the title to.
     * @param mainTitle The main title text.
     * @param subtitle The subtitle text.
     * @param fadeIn Duration (in ticks) for the title to fade in.
     * @param stay Duration (in ticks) for the title to stay visible.
     * @param fadeOut Duration (in ticks) for the title to fade out.
     */
    public static void sendTitle(Player player, Component mainTitle, Component subtitle, int fadeIn, int stay, int fadeOut) {
        Title title = Title.title(
                mainTitle,       // Main title
                subtitle,       // Subtitle
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50),  // Convert ticks to milliseconds
                        Duration.ofMillis(stay * 50),
                        Duration.ofMillis(fadeOut * 50)
                )
        );
        player.showTitle(title);
    }

    /**
     * Plays a custom sound for the player.
     *
     * @param player    The player to play the sound for.
     * @param soundName The name of the custom sound (e.g., "custom.sound.name").
     * @param master   The sound source (e.g., Sound.Source.MASTER).
     * @param volume    The volume of the sound (1.0 is normal volume).
     * @param pitch     The pitch of the sound (1.0 is normal pitch).
     */
    public static void playCustomSound(Player player, String soundName, Sound.Source master, float volume, float pitch) {
        String namespace = soundName.split(":")[0];
        String value = soundName.split(":")[1];
        player.playSound(
                Sound.sound(new Key() {
                    @Override
                    public @NotNull String namespace() {
                        return namespace;
                    }

                    @Override
                    public @NotNull String value() {
                        return value;
                    }

                    @Override
                    public @NotNull String asString() {
                        return soundName;
                    }
                }, master, volume, pitch)  // Custom sound name
        );
    }

}
