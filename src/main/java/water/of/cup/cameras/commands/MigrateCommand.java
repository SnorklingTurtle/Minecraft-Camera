package main.java.water.of.cup.cameras.commands;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.MapStorageDB;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MigrateCommand implements CommandExecutor {

    Camera instance = Camera.getInstance();
    List<Integer> mapIDs_OLD = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("migratepictures"))
        {
            return false;
        }

        Player player = ((Player)sender);

        long seed = player.getWorld().getSeed();
        if (args != null && args.length > 0 && !args[0].trim().isEmpty())
        {
            seed = Long.parseLong(args[0]);
        }

        int pictureCounter = 0;
        Connection dbConnection = instance.getDbConnection();

        instance.getLogger().info("Importing pictures from maps-folder into database, using seed " + seed + " ...");

        // TODO: Remove loading from files
        File folder = new File(instance.getDataFolder() + "/maps");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null)
        {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    int mapId = Integer.parseInt(file.getName().split("_")[1].split(Pattern.quote("."))[0]);
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String encodedData = br.readLine();

                        if (!mapIDs_OLD.contains(mapId)) {
                            mapIDs_OLD.add(mapId);

                            int x = 0;
                            int y = 0;
                            int skipsLeft = 0;
                            byte colorByte = 0;
                            byte[][] map = new byte[128][128];
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
                                    map[x][y] = colorByte;

                                    y++;
                                    if (y == 128) {
                                        y = 0;
                                        x++;
                                    }

                                    skipsLeft -= 1;
                                }
                            }

                            pictureCounter++;

                            // Cant know for sure how many copies the player has made, so it's just 99 to be on the safe side.
                            MapStorageDB.store(dbConnection, mapId, seed, map, null, 99);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        instance.getLogger().info("Migrated " + pictureCounter + " pictures (incl. ignored duplicates).");

        return true;
    }
}
