package main.java.water.of.cup.cameras;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.HashMap;

public class Picture {
    private static HashMap<Player, Long> delayMap = new HashMap<>();

    public static boolean takePicture(Player p) {
        Camera instance = Camera.getInstance();

        boolean messages = instance.getConfig().getBoolean("settings.messages.enabled");

        boolean usePerms = instance.getConfig().getBoolean("settings.camera.permissions");
        if (usePerms && !p.hasPermission("cameras.useitem")) return false;

        if (p.getInventory().firstEmpty() == -1) { //check to make sure there is room in the inventory for the map
            if (messages) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("settings.messages.invfull")));
            }
            return false;
        }

        if (instance.getResourcePackManager().isLoaded()) {
            if (instance.getConfig().getBoolean("settings.delay.enabled")) {
                if (!delayMap.containsKey(p)) {
                    delayMap.put(p, System.currentTimeMillis());
                } else {
                    int delay = instance.getConfig().getInt("settings.delay.amount");
                    if (System.currentTimeMillis() - delayMap.get(p) >= delay) {
                        delayMap.put(p, System.currentTimeMillis());
                    } else {
                        if (messages) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("settings.messages.delay")));
                        }
                        return false;
                    }
                }
            }
        } else {
            if (messages) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("settings.messages.notready")));
            }
            return false;
        }

        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.createMap(p.getWorld());

        mapView.setTrackingPosition(false);

        for (MapRenderer renderer : mapView.getRenderers())
            mapView.removeRenderer(renderer);

        Renderer customRenderer = new Renderer();
        mapView.addRenderer(customRenderer);
        mapMeta.setMapView(mapView);

        itemStack.setItemMeta(mapMeta);
        p.getInventory().addItem(itemStack);

        // Play capture sound
        p.playSound(p.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.5F, 2.0F);

        return true;
    }

}
