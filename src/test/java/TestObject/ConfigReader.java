package TestObject;

import core.config.ConfigManager;

public final class ConfigReader {

    private ConfigReader() {
    }

    public static String get(String key) {
        return ConfigManager.getRequired(key);
    }

    public static int getInt(String key, int defaultValue) {
        return ConfigManager.getInt(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return ConfigManager.getBoolean(key, defaultValue);
    }

    public static String getOptional(String key, String defaultValue) {
        return ConfigManager.getOptional(key, defaultValue);
    }

    @Deprecated
    public static String getProperty(String key) {
        return get(key);
    }
}
