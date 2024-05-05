package main.java.water.of.cup.cameras.commands;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.MapStorageDB;
import main.java.water.of.cup.cameras.Picture;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CopyPictureCommand implements CommandExecutor {

    MapRenderer renderer;
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("copypicture"))
            return false;

        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        ItemStack itemStack = p.getInventory().getItemInMainHand();

        if (!Picture.isPicture(itemStack))
            return false;

        Integer mapId = Picture.getMapId(itemStack);
        if (mapId == null)
            return false;


//        MapView mapView = Bukkit.getMap(mapId);
//        if (mapView == null)
//            return false;
//
//        renderer = new MapRenderer() {
//            @Override
//            public void render(MapView mapViewNew, MapCanvas mapCanvas, Player player) {
//                if (mapViewNew.getId() != mapId)
//                    return;
//
//                BufferedImage bufferedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
//                Graphics graphics = bufferedImage.getGraphics();
//
//                for (int y = 0; y < 128; y++) {
//                    for (int x = 0; x < 128; x++) {
//                        Color color = mapCanvas.getPixelColor(x, y);
//                        graphics.setColor(color);
//                        graphics.fillRect(x, y, 1, 1);
//                    }
//                }
//
//                BufferedImage image = getScaledImage(bufferedImage, 512, 512);
//                Toolkit toolkit = Toolkit.getDefaultToolkit();
//                Clipboard clipboard = toolkit.getSystemClipboard();
//                clipboard.setContents(new ImageTransferable(image), null);
//
//                mapView.removeRenderer(renderer);
//            }
//        };
//        mapView.addRenderer(renderer);



        ResultSet rs = MapStorageDB.getById(Camera.getInstance().getDbConnection(), mapId);

        byte[] mapDataSerialized = null;
        try {
            if (rs.next())
            {
                mapDataSerialized = rs.getBytes("data");
            }
        }
        catch (SQLException ignore) { }

        byte[][] map = MapStorageDB.deserializeByteArray2d(mapDataSerialized);
        if (map == null)
            return false;

        BufferedImage sourceImage = getBufferedImage(map);
        BufferedImage image = getScaledImage(sourceImage, 512, 512);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        clipboard.setContents(new ImageTransferable(image), null);

        return true;
    }

    public BufferedImage getBufferedImage(byte[][] imageData) {
        int sourceWidth = imageData[0].length;
        int sourceHeight = imageData.length;

        BufferedImage bufferedImage = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();

        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                Color color = MapPalette.getColor(imageData[x][y]);
                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }

        return bufferedImage;
    }

    private BufferedImage getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    static final class ImageTransferable implements Transferable {
        final BufferedImage image;

        public ImageTransferable(final BufferedImage image) {
            this.image = image;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor};
        }

        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return image;
            }

            throw new UnsupportedFlavorException(flavor);
        }
    };
}


