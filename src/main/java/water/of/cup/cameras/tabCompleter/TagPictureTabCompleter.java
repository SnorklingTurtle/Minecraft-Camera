package main.java.water.of.cup.cameras.tabCompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TagPictureTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && command.getName().equalsIgnoreCase("tagpicture"))
        {
            // TODO: list random suggestions
            return new ArrayList<>();
        }

        return null;
    }

}
