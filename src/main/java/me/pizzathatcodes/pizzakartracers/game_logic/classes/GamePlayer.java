package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import fr.mrmicky.fastboard.FastBoard;
import me.pizzathatcodes.pizzakartracers.utils.util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GamePlayer {

    UUID uuid;
    Kart kart;
    FastBoard board;
    double driftDelay;
    String item;

    int lap;

    public GamePlayer(UUID uuid, Kart kart, FastBoard board) {
        this.uuid = uuid;
        this.kart = kart;
        this.lap = 1;
        this.board = board;
        this.item = util.translate("&7none");
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
     * Get the player's scoreboard
     * @return the player's scoreboard
     */
    public FastBoard getBoard() {
        return board;
    }

    /**
     * Set the player's scoreboard
     * @param board the scoreboard to set
     */
    public void setBoard(FastBoard board) {
        this.board = board;
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
        Player player = Bukkit.getPlayer(getUuid());
        Location loc = player.getLocation();
        ArmorStand armorStand = loc.getWorld().spawn(loc.clone().subtract(0, 0, 0), ArmorStand.class);

        // Make the armor stand invisible and not affected by gravity
        armorStand.setVisible(false);
        armorStand.setGravity(true);

        // Create a Jukebox ItemStack
        ItemStack jukebox = new ItemStack(Material.JUKEBOX);

        // Set the jukebox as the helmet (on the armor stand's head)
        armorStand.getEquipment().setHelmet(jukebox);

        getKart().kartEntity = armorStand;

        if(player.getVehicle() == null) {
            getKart().getKartEntity().setPassenger(player);
        }

    }
}
