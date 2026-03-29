package api.utils;

import io.restassured.RestAssured;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApiConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = loadProperties();
    private static final String ACTIVE_ENV = resolveActiveEnv();
    private static final String BASE_URL = resolveBaseUrl();

    private ApiConfig() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = ApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Unable to find " + CONFIG_FILE + " in classpath");
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + CONFIG_FILE, exception);
        }
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getUsername() {
        return getRequired("api.username");
    }

    public static String getPassword() {
        return getRequired("api.password");
    }

    public static String getRequired(String key) {
        String value = getSystemOverrideOrConfig(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration value for key: " + key);
        }
        return value.trim();
    }

    public static String getOptional(String key, String defaultValue) {
        String value = getSystemOverrideOrConfig(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        String value = getSystemOverrideOrConfig(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    private static String getSystemOverrideOrConfig(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }

    private static String resolveActiveEnv() {
        String env = System.getProperty("env", "production").trim().toLowerCase();
        System.out.println("[API-CONFIG] Environment: " + env);
        return env;
    }

    private static String resolveBaseUrl() {
        String envSpecificKey = switch (ACTIVE_ENV) {
            case "production" -> "production.api.baseUrl";
            case "preprod" -> "preprod.api.baseUrl";
            default -> throw new IllegalStateException(
                    "Unsupported env value '" + ACTIVE_ENV + "'. Supported values: production, preprod"
            );
        };

        String baseUrl = getRequired(envSpecificKey);
        System.out.println("[API-CONFIG] Base URL: " + baseUrl);
        RestAssured.baseURI = baseUrl;
        return baseUrl;
    }
}
