package TestObject;

import core.config.ConfigManager;
import core.device.DeviceInfo;
import core.device.DeviceManager;
import core.driver.AndroidCapabilitiesBuilder;
import core.driver.DriverFactory;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class DriverManager {
    private static final int DEFAULT_ADB_EXEC_TIMEOUT_MS = 120000;
    private static final int DEFAULT_DEVICE_READY_TIMEOUT_MS = 120000;
    private static final int DEFAULT_DEVICE_READY_POLL_MS = 2000;
    private static final int DEFAULT_DRIVER_INIT_RETRIES = 3;
    private static final int DEFAULT_DRIVER_RETRY_DELAY_MS = 5000;
    private static final int DEFAULT_ADB_COMMAND_TIMEOUT_MS = 30000;
    private static final int DEFAULT_UIAUTOMATOR2_SERVER_INSTALL_TIMEOUT_MS = 120000;
    private static final int DEFAULT_UIAUTOMATOR2_SERVER_LAUNCH_TIMEOUT_MS = 60000;
    private static final int DEFAULT_ANDROID_INSTALL_TIMEOUT_MS = 120000;

    private static final ThreadLocal<SessionConfig> SESSION_CONFIG =
            ThreadLocal.withInitial(SessionConfig::fromConfig);

    private DriverManager() {
    }

    public static void configureSession(String deviceName, String udid, Integer systemPort) {
        SESSION_CONFIG.set(SessionConfig.resolve(deviceName, udid, systemPort));
    }

    public static SessionConfig getSessionConfig() {
        return SESSION_CONFIG.get();
    }

    public static void initializeDriver() throws MalformedURLException {
        if (getDriver() != null) {
            return;
        }

        SessionConfig sessionConfig = getSessionConfig();
        FlowLogger.step("DRIVER", sessionConfig.isRealDevice()
                ? "Running on REAL device: " + sessionConfig.udid()
                : "Running on EMULATOR: " + sessionConfig.udid());
        List<URL> appiumServerUrls = resolveAppiumServerUrls();
        int maxAttempts = Math.max(1, ConfigReader.getInt("driverInitRetries", DEFAULT_DRIVER_INIT_RETRIES));
        int retryDelayMs = Math.max(0, ConfigReader.getInt("driverInitRetryDelayMs", DEFAULT_DRIVER_RETRY_DELAY_MS));
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logDeviceStatus(sessionConfig, "before session attempt " + attempt);
            waitUntilDeviceReady(sessionConfig);

            var options = AndroidCapabilitiesBuilder.build(sessionConfig);
            for (int urlIndex = 0; urlIndex < appiumServerUrls.size(); urlIndex++) {
                URL appiumServerUrl = appiumServerUrls.get(urlIndex);
                FlowLogger.step("DRIVER", "Starting Appium session attempt " + attempt + "/" + maxAttempts
                        + " using server=" + appiumServerUrl
                        + ", deviceName=" + sessionConfig.deviceName()
                        + ", udid=" + sessionConfig.udid()
                        + ", systemPort=" + sessionConfig.systemPort()
                        + ", adbExecTimeout=" + sessionConfig.adbExecTimeoutMs());
                try {
                    AndroidDriver driver = new AndroidDriver(appiumServerUrl, options);
                    disableHardwareKeyboardIme(driver);
                    setDriver(driver);
                    setWait(new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getInt("explicitWaitSeconds", 6))));
                    FlowLogger.step("DRIVER", "Appium session created successfully for udid="
                            + sessionConfig.udid() + ", systemPort=" + sessionConfig.systemPort()
                            + ", server=" + appiumServerUrl);
                    return;
                } catch (WebDriverException exception) {
                    lastFailure = exception;
                    FlowLogger.step("DRIVER", "Session creation failed on attempt " + attempt + "/" + maxAttempts
                            + " using server=" + appiumServerUrl
                            + " for udid=" + sessionConfig.udid() + ": " + summarizeException(exception));
                    cleanupFailedInitialization();

                    boolean hasAlternateServerUrl = urlIndex < appiumServerUrls.size() - 1;
                    if (hasAlternateServerUrl && isSessionEndpointNotFound(exception)) {
                        FlowLogger.step("DRIVER", "Received HTTP 404 while creating the session. "
                                + "Retrying with alternate Appium base path.");
                        continue;
                    }

                    break;
                }
            }

            if (attempt < maxAttempts) {
                sleep(retryDelayMs);
            }
        }

        throw lastFailure;
    }

    private static void waitUntilDeviceReady(SessionConfig sessionConfig) {
        int timeoutMs = Math.max(1000, ConfigManager.getInt("deviceReadyTimeoutMs", DEFAULT_DEVICE_READY_TIMEOUT_MS));
        int pollMs = Math.max(500, ConfigManager.getInt("deviceReadyPollMs", DEFAULT_DEVICE_READY_POLL_MS));
        Instant deadline = Instant.now().plusMillis(timeoutMs);

        runAdbCommand(sessionConfig, "wait-for-device");
        while (Instant.now().isBefore(deadline)) {
            String adbState = runAdbCommand(sessionConfig, "get-state").stdout();
            String sysBootCompleted = runAdbShell(sessionConfig, "getprop", "sys.boot_completed").stdout();
            String devBootComplete = runAdbShell(sessionConfig, "getprop", "dev.bootcomplete").stdout();
            String bootAnimation = runAdbShell(sessionConfig, "getprop", "init.svc.bootanim").stdout();
            String bootAnimationExit = runAdbShell(sessionConfig, "getprop", "service.bootanim.exit").stdout();

            boolean ready = "device".equalsIgnoreCase(adbState)
                    && "1".equals(sysBootCompleted)
                    && ("1".equals(devBootComplete) || devBootComplete.isBlank())
                    && ("stopped".equalsIgnoreCase(bootAnimation) || bootAnimation.isBlank())
                    && ("1".equals(bootAnimationExit) || bootAnimationExit.isBlank());
            if (ready) {
                FlowLogger.step("DRIVER", "Device ready for session start: udid=" + sessionConfig.udid()
                        + ", adbState=" + adbState
                        + ", sys.boot_completed=" + sysBootCompleted
                        + ", dev.bootcomplete=" + devBootComplete
                        + ", bootanim=" + bootAnimation
                        + ", service.bootanim.exit=" + bootAnimationExit);
                return;
            }

            FlowLogger.step("DRIVER", "Waiting for device readiness: udid=" + sessionConfig.udid()
                    + ", adbState=" + adbState
                    + ", sys.boot_completed=" + sysBootCompleted
                    + ", dev.bootcomplete=" + devBootComplete
                    + ", bootanim=" + bootAnimation
                    + ", service.bootanim.exit=" + bootAnimationExit);
            sleep(pollMs);
        }

        throw new RuntimeException("Timed out waiting for device readiness for udid=" + sessionConfig.udid());
    }

    private static void logDeviceStatus(SessionConfig sessionConfig, String phase) {
        AdbCommandResult state = runAdbCommand(sessionConfig, "get-state");
        AdbCommandResult model = runAdbShell(sessionConfig, "getprop", "ro.product.model");
        AdbCommandResult bootCompleted = runAdbShell(sessionConfig, "getprop", "sys.boot_completed");
        FlowLogger.step("DRIVER", "Device status " + phase + ": udid=" + sessionConfig.udid()
                + ", adbState=" + state.stdout()
                + ", model=" + model.stdout()
                + ", sys.boot_completed=" + bootCompleted.stdout());
        if (!state.stderr().isBlank()) {
            FlowLogger.step("DRIVER", "ADB stderr " + phase + ": " + state.stderr());
        }
    }

    private static void disableHardwareKeyboardIme(AndroidDriver driver) {
        try {
            driver.executeScript("mobile: shell", java.util.Map.of(
                    "command", "settings",
                    "args", List.of("put", "secure", "show_ime_with_hard_keyboard", "0")
            ));
            FlowLogger.step("DRIVER", "Disabled Android hardware keyboard IME overlay");
        } catch (Exception exception) {
            FlowLogger.step("DRIVER", "Could not disable Android hardware keyboard IME overlay");
        }
    }

    public static AndroidDriver getDriver() {
        return DriverFactory.getDriver();
    }

    public static void setDriver(AndroidDriver driver) {
        DriverFactory.setDriver(driver);
    }

    public static WebDriverWait getWait() {
        return DriverFactory.getWait();
    }

    static void setWait(WebDriverWait wait) {
        DriverFactory.setWait(wait);
    }

    public static void quitDriver() {
        try {
            DriverFactory.quitDriver();
        } finally {
            SESSION_CONFIG.remove();
            FlowLogger.step("DRIVER", "Driver disposed for thread " + Thread.currentThread().threadId());
        }
    }

    public static void restartDriver() {
        SessionConfig sessionConfig = getSessionConfig();
        cleanupFailedInitialization();
        configureSession(sessionConfig.deviceName(), sessionConfig.udid(), sessionConfig.systemPort());
        try {
            initializeDriver();
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Failed to restart Appium session for udid=" + sessionConfig.udid(), exception);
        }
    }

    public static void relaunchApp() {
        AndroidDriver driver = getDriver();
        if (driver == null) {
            restartDriver();
            return;
        }

        String appPackage = ConfigManager.getAppPackage();
        String appActivity = ConfigManager.getAppActivity();
        try {
            driver.activateApp(appPackage);
            FlowLogger.step("DRIVER", "App relaunched via activateApp for package=" + appPackage);
        } catch (Exception exception) {
            FlowLogger.step("DRIVER", "activateApp failed, falling back to startActivity for package="
                    + appPackage + ": " + summarizeException(exception));
            driver.executeScript("mobile: startActivity", java.util.Map.of(
                    "intent", appPackage + "/" + appActivity
            ));
            FlowLogger.step("DRIVER", "App relaunched via startActivity for package=" + appPackage);
        }
    }

    public static AdbCommandResult executeAdbCommand(String... args) {
        return runAdbCommand(getSessionConfig(), args);
    }

    public static AdbCommandResult executeAdbShell(String... shellArgs) {
        return runAdbShell(getSessionConfig(), shellArgs);
    }

    private static void cleanupFailedInitialization() {
        AndroidDriver driver = getDriver();
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception exception) {
            FlowLogger.step("DRIVER", "Ignoring cleanup failure after unsuccessful session creation: "
                    + summarizeException(exception));
        } finally {
            DriverFactory.clear();
        }
    }

    private static AdbCommandResult runAdbShell(SessionConfig sessionConfig, String... shellArgs) {
        String[] command = new String[shellArgs.length + 1];
        command[0] = "shell";
        System.arraycopy(shellArgs, 0, command, 1, shellArgs.length);
        return runAdbCommand(sessionConfig, command);
    }

    private static AdbCommandResult runAdbCommand(SessionConfig sessionConfig, String... args) {
        List<String> command = new ArrayList<>();
        command.add("adb");
        command.add("-s");
        command.add(sessionConfig.udid());
        for (String arg : args) {
            command.add(arg);
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false);
        try {
            Process process = builder.start();
            int commandTimeoutMs = Math.max(1000, ConfigManager.getInt("adbCommandTimeoutMs", DEFAULT_ADB_COMMAND_TIMEOUT_MS));
            boolean finished = process.waitFor(commandTimeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("adb command timed out after " + commandTimeoutMs + "ms: "
                        + String.join(" ", command));
            }
            String stdout = readStream(process.inputReader());
            String stderr = readStream(process.errorReader());
            int exitCode = process.exitValue();
            return new AdbCommandResult(exitCode, stdout.trim(), stderr.trim());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to execute adb command. Ensure adb is installed and on PATH.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("adb command interrupted", exception);
        }
    }

    private static String readStream(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!builder.isEmpty()) {
                builder.append(System.lineSeparator());
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private static String summarizeException(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("\\s+", " ").trim();
    }

    private static List<URL> resolveAppiumServerUrls() throws MalformedURLException {
        URL configuredUrl = new URL(ConfigManager.getRequired("appiumServer"));
        List<URL> urls = new ArrayList<>();
        urls.add(configuredUrl);

        String normalizedPath = normalizePath(configuredUrl.getPath());
        if (normalizedPath.isEmpty()) {
            urls.add(withPath(configuredUrl, "/wd/hub"));
        } else if ("/wd/hub".equals(normalizedPath)) {
            urls.add(withPath(configuredUrl, ""));
        }

        return urls;
    }

    private static URL withPath(URL url, String path) throws MalformedURLException {
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "";
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static boolean isSessionEndpointNotFound(WebDriverException exception) {
        String message = summarizeException(exception);
        return message.contains("Response code 404");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void sleep(int delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", exception);
        }
    }

    public record SessionConfig(String deviceMode, String deviceName, String udid, int systemPort, int adbExecTimeoutMs) {
        static SessionConfig fromConfig() {
            return fromDevice(DeviceManager.getAvailableDevice(), null, null);
        }

        static SessionConfig resolve(String deviceNameOverride, String udidOverride, Integer systemPortOverride) {
            DeviceInfo deviceInfo = DeviceManager.getAvailableDevice(udidOverride);
            return fromDevice(deviceInfo, deviceNameOverride, systemPortOverride);
        }

        private static SessionConfig fromDevice(DeviceInfo deviceInfo, String deviceNameOverride, Integer systemPortOverride) {
            String deviceMode = deviceInfo.isReal() ? "real" : "emulator";
            String udid = deviceInfo.udid();
            String deviceName = isBlank(deviceNameOverride) ? ConfigManager.getOptional(
                    deviceInfo.isReal() ? "real.deviceName" : "emulator.deviceName",
                    deviceInfo.model().isBlank() ? udid : deviceInfo.model()
            ) : deviceNameOverride.trim();
            int systemPort = systemPortOverride == null
                    ? ConfigManager.getInt("systemPort", resolveSystemPort(udid))
                    : systemPortOverride;
            int adbExecTimeoutMs = ConfigManager.getInt("adbExecTimeoutMs", DEFAULT_ADB_EXEC_TIMEOUT_MS);
            return new SessionConfig(deviceMode, deviceName, udid, systemPort, adbExecTimeoutMs);
        }

        boolean isRealDevice() {
            return "real".equalsIgnoreCase(deviceMode);
        }

        private static int resolveSystemPort(String udid) {
            int numericSuffix = extractNumericSuffix(udid);
            if (numericSuffix >= 0) {
                return 8200 + ((numericSuffix / 2) % 100);
            }
            long threadId = Thread.currentThread().threadId();
            return 8200 + (int) (threadId % 100);
        }

        private static int extractNumericSuffix(String value) {
            if (value == null || value.isBlank()) {
                return -1;
            }

            int cursor = value.length() - 1;
            while (cursor >= 0 && Character.isDigit(value.charAt(cursor))) {
                cursor--;
            }

            if (cursor == value.length() - 1) {
                return -1;
            }
            return Integer.parseInt(value.substring(cursor + 1));
        }
    }

    public record AdbCommandResult(int exitCode, String stdout, String stderr) {
    }
}
