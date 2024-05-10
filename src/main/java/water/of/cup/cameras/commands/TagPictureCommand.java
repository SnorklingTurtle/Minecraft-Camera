package main.java.water.of.cup.cameras.commands;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.MapStorageDB;
import main.java.water.of.cup.cameras.Message;
import main.java.water.of.cup.cameras.Picture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TagPictureCommand  implements CommandExecutor {

    Camera instance = Camera.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("tagpicture"))
            return false;

        if (args == null)
            return false;

        String tag = args[0];

        if (!tag.matches("[a-zA-Z0-9]*"))
            return false;

        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!Picture.isPicture(itemStack))
        {
            Message.show(player, "settings.messages.canttag");
            return false;
        }

        Integer mapId = Picture.getMapId(itemStack);
        if (mapId == null)
            return false;

        long seed = player.getWorld().getSeed();
        UUID tagger = player.getUniqueId();
        boolean hasSucceeded = MapStorageDB.updateTag(instance.getDbConnection(), mapId, seed, tag, tagger);

        if (!hasSucceeded)
        {
            Message.show(player, "settings.messages.tagused");
            return false;
        }

        List<String> lore = new ArrayList<>();
        lore.add("Tag: " + tag);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null)
        {
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }

        return true;
    }
}
