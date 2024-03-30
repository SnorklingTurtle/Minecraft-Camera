package main.java.water.of.cup.cameras;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.awt.Color;

public class Renderer extends MapRenderer {
    private Camera instance = Camera.getInstance();

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (map.isLocked()) {
            return;
        }

        boolean renderAsync = instance.getConfig().getBoolean("settings.camera.renderAsync");
        if (renderAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(Camera.getInstance(), () -> renderMap(map, canvas, player));
        } else {
            renderMap(map, canvas, player);
        }

    }

    private void renderMap(MapView map, MapCanvas canvas, Player player) {
        Location eyes = player.getEyeLocation().clone();

        boolean transparentWater = instance.getConfig().getBoolean("settings.camera.transparentWater");
        boolean shadows = instance.getConfig().getBoolean("settings.camera.shadows");

        // get pitch and yaw of players head to calculate ray trace directions
        double pitch = -Math.toRadians(eyes.getPitch());
        double yaw = Math.toRadians(eyes.getYaw() + 90);

        byte[][] canvasBytes = new byte[128][128];

        // loop through every pixel on map
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {

                // calculate ray rotations
                double yrotate = -((y) * .9 / 128 - .45);
                double xrotate = ((x) * .9 / 128 - .45);

                Vector rayTraceVector = new Vector(Math.cos(yaw + xrotate) * Math.cos(pitch + yrotate),
                        Math.sin(pitch + yrotate), Math.sin(yaw + xrotate) * Math.cos(pitch + yrotate));

                RayTraceResult result = eyes.getWorld().rayTraceBlocks(eyes, rayTraceVector, 256);

                // Color change for liquids
                RayTraceResult liquidResult = eyes.getWorld().rayTraceBlocks(eyes, rayTraceVector, 256,
                        FluidCollisionMode.ALWAYS, false);
                double[] dye = new double[]{1, 1, 1}; // values color is multiplied by
                if (transparentWater) {
                    if (liquidResult != null) {
                        if (liquidResult.getHitBlock().getType().equals(Material.WATER))
                            dye = new double[]{0.2, 0.2, 1};
                        if (liquidResult.getHitBlock().getType().equals(Material.LAVA))
                            dye = new double[]{1, .3, .3};
                    }
                }

                if (result != null) {
                    byte lightLevel = result.getHitBlock().getRelative(result.getHitBlockFace()).getLightLevel();

                    if (lightLevel > 0 && shadows) {
                        double shadowLevel = 15.0;

                        for (int i = 0; i < dye.length; i++) {
                            dye[i] = dye[i] * (lightLevel / shadowLevel);
                        }
                    }

                    byte color;
                    if (transparentWater) {
                        color = Utils.colorFromType(result.getHitBlock(), dye);
                    } else {
                        color = Utils.colorFromType(liquidResult.getHitBlock(), dye);
                    }
                    canvas.setPixel(x, y, color);
                    canvasBytes[x][y] = color;
                } else if (liquidResult != null) {
                    // set map pixel to color of liquid block found
                    byte color = Utils.colorFromType(liquidResult.getHitBlock(), new double[]{1, 1, 1});
                    canvas.setPixel(x, y, color);
                    canvasBytes[x][y] = color;
                } else {
                    // no block was hit, so we will assume we are looking at the sky
                    byte skyColor = MapPalette.matchColor(getSkyColor(eyes.getWorld()));
                    canvas.setPixel(x, y, skyColor);
                    canvasBytes[x][y] = skyColor;
                }
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(Camera.getInstance(),
                () -> MapStorage.store(map.getId(), canvasBytes));

        map.setLocked(true);
    }

    private Color getSkyColor(World world) {
        if (world == null) return new Color(113, 156, 237);
        if (world.getName().contains("end")) return new Color(36, 20, 61);
        if (world.getName().contains("nether")) return new Color(44, 7, 7);
        return new Color(113, 156, 237);
    }

}
