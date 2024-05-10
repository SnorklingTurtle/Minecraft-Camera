package main.java.water.of.cup.cameras.tabCompleter;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.MapStorageDB;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FetchPictureTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && command.getName().equalsIgnoreCase("fetchpicture"))
        {
            int maxSuggestionAmount = 50;
            UUID tagger = ((Player)sender).getUniqueId();
            ResultSet rs = MapStorageDB.getTagsByPlayer(Camera.getInstance().getDbConnection(), tagger, maxSuggestionAmount);
            List<String> suggestedTags = new ArrayList<>();
            try {
                while (rs.next()) {
                    suggestedTags.add(rs.getString("tag"));
                }
            }
            catch (SQLException e)
            {
                e.getStackTrace();
            }

            return suggestedTags;
        }

        return null;
    }

}
