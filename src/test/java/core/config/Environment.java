package core.config;

public enum Environment {
    PRODUCTION,
    STAGING,
    PREPROD;

    public static Environment from(String value) {
        if (value == null || value.isBlank()) {
            return PRODUCTION;
        }

        try {
            return Environment.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return PRODUCTION;
        }
    }
}
