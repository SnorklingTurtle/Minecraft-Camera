package main.java.water.of.cup.cameras;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapPalette;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Utils {

    @SuppressWarnings("deprecation")
    public static byte colorFromType(Block block, double[] dye) {
        Camera instance = Camera.getInstance();

        Color color = instance.colorMapping.getColorFromType(block, dye);
        if (color == null) {
            return MapPalette.GRAY_2; // no color was found, use gray
        }

        return MapPalette.matchColor(color);
    }

    public static boolean isCamera(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer()
                .has(Camera.getInstance().getCameraKey(), PersistentDataType.INTEGER);
    }

    public static void removePaperFromInventory(Player player, int amount)
    {
        // remove 1 paper from the player's inventory
        Map<Integer, ? extends ItemStack> paperHash = player.getInventory().all(Material.PAPER);
        for (ItemStack item : paperHash.values()) {
            item.setAmount(item.getAmount() - amount);
            break;
        }
    }
}
