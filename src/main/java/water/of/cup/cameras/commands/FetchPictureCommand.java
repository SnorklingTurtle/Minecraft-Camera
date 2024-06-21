package main.java.water.of.cup.cameras.commands;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.MapStorageDB;
import main.java.water.of.cup.cameras.Message;
import main.java.water.of.cup.cameras.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

        // check to make sure there is room in the inventory for the map
        if (player.getInventory().firstEmpty() == -1) {
            Message.show(player, "settings.messages.invfull");
            return false;
        }

        // check to make sure the player has paper
        if (!player.hasPermission("cameras.paperless") && !player.getInventory().contains(Material.PAPER)) {
            Message.show(player, "settings.messages.nopaper");
            return false;
        }

        ItemStack itemMapFilled = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemMapFilled.getItemMeta();
        long currentSeed = player.getWorld().getSeed();

        String tag = args[0];
        if (tag == null || tag.isEmpty() || mapMeta == null) {
            Message.show(player, "settings.messages.cantfetch");
            return false;
        }

        ResultSet rs = MapStorageDB.getByTag(instance.getDbConnection(), tag);

        long sourceSeed = 0;
        int sourceMapId = 0;
        String sourcePhotographer = null;
        byte[] mapDataSerialized = null;
        try {
            if (rs.next()) {
                mapDataSerialized = rs.getBytes("data");
                sourceMapId = rs.getInt("map_id");
                sourceSeed = rs.getLong("seed");
                sourcePhotographer = rs.getString("photographer");
            }
            else
            {
                Message.show(player, "settings.messages.cantfetch");
                return false;
            }
        } catch (SQLException e) {
            e.getStackTrace();
            return false;
        }

        // Check whether map_id exist within this seed
        MapView mapView = Bukkit.getMap(sourceMapId);
        if (mapView != null)
        {
            MapStorageDB.updateCounter(instance.getDbConnection(), sourceMapId, currentSeed, 1);
        }
        else
        {
            // Map does not exist in current seed, re-render it and move
            // tag to the newly rendered picture. Next time the picture is fetched
            // within the same seed, we won't need to re-render it.

            byte[][] map = MapStorageDB.deserializeByteArray2d(mapDataSerialized);
            if (map == null)
                return false;

            // Create a new map
            mapView = Bukkit.createMap(player.getWorld());

            for (MapRenderer renderer : mapView.getRenderers())
                mapView.removeRenderer(renderer);

            mapView.setTrackingPosition(false);

            // Render to the new map by copying from the old map
            mapView.addRenderer(new MapRenderer() {
                @Override
                public void render(@NonNull MapView mapViewNew, @NonNull MapCanvas mapCanvas, @NonNull Player player) {
                    if (mapViewNew.isLocked())
                        return;

                    mapViewNew.setLocked(true);

                    for (int y = 0; y < 128; y++) {
                        for (int x = 0; x < 128; x++) {
                            mapCanvas.setPixel(x, y, map[x][y]);
                        }
                    }
                }
            });

            UUID tagger = player.getUniqueId();
            UUID photographer = sourcePhotographer != null ? UUID.fromString(sourcePhotographer) : null;

            // Remove tag from old picture
            MapStorageDB.updateTag(instance.getDbConnection(), sourceMapId, sourceSeed, null, null);

            // Store new picture with old tag
            MapStorageDB.store(instance.getDbConnection(), mapView.getId(), currentSeed, map, photographer, 1, tag, tagger);
        }

        // Add lore
        List<String> lore = new ArrayList<>();
        lore.add(String.format("Tag: %s", tag));
        mapMeta.setLore(lore);

        // Copy map
        mapMeta.setMapView(mapView);
        itemMapFilled.setItemMeta(mapMeta);

        if (!player.hasPermission("cameras.paperless"))
        {
            // Remove 1 paper from players inventory
            Utils.removePaperFromInventory(player, 1);
        }

        // Add map to inventory
        player.getInventory().addItem(itemMapFilled);

        return true;
    }
}