package TestObject;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebDriverException;

public final class ActivityUtils {
    private static final String FLOW = "HEALTH_CHECK";

    private ActivityUtils() {
    }

    public static ActivitySnapshot captureSnapshot() {
        AndroidDriver driver = DriverManager.getDriver();
        String expectedPackage = ConfigReader.get("appPackage");
        if (driver == null) {
            return new ActivitySnapshot(expectedPackage, "", "", false, "driver is null");
        }

        String currentPackage = safeRead(() -> driver.getCurrentPackage(), "getCurrentPackage");
        String currentActivity = safeRead(driver::currentActivity, "currentActivity");

        boolean inExpectedPackage = !currentPackage.isBlank() && expectedPackage.equalsIgnoreCase(currentPackage);
        boolean inExpectedActivity = isExpectedActivity(currentActivity, expectedPackage);
        String summary = "package=" + blankToUnknown(currentPackage)
                + ", activity=" + blankToUnknown(currentActivity)
                + ", inExpectedPackage=" + inExpectedPackage
                + ", inExpectedActivity=" + inExpectedActivity;
        return new ActivitySnapshot(expectedPackage, currentPackage, currentActivity, inExpectedPackage && inExpectedActivity, summary);
    }

    public static boolean isExpectedActivity(String currentActivity, String expectedPackage) {
        if (currentActivity == null || currentActivity.isBlank()) {
            return false;
        }
        String normalized = currentActivity.trim();
        if (normalized.startsWith(".")) {
            return true;
        }
        if (normalized.startsWith(expectedPackage)) {
            return true;
        }
        return !normalized.contains("/") && !normalized.contains("launcher") && !normalized.contains("resolver");
    }

    public static String formatForLog(String context, String orderId) {
        ActivitySnapshot snapshot = captureSnapshot();
        String orderSuffix = orderId == null || orderId.isBlank() ? "" : ", orderId=" + orderId;
        return "context=" + context + orderSuffix + ", " + snapshot.summary();
    }

    private static String safeRead(ReadOperation operation, String label) {
        try {
            String value = operation.read();
            return value == null ? "" : value.trim();
        } catch (WebDriverException exception) {
            FlowLogger.step(FLOW, "Could not read " + label + ": " + summarizeException(exception));
            return "";
        }
    }

    private static String blankToUnknown(String value) {
        return value == null || value.isBlank() ? "<unknown>" : value;
    }

    private static String summarizeException(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("\\s+", " ").trim();
    }

    @FunctionalInterface
    private interface ReadOperation {
        String read();
    }

    public record ActivitySnapshot(String expectedPackage,
                                   String currentPackage,
                                   String currentActivity,
                                   boolean inApp,
                                   String summary) {
    }
}
