package main.java.water.of.cup.cameras;

import main.java.water.of.cup.cameras.commands.CameraCommands;
import main.java.water.of.cup.cameras.listeners.CameraClick;
import main.java.water.of.cup.cameras.listeners.PlayerJoin;
import main.java.water.of.cup.cameras.listeners.PrepareItemCraft;
import org.bukkit.*;
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
import java.util.*;
import java.util.regex.Pattern;

import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class Camera extends JavaPlugin {

    private NamespacedKey cameraKey;
    private NamespacedKey recipeKey;
    private static Camera instance;
    List<Integer> mapIDsNotToRender = new ArrayList<>();
    ColorMapping colorMapping = new ColorMapping();
    private File configFile;
    private FileConfiguration config;


    @Override
    public void onEnable() {
        instance = this;

        cameraKey = new NamespacedKey(this, "camera"); //Key which identifies a token loot placeholder
        recipeKey = new NamespacedKey(this, "camera");

        loadConfig();
        this.colorMapping.initialize();

        File folder = new File(getDataFolder() + "/maps");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                int mapId = Integer.parseInt(file.getName().split("_")[1].split(Pattern.quote("."))[0]);
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String encodedData = br.readLine();

                    MapView mapView = Bukkit.getMap(Integer.valueOf(mapId));

                    mapView.setTrackingPosition(false);
                    for (MapRenderer renderer : mapView.getRenderers())
                        mapView.removeRenderer(renderer);

                    mapView.addRenderer(new MapRenderer() {
                        @Override
                        public void render(MapView mapViewNew, MapCanvas mapCanvas, Player player) {
                            if (!mapIDsNotToRender.contains(mapId)) {
                                mapIDsNotToRender.add(mapId);

                                int x = 0;
                                int y = 0;
                                int skipsLeft = 0;
                                byte colorByte = 0;
                                for (int index = 0; index < encodedData.length(); index++) {
                                    if (skipsLeft == 0) {
                                        int end = index;

                                        while (encodedData.charAt(end) != ',')
                                            end++;

                                        String str = encodedData.substring(index, end);
                                        index = end;

                                        colorByte = Byte.parseByte(str.substring(0, str.indexOf('_')));

                                        skipsLeft = Integer.parseInt(str.substring(str.indexOf('_') + 1));

                                    }

                                    while (skipsLeft != 0) {
                                        mapCanvas.setPixel(x, y, colorByte);

                                        y++;
                                        if (y == 128) {
                                            y = 0;
                                            x++;
                                        }

                                        skipsLeft -= 1;
                                    }
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        getCommand("takePicture").setExecutor(new CameraCommands());
        registerListeners(new CameraClick(), new PlayerJoin(), new PrepareItemCraft());

        if (config.getBoolean("settings.camera.recipe.enabled"))
            addCameraRecipe();
    }

    @Override
    public void onDisable() {
        /* Disable all current async tasks */
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.removeRecipe(recipeKey);
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
        // Bukkit.getOnlinePlayers().forEach((Player player) -> player.discoverRecipe(recipeKey));
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

        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public  ColorMapping getColorMapping() {
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
