package core.config;

import core.exceptions.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public final class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = load();
    private static final Environment ENVIRONMENT = resolveEnvironment();
    private static final EnvironmentConfig ENVIRONMENT_CONFIG = EnvironmentConfig.from(ENVIRONMENT, PROPERTIES);

    private ConfigManager() {
    }

    public static String getEnv() {
        return ENVIRONMENT.name().toLowerCase();
    }

    public static Environment getEnvironment() {
        return ENVIRONMENT;
    }

    public static String getBaseUrl() {
        String override = getOptional("baseUrl", getOptional("api.baseUrl", ""));
        String baseUrl = override.isBlank() ? ENVIRONMENT_CONFIG.baseUrl() : override;
        if (baseUrl.isBlank()) {
            throw new ConfigurationException("Base URL is not configured");
        }
        return baseUrl;
    }

    public static String getPlatformName() {
        return getOptional("platformName", "Android");
    }

    public static String getPlatform() {
        return getOptional("platform", "android").toLowerCase();
    }

    public static String getAppPackage() {
        return getRequired("appPackage");
    }

    public static String getAppActivity() {
        return getRequired("appActivity");
    }

    public static String getDevicePreference() {
        return getOptional("device", "auto").toLowerCase();
    }

    public static String getUdidOverride() {
        String lowercase = resolve("udid");
        if (!lowercase.isBlank()) {
            return lowercase;
        }
        return resolve("Udid");
    }

    public static String getRequired(String key) {
        String value = resolve(key);
        if (value.isBlank()) {
            throw new ConfigurationException("Missing configuration value for key: " + key);
        }
        return value;
    }

    public static String getOptional(String key, String defaultValue) {
        String value = resolve(key);
        return value.isBlank() ? defaultValue : value;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = resolve(key);
        return value.isBlank() ? defaultValue : Boolean.parseBoolean(value);
    }

    public static int getInt(String key, int defaultValue) {
        String value = resolve(key);
        if (value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("Invalid integer value for key: " + key + " -> " + value, exception);
        }
    }

    private static String resolve(String key) {
        return Optional.ofNullable(System.getProperty(key))
                .orElse(PROPERTIES.getProperty(key, ""))
                .trim();
    }

    private static Environment resolveEnvironment() {
        return Environment.from(Optional.ofNullable(System.getProperty("env"))
                .orElse(PROPERTIES.getProperty("env", "production")));
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new ConfigurationException("Unable to find " + CONFIG_FILE + " in classpath");
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new ConfigurationException("Unable to load " + CONFIG_FILE, exception);
        }
    }
}
