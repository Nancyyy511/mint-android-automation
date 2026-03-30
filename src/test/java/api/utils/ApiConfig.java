package api.utils;

import core.config.ConfigManager;
import io.restassured.RestAssured;

public final class ApiConfig {
    private static final String BASE_URL = resolveBaseUrl();

    private ApiConfig() {
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getEnvironment() {
        return ConfigManager.getEnv();
    }

    public static String getUsername() {
        return ConfigManager.getRequired("api.username");
    }

    public static String getPassword() {
        return ConfigManager.getRequired("api.password");
    }

    public static String getRequired(String key) {
        return ConfigManager.getRequired(key);
    }

    public static String getOptional(String key, String defaultValue) {
        return ConfigManager.getOptional(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return ConfigManager.getInt(key, defaultValue);
    }

    private static String resolveBaseUrl() {
        String env = ConfigManager.getEnv();
        String baseUrl = ConfigManager.getBaseUrl();
        System.out.println("[API-CONFIG] Environment: " + env);
        System.out.println("[API-CONFIG] Base URL: " + baseUrl);
        RestAssured.baseURI = baseUrl;
        return baseUrl;
    }
}
