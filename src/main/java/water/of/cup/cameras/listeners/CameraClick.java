package main.java.water.of.cup.cameras.listeners;

import main.java.water.of.cup.cameras.Message;
import main.java.water.of.cup.cameras.Picture;
import main.java.water.of.cup.cameras.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CameraClick implements Listener {

    @EventHandler
    public void cameraClicked(PlayerInteractEvent e) {

        if (!Utils.isCamera(e.getItem()))
            return;

        e.setCancelled(true);

        if (!e.getAction().equals(Action.RIGHT_CLICK_AIR))
            return;

        Player p = e.getPlayer();
        boolean isOp = p.isOp();

        // check to make sure the player has paper
        if (!p.getInventory().contains(Material.PAPER) && !isOp) {
            Message.show(p, "settings.messages.nopaper");
            return;
        }

        boolean tookPicture = Picture.takePicture(p);

        if (tookPicture && !isOp) {
            // remove 1 paper from the player's inventory
            Utils.removePaperFromInventory(p, 1);
        }

    }
}
