package main.java.water.of.cup.cameras;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.util.Map;

public class Utils {

    public static Color colorFromType(Block block, double[] dye) {
        Camera instance = Camera.getInstance();
        Color color = instance.colorMapping.getColorFromType(block, dye);
        return color == null ? Color.gray : color;
    }

    public static boolean isCamera(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer()
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
