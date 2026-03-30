package core.device;

import TestObject.FlowLogger;
import core.config.ConfigManager;
import core.exceptions.ConfigurationException;
import core.exceptions.DeviceNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class DeviceManager {
    private DeviceManager() {
    }

    public static DeviceInfo getAvailableDevice() {
        return getAvailableDevice(ConfigManager.getUdidOverride());
    }

    public static DeviceInfo getAvailableDevice(String requestedUdid) {
        List<DeviceInfo> detectedDevices = getConnectedDevices();
        List<DeviceInfo> availableDevices = detectedDevices.stream()
                .filter(DeviceInfo::isAvailable)
                .sorted(Comparator.comparing(DeviceInfo::type).thenComparing(DeviceInfo::udid))
                .toList();

        logDetectedDevices(detectedDevices);

        if (!isBlank(requestedUdid)) {
            Optional<DeviceInfo> requestedDevice = detectedDevices.stream()
                    .filter(device -> device.udid().equalsIgnoreCase(requestedUdid.trim()))
                    .findFirst();
            if (requestedDevice.isPresent() && requestedDevice.get().isAvailable()) {
                FlowLogger.step("DEVICE", "Requested UDID found in adb devices: " + requestedUdid.trim());
                return logSelection(requestedDevice.get());
            }
            if (requestedDevice.isPresent()) {
                FlowLogger.step("DEVICE", "Requested UDID is connected but not ready: " + requestedUdid.trim()
                        + " (status=" + requestedDevice.get().status() + "). Falling back to auto-detection.");
            } else {
                FlowLogger.step("DEVICE", "Requested UDID was not found in adb devices: " + requestedUdid.trim()
                        + ". Falling back to auto-detection.");
            }
        }

        String preference = ConfigManager.getDevicePreference();
        return switch (preference) {
            case "real" -> selectByType(availableDevices, DeviceType.REAL,
                    buildMissingDeviceMessage("No real Android devices available.", detectedDevices));
            case "emulator" -> selectByType(availableDevices, DeviceType.EMULATOR,
                    buildMissingDeviceMessage("No Android emulators available.", detectedDevices));
            case "auto" -> selectSmartDevice(availableDevices, detectedDevices);
            default -> throw new ConfigurationException("Unsupported device value: " + preference + ". Use auto, real, or emulator.");
        };
    }

    public static List<DeviceInfo> getConnectedDevices() {
        List<DeviceInfo> devices = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder("adb", "devices", "-l");

        try {
            Process process = builder.start();
            try (BufferedReader reader = process.inputReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    FlowLogger.step("DEVICE", "adb devices -l :: " + line);
                    DeviceInfo device = parse(line);
                    if (device != null) {
                        devices.add(device);
                    }
                }
            }
            process.waitFor();
        } catch (IOException exception) {
            throw new DeviceNotFoundException("Unable to execute adb devices. Ensure ADB is installed and available on PATH.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DeviceNotFoundException("Interrupted while discovering Android devices");
        }

        return devices.stream()
                .sorted(Comparator.comparing(DeviceInfo::udid))
                .toList();
    }

    public static List<DeviceInfo> getRealDevices() {
        return getConnectedDevices().stream()
                .filter(DeviceInfo::isAvailable)
                .filter(DeviceInfo::isReal)
                .toList();
    }

    public static List<DeviceInfo> getEmulators() {
        return getConnectedDevices().stream()
                .filter(DeviceInfo::isAvailable)
                .filter(DeviceInfo::isEmulator)
                .toList();
    }

    public static DeviceInfo selectSmartDevice() {
        return getAvailableDevice("");
    }

    private static DeviceInfo selectSmartDevice(List<DeviceInfo> availableDevices, List<DeviceInfo> detectedDevices) {
        if (availableDevices.isEmpty()) {
            throw new DeviceNotFoundException(buildMissingDeviceMessage("No Android devices available.", detectedDevices));
        }

        DeviceInfo selectedDevice = availableDevices.stream()
                .filter(DeviceInfo::isReal)
                .findFirst()
                .or(() -> availableDevices.stream().filter(DeviceInfo::isEmulator).findFirst())
                .orElseThrow(() -> new DeviceNotFoundException(buildMissingDeviceMessage("No Android devices available.", detectedDevices)));

        if (selectedDevice.isReal()) {
            FlowLogger.step("DEVICE", "Auto-detection selected a real device because at least one physical device is ready.");
        } else {
            FlowLogger.step("DEVICE", "Auto-detection selected an emulator because no real device is ready.");
        }
        return logSelection(selectedDevice);
    }

    private static DeviceInfo selectByType(List<DeviceInfo> devices, DeviceType type, String errorMessage) {
        return devices.stream()
                .filter(device -> device.type() == type)
                .findFirst()
                .map(device -> {
                    FlowLogger.step("DEVICE", "Device preference matched " + type.name().toLowerCase(Locale.ROOT)
                            + " device selection.");
                    return logSelection(device);
                })
                .orElseThrow(() -> new DeviceNotFoundException(errorMessage));
    }

    private static DeviceInfo parse(String line) {
        if (line == null || line.isBlank() || line.startsWith("List of devices attached")) {
            return null;
        }

        String normalized = line.trim();
        String[] tokens = normalized.split("\\s+");
        if (tokens.length < 2) {
            return null;
        }

        String udid = tokens[0];
        String status = tokens[1];
        DeviceType type = udid.startsWith("emulator-") ? DeviceType.EMULATOR : DeviceType.REAL;
        String model = extractModel(tokens, udid);
        String platformVersion = "device".equalsIgnoreCase(status) ? resolvePlatformVersion(udid) : "";
        return new DeviceInfo(udid, type, model, platformVersion, status);
    }

    private static String extractModel(String[] tokens, String fallback) {
        for (String token : tokens) {
            if (token.startsWith("model:")) {
                return token.substring("model:".length()).replace('_', ' ');
            }
        }
        return fallback;
    }

    private static void logDetectedDevices(List<DeviceInfo> devices) {
        if (devices.isEmpty()) {
            FlowLogger.step("DEVICE", "Detected devices: none");
            return;
        }

        for (DeviceInfo device : devices) {
            String category = device.isReal() ? "REAL" : "EMULATOR";
            FlowLogger.step("DEVICE", "Detected device: type=" + category
                    + ", udid=" + device.udid()
                    + ", status=" + device.status()
                    + (device.model().isBlank() ? "" : ", model=" + device.model())
                    + (device.platformVersion().isBlank() ? "" : ", android=" + device.platformVersion()));
        }
    }

    private static DeviceInfo logSelection(DeviceInfo device) {
        String label = device.isReal() ? "Selected REAL device: " : "Selected EMULATOR: ";
        FlowLogger.step("DEVICE", label + device.udid()
                + ", model=" + device.model()
                + ", status=" + device.status()
                + ", platformVersion=" + device.platformVersion());
        return device;
    }

    private static String buildMissingDeviceMessage(String prefix, List<DeviceInfo> detectedDevices) {
        if (detectedDevices.isEmpty()) {
            return prefix + " adb devices returned an empty list.";
        }

        long unauthorizedCount = detectedDevices.stream().filter(DeviceInfo::isUnauthorized).count();
        long offlineCount = detectedDevices.stream().filter(DeviceInfo::isOffline).count();
        StringBuilder message = new StringBuilder(prefix);
        if (unauthorizedCount > 0) {
            message.append(" Unauthorized devices=").append(unauthorizedCount).append('.');
        }
        if (offlineCount > 0) {
            message.append(" Offline devices=").append(offlineCount).append('.');
        }
        if (unauthorizedCount == 0 && offlineCount == 0) {
            message.append(" No ready devices were reported by adb.");
        }
        return message.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String resolvePlatformVersion(String udid) {
        try {
            Process process = new ProcessBuilder("adb", "-s", udid, "shell", "getprop", "ro.build.version.release").start();
            try (BufferedReader reader = process.inputReader()) {
                String version = reader.readLine();
                process.waitFor();
                return version == null ? "" : version.trim();
            }
        } catch (Exception ignored) {
            return "";
        }
    }
}
