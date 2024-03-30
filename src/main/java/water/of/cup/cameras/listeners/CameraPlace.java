package main.java.water.of.cup.cameras.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import main.java.water.of.cup.cameras.Utils;
import org.bukkit.event.player.PlayerInteractEvent;

public class CameraPlace implements Listener {
    @EventHandler
    public void cameraPlaced(PlayerInteractEvent e) {
        // Prevent players from placing Cameras

        if (Utils.isCamera(e.getItem())) {
            e.setCancelled(true);
        }
    }
}
