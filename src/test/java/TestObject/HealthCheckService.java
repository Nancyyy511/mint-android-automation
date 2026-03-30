package TestObject;

import PageObject.P02_LoginPage;
import api.utils.RetryUtils;
import org.testng.Assert;
import utils.AllureUtils;
import utils.ScreenshotUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HealthCheckService {
    private static final String FLOW = "HEALTH_CHECK";
    private static final String CRASH = "CRASH_DETECTED";
    private static final String RECOVERY = "RECOVERY";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final P02_LoginPage loginPage = new P02_LoginPage();
    private final LoginFlow loginFlow = new LoginFlow();
    private final String testName;
    private boolean loginCompleted;

    public HealthCheckService(String testName) {
        this.testName = testName;
    }

    public AppState detectCurrentAppState() {
        if (loginPage.isHomeDisplayed()) {
            return AppState.HOME;
        }

        P02_LoginPage.PostLoginState state = loginPage.detectPostLoginState();
        return switch (state) {
            case HOME -> AppState.HOME;
            case PIN -> AppState.PIN;
            case SECURITY_QUESTION -> AppState.SECURITY_QUESTION;
            case LOGIN_FORM -> AppState.LOGIN;
            case UNKNOWN -> loginPage.isLoginScreenDisplayed() ? AppState.LOGIN : AppState.UNKNOWN;
        };
    }

    public boolean isAppAlive() {
        HealthSnapshot snapshot = captureSnapshot();
        if (!snapshot.alive()) {
            log("WARN", CRASH, "app-alive", null, "App is not alive. reason=" + snapshot.signal()
                    + ", details=" + snapshot.details());
            return false;
        }

        log("INFO", FLOW, "app-alive", null, "App is responsive");
        return true;
    }

    public void validateAppStability(String context, String orderId) {
        validateAppStability(context, orderId, Duration.ofSeconds(6));
    }

    public void validateAppStability(String context, String orderId, Duration gracePeriod) {
        HealthSnapshot snapshot;
        try {
            snapshot = RetryUtils.until(
                    "app stability for " + context,
                    this::captureSnapshot,
                    HealthSnapshot::alive,
                    gracePeriod,
                    Duration.ofMillis(500)
            );
        } catch (AssertionError exception) {
            HealthSnapshot latestSnapshot = captureSnapshot();
            failFast(latestSnapshot.signal(),
                    "App remained unstable after grace period",
                    context,
                    orderId,
                    latestSnapshot);
            return;
        }

        if (!snapshot.alive()) {
            failFast(snapshot.signal(), "App remained unstable after grace period. " + snapshot.details(),
                    context, orderId, snapshot);
        }

        if (loginCompleted && snapshot.appState() == AppState.LOGIN) {
            failFast(CrashSignal.OUT_OF_APP,
                    "Unexpected navigation back to login after successful login",
                    context,
                    orderId,
                    snapshot);
        }

        log("INFO", FLOW, "stability-check", orderId,
                "Stability check passed. context=" + context + ", appState=" + snapshot.appState());
    }

    public void markLoginCompleted(boolean value) {
        this.loginCompleted = value;
    }

    public void ensureHomeState() {
        Assert.assertTrue(detectCurrentAppState() == AppState.HOME, "Home screen should be displayed");
    }

    public void runCrashRecoveryCheck(String username, String password) {
        log("INFO", RECOVERY, "crash-recovery-start", null, "Running optional crash recovery check");
        CrashUtils.triggerCrash(DriverManager.getDriver(), CrashUtils.CrashMode.AM_CRASH);

        if (!CrashUtils.waitForAppToBeDown(Duration.ofSeconds(10))) {
            failFast(CrashSignal.NONE, "Crash trigger did not bring the app down", "optional crash recovery", null, captureSnapshot());
        }

        log("WARN", CRASH, "crash-confirmed", null, CrashUtils.describeDriverState());
        CrashUtils.relaunchAppAfterCrash(Duration.ofSeconds(15));
        markLoginCompleted(false);
        validateAppStability("after app relaunch", null);

        if (detectCurrentAppState() == AppState.LOGIN) {
            log("INFO", RECOVERY, "re-login", null, "App restarted on login screen. Logging in again.");
            loginFlow.loginFromCurrentScreen(username, password);
        }

        markLoginCompleted(true);
        ensureHomeState();
        log("INFO", RECOVERY, "crash-recovery-complete", null, "Crash recovery completed successfully");
    }

    public NavigationResult attemptSoftRecovery(String context) {
        log("WARN", RECOVERY, "soft-recovery", null, "Attempting soft recovery. context=" + context);
        NavigationResult result = NavigationUtils.safeNavigateToHistory(new PageObject.P09_ReviewOrderPage(), context, true);
        if (result.succeeded()) {
            validateAppStability("after soft recovery", null);
            return result;
        }
        failFast(CrashSignal.NONE, "Soft recovery failed. " + result.details(), context, null, captureSnapshot());
        return result;
    }

    public void log(String level, String channel, String stepName, String orderId, String message) {
        ActivityUtils.ActivitySnapshot activity = ActivityUtils.captureSnapshot();
        String prefix = "level=" + level
                + ", step=" + stepName
                + ", orderId=" + safe(orderId)
                + ", appState=" + detectCurrentAppState()
                + ", activity=" + safe(activity.currentActivity())
                + ", package=" + safe(activity.currentPackage());
        FlowLogger.step(channel, prefix + ", " + message);
    }

    public HealthSnapshot captureSnapshot() {
        ActivityUtils.ActivitySnapshot activity = ActivityUtils.captureSnapshot();
        CrashUtils.AppStatus status = CrashUtils.inspectAppStatus(DriverManager.getDriver());
        AppState appState = detectCurrentAppState();
        String pageSource = readPageSourceSilently();

        CrashSignal signal = CrashSignal.NONE;
        String reason = "App is healthy";
        if (status.sessionLost()) {
            signal = CrashSignal.SESSION_LOST;
            reason = status.describe();
        } else if (status.killed()) {
            signal = CrashSignal.APP_KILLED;
            reason = status.describe();
        } else if (status.backgroundedOrOutOfApp()) {
            signal = CrashSignal.OUT_OF_APP;
            reason = status.describe();
        } else if (status.activityMissing()) {
            signal = CrashSignal.ACTIVITY_MISSING;
            reason = status.describe();
        } else if (status.pageSourceUnavailable()) {
            signal = CrashSignal.PAGE_SOURCE_EMPTY;
            reason = status.describe();
        } else if (containsCrashPopup(pageSource)) {
            signal = CrashSignal.CRASH_POPUP;
            reason = "Crash popup or ANR dialog detected";
        } else if (isBlankScreen(pageSource, appState)) {
            signal = CrashSignal.BLANK_SCREEN;
            reason = "Blank screen detected";
        }

        boolean alive = signal == CrashSignal.NONE;
        String details = "signal=" + signal
                + ", appState=" + appState
                + ", currentActivity=" + safe(activity.currentActivity())
                + ", currentPackage=" + safe(activity.currentPackage())
                + ", reason=" + reason;
        return new HealthSnapshot(alive, signal, appState, activity, status, details);
    }

    private void failFast(CrashSignal signal,
                          String reason,
                          String context,
                          String orderId,
                          HealthSnapshot snapshot) {
        Path screenshot = captureFailureScreenshot(context, orderId);
        if (screenshot != null) {
            AllureUtils.attachFile("Failure Screenshot - " + testName, screenshot, "image/png");
        }

        String message = "reasonCode=" + signal
                + ", context=" + context
                + ", orderId=" + safe(orderId)
                + ", details=" + snapshot.details()
                + ", failure=" + reason;
        log("ERROR", CRASH, "fail-fast", orderId, message);
        Assert.fail(message);
    }

    private Path captureFailureScreenshot(String context, String orderId) {
        try {
            String fileName = testName + "_" + sanitize(context) + "_" + sanitize(orderId) + "_" + TS.format(LocalDateTime.now());
            return ScreenshotUtils.capture(fileName);
        } catch (Exception exception) {
            log("WARN", CRASH, "screenshot-failure", orderId, "Could not capture failure screenshot: " + summarizeException(exception));
            return null;
        }
    }

    private String readPageSourceSilently() {
        try {
            String pageSource = DriverManager.getDriver().getPageSource();
            return pageSource == null ? "" : pageSource;
        } catch (Exception exception) {
            return "";
        }
    }

    private boolean containsCrashPopup(String pageSource) {
        String normalized = pageSource.toLowerCase(Locale.ROOT);
        return normalized.contains("keeps stopping")
                || normalized.contains("isn't responding")
                || normalized.contains("app has stopped")
                || normalized.contains("wait or close")
                || normalized.contains("wait or ok");
    }

    private boolean isBlankScreen(String pageSource, AppState appState) {
        return pageSource.isBlank() || (pageSource.length() < 120 && appState == AppState.UNKNOWN);
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "na";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "<empty>" : value;
    }

    private String summarizeException(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("\\s+", " ").trim();
    }

    public record HealthSnapshot(boolean alive,
                                 CrashSignal signal,
                                 AppState appState,
                                 ActivityUtils.ActivitySnapshot activitySnapshot,
                                 CrashUtils.AppStatus appStatus,
                                 String details) {
    }
}
