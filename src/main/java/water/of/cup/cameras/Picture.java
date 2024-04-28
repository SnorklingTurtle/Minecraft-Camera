package main.java.water.of.cup.cameras;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.HashMap;

public class Picture {

    private static boolean isBusy = false;

    public static boolean takePicture(Player p) {
        Camera instance = Camera.getInstance();
        boolean messages = instance.getConfig().getBoolean("settings.messages.enabled");

        if (!p.hasPermission("cameras.useitem")) return false;

        FileConfiguration config = instance.getConfig();

        if (isBusy) {
            if (messages) {
                String message = config.getString("settings.messages.delay");
                if (message != null)
                {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
            return false;
        }

        if (p.getInventory().firstEmpty() == -1) { //check to make sure there is room in the inventory for the map
            if (messages) {
                String message = config.getString("settings.messages.invfull");
                if (message != null)
                {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
            return false;
        }

        if (!instance.getColorMapping().isLoaded()) {
            if (messages) {
                String message = config.getString("settings.messages.notready");
                if (message != null)
                {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
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

        instance.getMapIDs().add(mapView.getId());

        // Play capture sound
        p.playSound(p.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.5F, 2.0F);

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPicture(ItemStack itemStack)
    {
        if (itemStack.getType() != Material.FILLED_MAP)
            return false;

        Integer mapId = Picture.getMapId(itemStack);
        if (mapId == null)
            return false;

        return Camera.getInstance().getMapIDs().contains(mapId);
    }

    public static Integer getMapId(ItemStack item)
    {
        MapMeta mapMeta = (MapMeta) item.getItemMeta();
        if (mapMeta == null || mapMeta.getMapView() == null)
            return null;

        return mapMeta.getMapView().getId();
    }

    public static void setBusy(boolean isBusy) {
        Picture.isBusy = isBusy;
    }

}
