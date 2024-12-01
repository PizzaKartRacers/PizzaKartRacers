package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.UUID;

public class GamePlayer {

    UUID uuid;
    Kart kart;
    String item;
    int lap;


    /**
     * Constructor for GamePlayer
     * @param uuid the UUID of the player
     * @param kart the kart the player is using
     */
    public GamePlayer(UUID uuid, Kart kart) {
        this.uuid = uuid;
        this.kart = kart;
        this.lap = 1;
        this.item = "none";
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return the kart
     */
    public Kart getKart() {
        return kart;
    }

    /**
     * Set the kart
     * @param kart the kart to set
     */
    public void setKart(Kart kart) {
        this.kart = kart;
    }

    /**
     * Set the uuid
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the lap the player is on
     */
    public int getLap() {
        return lap;
    }

    /**
     * Set the lap the player is on
     * @param lap the lap to set
     */
    public void setLap(int lap) {
        this.lap = lap;
    }

    /**
     * Get the player's item
     * @return the player's item
     */
    public String getItem() {
        return item;
    }

    /**
     * Set the player's item
     * @param item the item to set
     */
    public void setItem(String item) {
        this.item = item;
    }

    public void createKart() {

        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(getUuid());
        Pos loc = player.getPosition();
        ItemStack jukeboxItem = ItemStack.of(Material.JUKEBOX);

        LivingEntity kart1 = new LivingEntity(EntityType.ARMOR_STAND);
        kart1.setInstance(player.getInstance());
        kart1.setHelmet(jukeboxItem);
        kart1.teleport(loc);
        kart1.spawn();
        kart1.addPassenger(player);
        kart1.setInvisible(true);
        kart1.setNoGravity(false);

        getKart().kartEntity = kart1;

    }


}
