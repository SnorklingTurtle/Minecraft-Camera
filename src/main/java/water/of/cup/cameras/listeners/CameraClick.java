package main.java.water.of.cup.cameras.listeners;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.Picture;
import main.java.water.of.cup.cameras.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CameraClick implements Listener {

    @EventHandler
    public void cameraClicked(PlayerInteractEvent e) {

        if (!Utils.isCamera(e.getItem()))
            return;

        e.setCancelled(true);

        if (!e.getAction().equals(Action.RIGHT_CLICK_AIR))
            return;

        Player p = e.getPlayer();
        Camera instance = Camera.getInstance();

        boolean messages = instance.getConfig().getBoolean("settings.messages.enabled");
        boolean usePerms = instance.getConfig().getBoolean("settings.camera.permissions");

        // check to make sure the player has paper
        if ((usePerms && p.hasPermission("cameras.paperRequired") && !p.getInventory().contains(Material.PAPER)) ||
                (!usePerms && !p.getInventory().contains(Material.PAPER))) {
            if (messages) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("settings.messages.nopaper")));
            }
            return;
        }

        boolean tookPicture = Picture.takePicture(p);

        boolean isPaperRequired = !usePerms || p.hasPermission("cameras.paperRequired");

        if (tookPicture && isPaperRequired) {
            // remove 1 paper from the player's inventory
            Map<Integer, ? extends ItemStack> paperHash = p.getInventory().all(Material.PAPER);
            for (ItemStack item : paperHash.values()) {
                item.setAmount(item.getAmount() - 1);
                break;
            }
        }

    }
}
