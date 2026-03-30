package core.device;

public record DeviceInfo(String udid, DeviceType type, String model, String platformVersion, String status) {
    public boolean isReal() {
        return type == DeviceType.REAL;
    }

    public boolean isEmulator() {
        return type == DeviceType.EMULATOR;
    }

    public boolean isAvailable() {
        return "device".equalsIgnoreCase(status);
    }

    public boolean isOffline() {
        return "offline".equalsIgnoreCase(status);
    }

    public boolean isUnauthorized() {
        return "unauthorized".equalsIgnoreCase(status);
    }
}
