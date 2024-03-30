package main.java.water.of.cup.cameras.commands;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.Picture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CameraCommands implements CommandExecutor {

    private Camera instance = Camera.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("takepicture"))
            return false;

        if (!instance.getConfig().getBoolean("settings.camera.permissions"))
            return false;

        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        if (!p.hasPermission("cameras.command")) {
            return false;
        }

        Picture.takePicture(p);

        return true;
    }

}
