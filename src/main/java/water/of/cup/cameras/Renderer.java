package main.java.water.of.cup.cameras;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.awt.Color;
import java.util.*;

interface Callback {
    void execute();
}

public class Renderer extends MapRenderer {
    private final Camera instance = Camera.getInstance();
    private final Set<Coordinate> pickedCoordinates = new HashSet<>();
    private final Random random = new Random();
    UUID playerUUID;
    Location eyes;
    boolean transparentWater;
    boolean shadows;
    double pitch;
    double yaw;
    int tracesPerTick;
    int renderDistance;
    byte[][] canvasBytes;
    MapCanvas canvas;
    MapView map;
    World world;
    long time;
    private int currentX = 0;
    private int currentY = 0;
    BukkitTask task;
    boolean isRenderingRandomly = true;
    boolean isDebugging = false;

    // Overworld sky colors
    private static final Color[] skyColors = {
            new Color(113, 194, 237),  // Morning
            new Color(52, 113, 227),   // Noon
            new Color(101, 97, 207),   // Night
            new Color(45, 56, 74),     // Midnight
    };

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (map.isLocked()) {
            return;
        }

        Picture.setBusy(true);

        map.setLocked(true);

        this.world = player.getWorld();
        this.canvas = canvas;
        this.map = map;
        this.time = world.getTime();
        isDebugging = instance.getConfig().getBoolean("settings.debug");

        playerUUID = player.getUniqueId();
        eyes = player.getEyeLocation().clone();

        isRenderingRandomly = instance.getConfig().getBoolean("settings.camera.renderRandomly");
        tracesPerTick = instance.getConfig().getInt("settings.camera.tracesPerTick");
        renderDistance = instance.getConfig().getInt("settings.camera.renderDistance");
        transparentWater = instance.getConfig().getBoolean("settings.camera.transparentWater");
        shadows = instance.getConfig().getBoolean("settings.camera.shadows");

        // get pitch and yaw of players head to calculate ray trace directions
        pitch = -Math.toRadians(eyes.getPitch());
        yaw = Math.toRadians(eyes.getYaw() + 90);

        canvasBytes = new byte[128][128];

        currentX = 0;
        currentY = 0;

        if (isDebugging)
            instance.getLogger().info("--- Render Begin ---");

        // Schedule the task to run every tick (20 times per second)
        int tickRate = 1;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long startTime = System.nanoTime();

                if (isRenderingRandomly)
                    renderMapSectionRandomly(() -> onRenderComplete());
                else
                    renderMapSectionSequentially(() -> onRenderComplete());

                long endTime = System.nanoTime();
                double duration = (endTime - startTime) * 1e-6;
                if (isDebugging)
                    instance.getLogger().info("Render Time (ms): " + duration);
            }
        }.runTaskTimer(instance, 0, tickRate);
    }

    private void onRenderComplete()
    {
        // Save to DB
        Bukkit.getScheduler().runTaskAsynchronously(instance,
            () -> MapStorageDB.store(instance.getDbConnection(), map.getId(), world.getSeed(), canvasBytes, playerUUID, 1));

        // Stop render task
        task.cancel();

        Picture.setBusy(false);
    }

    private void renderMapSectionRandomly(Callback onComplete) {

        for (int i = 0; i < tracesPerTick; i++)
        {
            // Check if all coordinates have been picked
            if (pickedCoordinates.size() >= 128 * 128) {
                onComplete.execute();
                return;
            }

            // Generate a random coordinate
            Coordinate coordinate;
            do {
                coordinate = new Coordinate(random.nextInt(128), random.nextInt(128));
            } while (pickedCoordinates.contains(coordinate));

            performRayTrace(coordinate.x, coordinate.y);

            // Add the picked coordinate to the set
            pickedCoordinates.add(coordinate);
        }
    }

    private void renderMapSectionSequentially(Callback onComplete) {
        for (int i = 0; i < tracesPerTick; i++) {
            // Check if all coordinates have been picked
            if (currentX >= 128 || currentY >= 128) {
                onComplete.execute();
                return;
            }

            performRayTrace(currentX, currentY);

            // Move to the next coordinate
            currentX++;
            if (currentX >= 128) {
                currentX = 0;
                currentY++;
            }
        }
    }

    private static class Coordinate {
        private final int x;
        private final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    void performRayTrace(int x, int y)
    {
        // calculate ray rotations
        double yrotate = -((y) * .9 / 128 - .45);
        double xrotate = ((x) * .9 / 128 - .45);

        Vector rayTraceVector = new Vector(Math.cos(yaw + xrotate) * Math.cos(pitch + yrotate),
                Math.sin(pitch + yrotate), Math.sin(yaw + xrotate) * Math.cos(pitch + yrotate));

        RayTraceResult result = world.rayTraceBlocks(eyes, rayTraceVector, renderDistance);

        // Color change for liquids
        RayTraceResult liquidResult = world.rayTraceBlocks(eyes, rayTraceVector, renderDistance,
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

            // TODO: Perhaps use getMapColor() instead
            // Color blockColor = result.getHitBlock().getBlockData().getMapColor().asRGB();
            // canvas.setPixelColor(x, y, new Color(blockColor));

            if (result.getHitBlock().getBlockPower() > 0)
            {
                if (result.getHitBlock().getType().equals(Material.REDSTONE_LAMP))
                    dye = new double[]{1.85, 1.85, 1.85};
            }

            Color color;
            if (transparentWater) {
                color = Utils.colorFromType(result.getHitBlock(), dye);
            } else {
                color = Utils.colorFromType(liquidResult.getHitBlock(), dye);
            }

            canvas.setPixelColor(x, y, color);
            canvasBytes[x][y] = ColorPalette.matchColor(color);
        } else if (liquidResult != null) {
            // set map pixel to color of liquid block found
            Color color = Utils.colorFromType(liquidResult.getHitBlock(), new double[]{1, 1, 1});
            canvas.setPixelColor(x, y, color);
            canvasBytes[x][y] = ColorPalette.matchColor(color);
        } else {
            // no block was hit, so we will assume we are looking at the sky
            Color skyColor = getSkyColor(world, y);
            canvas.setPixelColor(x, y, skyColor);
            canvasBytes[x][y] = ColorPalette.matchColor(skyColor);
        }
    }

    private Color getSkyColor(World world, int y) {
        Color dayColor = skyColors[1];
        if (world == null) return dayColor;
        if (world.getName().contains("end")) return new Color(36, 20, 61);
        if (world.getName().contains("nether")) return new Color(44, 7, 7);

        Color interpolated = interpolateColor((int)time);
        return getSkyGradientColor(interpolated, y, time > 12000 ? 128 : 190);
    }

    private static Color getSkyGradientColor(Color color, int y, float maxBrightness)
    {
        float brightnessFactor = y / Math.min(maxBrightness, 255);
        int red = (int)(color.getRed() + (255 - color.getRed()) * brightnessFactor);
        int green = (int)(color.getGreen() + (255 - color.getGreen()) * brightnessFactor);
        int blue = (int)(color.getBlue() + (255 - color.getBlue()) * brightnessFactor);
        return new Color(
                Math.min(red, 255),
                Math.min(green, 255),
                Math.min(blue, 255)
        );
    }

    public static Color interpolateColor(int value) {
        value = Math.min(Math.max(value, 0), 24000);

        // Determine the index of the first color
        int index = (int) ((double) value / 6000);

        // Calculate the fractional part of the value within the range of each color segment
        double fraction = (double) (value % 6000) / 6000;

        // Determine the two colors to interpolate between
        Color color1 = skyColors[index];
        Color color2 = skyColors[(index + 1) % skyColors.length];

        // Perform linear interpolation between the two colors
        int red = (int) (color1.getRed() + fraction * (color2.getRed() - color1.getRed()));
        int green = (int) (color1.getGreen() + fraction * (color2.getGreen() - color1.getGreen()));
        int blue = (int) (color1.getBlue() + fraction * (color2.getBlue() - color1.getBlue()));

        return new Color(red, green, blue);
    }

}
