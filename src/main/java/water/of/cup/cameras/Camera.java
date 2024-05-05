package main.java.water.of.cup.cameras;

import main.java.water.of.cup.cameras.commands.CopyPictureCommand;
import main.java.water.of.cup.cameras.commands.FetchPictureCommand;
import main.java.water.of.cup.cameras.commands.TakePictureCommand;
import main.java.water.of.cup.cameras.listeners.*;
import main.java.water.of.cup.cameras.tabCompleter.FetchPictureTabCompleter;
import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;

import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class Camera extends JavaPlugin {

    private NamespacedKey cameraKey;
    private NamespacedKey recipeKey;
    private static Camera instance;
    List<Integer> mapIDs = new ArrayList<>();
    ColorMapping colorMapping = new ColorMapping();
    private FileConfiguration config;
    Connection dbConnection;


    @Override
    public void onEnable() {
        instance = this;

        cameraKey = new NamespacedKey(this, "camera"); //Key which identifies a token loot placeholder
        recipeKey = new NamespacedKey(this, "camera");

        loadConfig();
        this.colorMapping.initialize();

        dbConnection = MapStorageDB.connect();
        MapStorageDB.createTable(dbConnection);
        MapStorageDB.createCleanUpTrigger(dbConnection);
        ResultSet mapsResultSet = MapStorageDB.getAll(dbConnection);

        try {
            while (mapsResultSet.next())
            {
                int mapId = mapsResultSet.getInt("map_id");
                byte[] mapDataSerialized = mapsResultSet.getBytes("data");

                MapView mapView = Bukkit.getMap(mapId);
                if (mapView == null)
                    continue;

                mapView.setTrackingPosition(false);
                for (MapRenderer renderer : mapView.getRenderers())
                    mapView.removeRenderer(renderer);

                mapView.addRenderer(new MapRenderer() {
                    @Override
                    public void render(MapView mapViewNew, MapCanvas mapCanvas, Player player) {
                    if (!mapIDs.contains(mapId)) {
                        mapIDs.add(mapId);
                        byte[][] mapData = MapStorageDB.deserializeByteArray2d(mapDataSerialized);

                        for (int i = 0; i < mapData.length; i++) {
                            for (int j = 0; j < mapData[0].length; j++) {
                                byte colorByte = mapData[i][j];
                                mapCanvas.setPixel(i, j, colorByte);
                            }
                        }
                    }
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        // Commands
        getCommand("takePicture").setExecutor(new TakePictureCommand());
        getCommand("copyPicture").setExecutor(new CopyPictureCommand());

        PluginCommand fetchPictureCommand = getCommand("fetchPicture");
        if (fetchPictureCommand != null)
        {
            fetchPictureCommand.setExecutor(new FetchPictureCommand());
            fetchPictureCommand.setTabCompleter(new FetchPictureTabCompleter());
        }

        // Events
        registerListeners(
            new CameraClick(),
            new PlayerJoin(),
            new PrepareItemCraft(),
            new PictureCopy(),
            new PictureDestroy()
        );

        if (config.getBoolean("settings.camera.recipe.enabled"))
            addCameraRecipe();


        /*
        StringBuilder materials = new StringBuilder();
        for (Material mat : Material.values())
        {
            if (mat.isBlock() && !mat.isAir())
            {
                try
                {
                    org.bukkit.Color col = mat.createBlockData().getMapColor();
                    int colRed = col.getRed();
                    int colGreen = col.getGreen();
                    int colBlue = col.getBlue();
                    materials.append(mat.name()).append("=").append(colRed).append(",").append(colGreen).append(",").append(colBlue).append("\n"); //.append("\t\t\t\t\t").append(mat.isSolid() ? "Solid" : "").append("\n");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        try (PrintWriter out = new PrintWriter(getDataFolder().getAbsolutePath() + "/color-mapping.config.sample")) {
            out.println(materials);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        */
    }

    @Override
    public void onDisable() {
        /* Disable all current async tasks */
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.removeRecipe(recipeKey);

        MapStorageDB.disconnect(dbConnection);
    }

    private void registerListeners(Listener... listeners) {
        Arrays.stream(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    public static Camera getInstance() {
        return instance;
    }


    public void addCameraRecipe() {
        ItemStack camera = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta cameraMeta = (SkullMeta) camera.getItemMeta();

        PlayerProfile customProfile = Bukkit.createPlayerProfile(UUID.randomUUID(), "");
        PlayerTextures customPlayerTextures = customProfile.getTextures();

        // Get texture from URL
        try {
            String skinUrl = config.getString("settings.camera.skinUrl");
            if (skinUrl != null && skinUrl.trim().startsWith("http"))
            {
                customPlayerTextures.setSkin(new URL(skinUrl.trim()));
                customProfile.setTextures(customPlayerTextures);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (cameraMeta != null)
        {
            // Apply camera skin to item by setting the custom profile as the owner (https://www.spigotmc.org/threads/help-needed-with-skull-skin-issue.621928/)
            cameraMeta.setOwnerProfile(customProfile);

            // Name displayed in-game
            cameraMeta.setDisplayName(ChatColor.WHITE + "Camera");

            // Mark item as a camera - this way we can later check for its existence
            cameraMeta.getPersistentDataContainer().set(cameraKey, PersistentDataType.INTEGER, 1);
        }

        camera.setItemMeta(cameraMeta);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, camera);

        ArrayList<String> shapeArr = (ArrayList<String>) config.get("settings.camera.recipe.shape");
        recipe.shape(shapeArr.toArray(new String[shapeArr.size()]));

        for (String ingredientKey : config.getConfigurationSection("settings.camera.recipe.ingredients").getKeys(false)) {
            recipe.setIngredient(ingredientKey.charAt(0), Material.valueOf((String) config.get("settings.camera.recipe.ingredients." + ingredientKey)));
        }

        Bukkit.addRecipe(recipe);
    }

    private void loadConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File mapDir = new File(getDataFolder(), "maps");
        if (!mapDir.exists()) {
            mapDir.mkdir();
        }

        String filePath = "/config.yml";
        boolean hasConfig = FileUtils.fileExists(filePath);

        if (!hasConfig)
        {
            try {
                FileUtils.copyResource(filePath, filePath);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        File configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public Connection getDbConnection()
    {
        return dbConnection;
    }

    public List<Integer> getMapIDs()
    {
        return mapIDs;
    }

    public ColorMapping getColorMapping() {
        return this.colorMapping;
    }

    @Override
    public FileConfiguration getConfig() {

        return config;
    }

    public NamespacedKey getCameraKey() {
        return cameraKey;
    }
}
