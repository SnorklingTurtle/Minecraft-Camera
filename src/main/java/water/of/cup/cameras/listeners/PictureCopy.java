package main.java.water.of.cup.cameras.listeners;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.Picture;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class PictureCopy implements Listener {

    Camera instance = Camera.getInstance();
    final int SLOT_OUT = 2;

    @EventHandler
    public void onPictureCopy(InventoryClickEvent e) {

        if (e.getClickedInventory() == null)
            return;

        if (e.getClickedInventory().getType() != InventoryType.CARTOGRAPHY)
            return;

        // Only check for clicks in the output slot
        if (e.getSlot() != SLOT_OUT)
            return;

        if (e.getCurrentItem() == null)
            return;

        ItemStack outItem = e.getClickedInventory().getItem(SLOT_OUT);

        if (outItem == null)
            return;

        // Is copy?
        if (outItem.getAmount() != 2)
            return;

        if (!Picture.isPicture(outItem))
            return;

        Bukkit.getLogger().info("Copy photo: " + e.getCurrentItem().getType().name());
    }


}
