package main.java.water.of.cup.cameras;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;


public class Picture {

    private static boolean isBusy = false;

    public static boolean takePicture(Player p) {
        Camera instance = Camera.getInstance();

        if (!p.hasPermission("cameras.useitem")) return false;

        if (isBusy) {
            Message.show(p, "settings.messages.delay");
            return false;
        }

        if (p.getInventory().firstEmpty() == -1) { //check to make sure there is room in the inventory for the map
            Message.show(p, "settings.messages.invfull");
            return false;
        }

        if (!instance.getColorMapping().isLoaded()) {
            Message.show(p, "settings.messages.notready");
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

        // Keep track of map IDs
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
