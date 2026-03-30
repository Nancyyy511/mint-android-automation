package core.config;

import core.exceptions.ConfigurationException;

import java.util.Properties;

public record EnvironmentConfig(Environment environment, String baseUrl) {
    public static EnvironmentConfig from(Environment environment, Properties properties) {
        String key = switch (environment) {
            case PRODUCTION -> "production.api.baseUrl";
            case PREPROD -> "preprod.api.baseUrl";
            case STAGING -> "staging.api.baseUrl";
        };

        String baseUrl = properties.getProperty(key, "").trim();
        if (baseUrl.isBlank()) {
            if (environment == Environment.STAGING) {
                baseUrl = properties.getProperty("preprod.api.baseUrl", "").trim();
            }
            if (baseUrl.isBlank()) {
                throw new ConfigurationException("Base URL is not configured for environment: " + environment.name().toLowerCase());
            }
        }

        return new EnvironmentConfig(environment, baseUrl);
    }
}
