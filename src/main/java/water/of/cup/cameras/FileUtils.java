package main.java.water.of.cup.cameras;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Logger;

public class FileUtils {

    public static boolean copyResource(String resourceFilePath, String destinationFilePath) throws IOException {
        Camera instance = Camera.getInstance();
        File folder = instance.getDataFolder();
        InputStream sourceStream = instance.getClass().getResourceAsStream(resourceFilePath);
        if (sourceStream == null)
            return false;

        Files.copy(sourceStream, Paths.get(folder + destinationFilePath), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public static boolean fileExists(String destinationFilePath)
    {
        Camera instance = Camera.getInstance();
        File folder = instance.getDataFolder();
        return Files.exists(Paths.get(folder + destinationFilePath));
    }

    public static Properties getConfig(String sourceFile)
    {
        Camera instance = Camera.getInstance();
        Logger log = instance.getLogger();
        Properties properties = new Properties();
        String configFilename = instance.getDataFolder() + sourceFile;

        try (FileInputStream colorMappingStream = new FileInputStream(configFilename)) {
            properties.load(colorMappingStream);
        }
        catch (IOException e)
        {
            log.severe("Config is missing or could not be opened: " + sourceFile);
            //e.printStackTrace();
        }

        return properties;
    }
}
