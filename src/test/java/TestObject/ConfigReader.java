package TestObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = loadProperties();

    private ConfigReader() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE + " in classpath");
            }

            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new RuntimeException("Unable to load " + CONFIG_FILE, exception);
        }
    }

    public static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));

        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing configuration value for key: " + key);
        }

        return value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    @Deprecated
    public static String getProperty(String key) {
        return get(key);
    }
}
