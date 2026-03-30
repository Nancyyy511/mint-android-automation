package TestObject;

import core.config.ConfigManager;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebDriverException;

import java.time.Duration;

public final class CrashUtils {
    private static final String FLOW = "CRASH_DETECTED";
    private static final int MIN_PAGE_SOURCE_LENGTH = 100;

    private CrashUtils() {
    }

    public enum CrashMode {
        AM_CRASH,
        FORCE_STOP
    }

    public static boolean isCrashTestingEnabled() {
        return ConfigReader.getBoolean("crashTestingEnabled", false);
    }

    public static void triggerCrash(AndroidDriver driver) {
        triggerCrash(driver, CrashMode.AM_CRASH);
    }

    public static void triggerCrash(AndroidDriver driver, CrashMode crashMode) {
        if (!isCrashTestingEnabled()) {
            FlowLogger.step(FLOW, "Crash testing is disabled. Skipping crash trigger.");
            return;
        }
        if (driver == null) {
            throw new IllegalArgumentException("AndroidDriver is required to trigger an app crash.");
        }

        String appPackage = ConfigManager.getAppPackage();
        String udid = DriverManager.getSessionConfig().udid();
        FlowLogger.step(FLOW, "Triggering app crash. package=" + appPackage + ", udid=" + udid + ", mode=" + crashMode);

        DriverManager.AdbCommandResult result = switch (crashMode) {
            case AM_CRASH -> DriverManager.executeAdbShell("am", "crash", appPackage);
            case FORCE_STOP -> DriverManager.executeAdbShell("am", "force-stop", appPackage);
        };

        logAdbResult("Crash command finished", result);

        if (!didAppCrash(driver)) {
            FlowLogger.step(FLOW, "Crash was not confirmed from driver state. package=" + appPackage + ", udid=" + udid);
        }
    }

    public static void relaunchAppAfterCrash() {
        relaunchAppAfterCrash(Duration.ofSeconds(15));
    }

    public static void relaunchAppAfterCrash(Duration timeout) {
        if (!isCrashTestingEnabled()) {
            FlowLogger.step(FLOW, "Crash testing is disabled. Skipping relaunch.");
            return;
        }

        String appPackage = ConfigManager.getAppPackage();
        FlowLogger.step(FLOW, "Relaunching app after crash. package=" + appPackage);

        if (!waitForAppToBeDown(timeout)) {
            FlowLogger.step(FLOW, "App was not fully confirmed down before relaunch. Continuing with recovery.");
        }

        if (isDriverSessionLost()) {
            FlowLogger.step(FLOW, "Appium session was lost after crash. Recreating driver session.");
            DriverManager.restartDriver();
        } else {
            DriverManager.relaunchApp();
        }

        FlowLogger.step(FLOW, "App relaunch completed after crash. package=" + appPackage);
    }

    public static boolean didAppCrash(AndroidDriver driver) {
        AppStatus status = inspectAppStatus(driver);
        boolean crashed = status.sessionLost()
                || status.pageSourceUnavailable()
                || status.activityMissing()
                || status.backgroundedOrOutOfApp()
                || status.killed();
        if (crashed) {
            FlowLogger.step(FLOW, "Crash detected: " + status.describe());
        } else {
            FlowLogger.step(FLOW, "Crash not detected: " + status.describe());
        }
        return crashed;
    }

    public static boolean waitForAppToBeDown(Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (didAppCrash(DriverManager.getDriver()) || isDriverSessionLost()) {
                return true;
            }
            sleep(500);
        }
        return didAppCrash(DriverManager.getDriver()) || isDriverSessionLost();
    }

    public static AppStatus inspectAppStatus(AndroidDriver driver) {
        if (driver == null) {
            return new AppStatus(true, true, true, true, true, "", "", "driver is null");
        }

        boolean sessionLost = false;
        boolean pageSourceUnavailable = false;
        boolean activityMissing = false;
        boolean backgroundedOrOutOfApp = false;
        boolean killed = false;
        String currentPackage = "";
        String currentActivity = "";
        String reason = "";

        try {
            driver.getSessionId();
            currentPackage = blankToEmpty(driver.getCurrentPackage());
            currentActivity = blankToEmpty(driver.currentActivity());
            String pageSource = blankToEmpty(driver.getPageSource());

            pageSourceUnavailable = pageSource.length() < MIN_PAGE_SOURCE_LENGTH;
            activityMissing = currentActivity.isBlank();

            ActivityUtils.ActivitySnapshot snapshot = ActivityUtils.captureSnapshot();
            backgroundedOrOutOfApp = !snapshot.inApp();
            killed = currentPackage.isBlank() && currentActivity.isBlank();
            if (pageSourceUnavailable) {
                reason = "page source length=" + pageSource.length();
            } else if (activityMissing) {
                reason = "current activity is blank";
            } else if (backgroundedOrOutOfApp) {
                reason = "app moved out of expected package/activity";
            }
        } catch (WebDriverException exception) {
            sessionLost = true;
            reason = summarizeException(exception);
        }

        return new AppStatus(sessionLost, pageSourceUnavailable, activityMissing, backgroundedOrOutOfApp, killed,
                currentPackage, currentActivity, reason);
    }

    public static boolean isDriverSessionLost() {
        AndroidDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return true;
        }

        try {
            driver.getSessionId();
            driver.getCurrentPackage();
            return false;
        } catch (WebDriverException exception) {
            FlowLogger.step(FLOW, "Driver session is no longer valid after crash: " + summarizeException(exception));
            return true;
        }
    }

    public static String describeDriverState() {
        return inspectAppStatus(DriverManager.getDriver()).describe();
    }

    private static void logAdbResult(String prefix, DriverManager.AdbCommandResult result) {
        FlowLogger.step(FLOW, prefix + ". exitCode=" + result.exitCode()
                + ", stdout=" + blankToNone(result.stdout())
                + ", stderr=" + blankToNone(result.stderr()));
    }

    private static String blankToNone(String value) {
        return value == null || value.isBlank() ? "<empty>" : value;
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted while validating crash state", exception);
        }
    }

    private static String summarizeException(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("\\s+", " ").trim();
    }

    public record AppStatus(boolean sessionLost,
                            boolean pageSourceUnavailable,
                            boolean activityMissing,
                            boolean backgroundedOrOutOfApp,
                            boolean killed,
                            String currentPackage,
                            String currentActivity,
                            String reason) {
        public String describe() {
            return "sessionLost=" + sessionLost
                    + ", pageSourceUnavailable=" + pageSourceUnavailable
                    + ", activityMissing=" + activityMissing
                    + ", backgroundedOrOutOfApp=" + backgroundedOrOutOfApp
                    + ", killed=" + killed
                    + ", currentPackage=" + blank(currentPackage)
                    + ", currentActivity=" + blank(currentActivity)
                    + ", reason=" + blank(reason);
        }

        private String blank(String value) {
            return value == null || value.isBlank() ? "<empty>" : value;
        }
    }
}
