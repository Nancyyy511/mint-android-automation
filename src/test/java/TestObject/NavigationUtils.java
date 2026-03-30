package TestObject;

import PageObject.P09_ReviewOrderPage;
import api.utils.RetryUtils;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

public final class NavigationUtils {
    private static final String FLOW = "NAVIGATION";

    private NavigationUtils() {
    }

    public static NavigationResult safeNavigateToHistory(P09_ReviewOrderPage orderHistoryPage,
                                                         String context,
                                                         boolean enableRecovery) {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                FlowLogger.step(FLOW, "Navigate to Order History attempt=" + attempt + ", "
                        + ActivityUtils.formatForLog(context, null));
                RetryUtils.retry(() -> {
                    orderHistoryPage.openHistoryFromCurrentScreen();
                    return true;
                }, 2, 500);
                FlowLogger.step(FLOW, "Order History reached successfully on attempt=" + attempt + ", "
                        + ActivityUtils.formatForLog(context, null));
                NavigationResult.Status status = attempt == 1
                        ? NavigationResult.Status.SUCCESS
                        : NavigationResult.Status.RECOVERED;
                return new NavigationResult(status, strategyName(attempt),
                        "context=" + context + ", " + ActivityUtils.formatForLog(context, null));
            } catch (RuntimeException exception) {
                lastFailure = exception;
                FlowLogger.step(FLOW, "Order History navigation failed on attempt=" + attempt
                        + ": " + summarizeException(exception) + ", " + ActivityUtils.formatForLog(context, null));
                applyFallback(attempt, orderHistoryPage, enableRecovery);
            }
        }

        return new NavigationResult(NavigationResult.Status.FAILED, "FAILED",
                "Could not navigate to Order History. context=" + context + ", "
                        + ActivityUtils.formatForLog(context, null)
                        + ", reason=" + summarizeException(lastFailure == null
                        ? new RuntimeException("unknown navigation failure")
                        : lastFailure));
    }

    private static void applyFallback(int attempt, P09_ReviewOrderPage orderHistoryPage, boolean enableRecovery) {
        if (attempt == 1) {
            pressBack();
            return;
        }

        if (attempt == 2) {
            try {
                orderHistoryPage.goHome();
                FlowLogger.step(FLOW, "Fallback navigation to Home succeeded");
            } catch (RuntimeException homeFailure) {
                FlowLogger.step(FLOW, "Fallback goHome failed: " + summarizeException(homeFailure));
                pressBack();
            }
            return;
        }

        if (enableRecovery) {
            try {
                DriverManager.relaunchApp();
                FlowLogger.step(FLOW, "Last-resort app relaunch completed");
            } catch (RuntimeException recoveryFailure) {
                FlowLogger.step(FLOW, "Last-resort app relaunch failed: " + summarizeException(recoveryFailure));
            }
        }
    }

    private static String strategyName(int attempt) {
        return switch (attempt) {
            case 1 -> "NORMAL";
            case 2 -> "BACK";
            case 3 -> "HOME";
            case 4 -> "RELAUNCH";
            default -> "UNKNOWN";
        };
    }

    private static void pressBack() {
        try {
            DriverManager.getDriver().pressKey(new KeyEvent(AndroidKey.BACK));
            FlowLogger.step(FLOW, "Pressed Android BACK as navigation fallback");
        } catch (Exception exception) {
            FlowLogger.step(FLOW, "BACK press fallback failed: " + summarizeException(exception));
        }
    }

    private static String summarizeException(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("\\s+", " ").trim();
    }
}
