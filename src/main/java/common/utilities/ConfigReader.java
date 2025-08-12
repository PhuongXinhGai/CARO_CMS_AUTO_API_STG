package common.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static final String CONFIG_PATH = "src/main/resources/config.properties";
    private static Properties properties;

    static {
        try {
            properties = new Properties();
            FileInputStream inputStream = new FileInputStream(CONFIG_PATH);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}

