package main.java.water.of.cup.cameras;

import org.bukkit.block.Block;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.awt.Color;

public class ColorMapping {

    private Properties colorMapping = new Properties();
    private  boolean isLoaded = false;
    String colorMappingResourceFile = "/color-mapping.config";
    String colorMappingDestinationFile = "/color-mapping.config";

    public void initialize()
    {
        if (!FileUtils.fileExists(colorMappingDestinationFile))
            copyConfig();

        colorMapping = FileUtils.getConfig(colorMappingDestinationFile);

        isLoaded = true;
    }

    public void copyConfig()
    {
        Camera instance = Camera.getInstance();
        try {
            FileUtils.copyResource(colorMappingResourceFile, colorMappingDestinationFile);
        }
        catch (IOException e) {
            instance.getLogger().warning(String.format("Could not copy '%s' into '%s'", colorMappingResourceFile, colorMappingDestinationFile));
        }
    }

    public Properties getColors()
    {
        return colorMapping;
    }

    public Color getColorFromType(Block block, double[] dye)
    {
        String colorString = colorMapping.getProperty(block.getType().name());
        if (colorString == null)
            return null;

        Color color = getColorFromString(colorString);
        int redColor = (int) (color.getRed() * dye[0]);
        int greenColor = (int) (color.getGreen() * dye[1]);
        int blueColor = (int) (color.getBlue() * dye[2]);

        if (redColor > 255) redColor = 255;
        if (greenColor > 255) greenColor = 255;
        if (blueColor > 255) blueColor = 255;
        return new Color(redColor, greenColor, blueColor);
    }

    private Color getColorFromString(String colorString)
    {
        int[] colorIntArray = new int[3];

        for (int i = 0; i < 3; i++)
        {
            String value = colorString.split(",")[i];
            colorIntArray[i] = Integer.parseInt(value);
        }

        return new Color(colorIntArray[0], colorIntArray[1], colorIntArray[2]);
    }

    public boolean isLoaded()
    {
        return isLoaded;
    }
}
