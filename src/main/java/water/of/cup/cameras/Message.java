package main.java.water.of.cup.cameras;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Message {

    public static void show(Player player, String configKey)
    {
        Camera instance = Camera.getInstance();
        FileConfiguration config = instance.getConfig();
        boolean messages = instance.getConfig().getBoolean("settings.messages.enabled");

        if (messages) {
            String message = config.getString(configKey);
            if (message != null)
            {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }

}
