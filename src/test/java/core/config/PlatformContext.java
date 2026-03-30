package core.config;

public final class PlatformContext {
    private static final ThreadLocal<String> PLATFORM = new ThreadLocal<>();

    private PlatformContext() {
    }

    public static void setPlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            PLATFORM.remove();
            return;
        }
        PLATFORM.set(platform.trim().toLowerCase());
    }

    public static String getPlatformOrDefault() {
        String platform = PLATFORM.get();
        return platform == null || platform.isBlank()
                ? ConfigManager.getPlatform()
                : platform;
    }

    public static void clear() {
        PLATFORM.remove();
    }
}
