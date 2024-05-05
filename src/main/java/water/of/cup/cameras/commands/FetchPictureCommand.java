package main.java.water.of.cup.cameras.commands;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;


public class FetchPictureCommand implements CommandExecutor {

    Camera instance = Camera.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("fetchpicture"))
            return false;

        if (args == null)
            return false;

        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        FileConfiguration config = instance.getConfig();
        boolean messages = instance.getConfig().getBoolean("settings.messages.enabled");

        // check to make sure there is room in the inventory for the map
        if (player.getInventory().firstEmpty() == -1) {
            if (messages) {
                String message = config.getString("settings.messages.invfull");
                if (message != null)
                {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
            return false;
        }

        // check to make sure the player has paper
        if (!player.getInventory().contains(Material.PAPER) && !player.isOp()) {
            if (messages) {
                String message = config.getString("settings.messages.nopaper");
                if (message != null)
                {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
            return false;
        }

        int mapId;
        try {
            mapId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            // TODO: Cast not possible ...
            return false;
        }

        ItemStack itemMapFilled = new ItemStack(Material.FILLED_MAP);

        MapView mapView = Bukkit.getMap(mapId);
        if (mapView == null)
            return false;

        MapMeta mapMeta = (MapMeta) itemMapFilled.getItemMeta();
        if (mapMeta == null)
            return false;

        // TODO: is setting the world needed?
        // mapView.setWorld(player.getWorld());
        mapView.setTrackingPosition(false);
        mapView.setLocked(true);

        // Copy map
        mapMeta.setMapView(mapView);
        itemMapFilled.setItemMeta(mapMeta);

        // Remove 1 paper from players inventory
        Utils.removePaperFromInventory(player, 1);

        // Add map to inventory
        player.getInventory().addItem(itemMapFilled);





//        ResultSet rs = MapStorageDB.getById(instance.getDbConnection(), mapId);
//
//        byte[] mapDataSerialized = null;
//        try {
//            if (rs.next()) {
//                mapDataSerialized = rs.getBytes("data");
//            }
//        } catch (SQLException ignore) {
//        }
//
//        byte[][] map = MapStorageDB.deserializeByteArray2d(mapDataSerialized);
//        if (map == null)
//            return false;
//
//        // Create a new map
//        ItemStack mapFilled = new ItemStack(Material.FILLED_MAP);
//        MapView mapView = Bukkit.createMap(player.getWorld());
//        MapMeta mapMeta = (MapMeta) mapFilled.getItemMeta();
//        mapView.setTrackingPosition(false);
//
//        for (MapRenderer renderer : mapView.getRenderers())
//            mapView.removeRenderer(renderer);
//
//        if (mapMeta == null)
//            return false;
//
//        // Remove 1 paper from players inventory
//        Utils.removePaperFromInventory(player, 1);
//
//        // Render to the map by copying from the old map
//        mapView.addRenderer(new MapRenderer() {
//            @Override
//            public void render(MapView mapViewNew, MapCanvas mapCanvas, Player player) {
//                if (mapViewNew.isLocked())
//                    return;
//
//                mapViewNew.setLocked(true);
//
//                Bukkit.getLogger().info("Render: " + mapId);
//
//                for (int y = 0; y < 128; y++) {
//                    for (int x = 0; x < 128; x++) {
//                        mapCanvas.setPixel(x, y, map[x][y]);
//                    }
//                }
//            }
//        });
//
//        mapMeta.setMapView(mapView);
//        mapFilled.setItemMeta(mapMeta);
//
//        // Add map to inventory
//        player.getInventory().addItem(mapFilled);
//
//        // Keep track of map IDs
//        instance.getMapIDs().add(mapView.getId());

        return true;
    }
}