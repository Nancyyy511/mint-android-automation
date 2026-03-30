package utils;

import TestObject.FlowLogger;
import core.config.ConfigManager;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

public final class GestureUtils {
    private GestureUtils() {
    }

    public static void tap(AndroidDriver driver, int x, int y) {
        logDebug(driver.manage().window().getSize(), "tap", x, y);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(tap));
    }

    public static void tapByPercentage(AndroidDriver driver, double xPercent, double yPercent) {
        Dimension size = driver.manage().window().getSize();
        int x = (int) Math.round(size.width * xPercent);
        int y = (int) Math.round(size.height * yPercent);
        logDebug(size, "tapByPercentage(" + xPercent + "," + yPercent + ")", x, y);
        tap(driver, x, y);
    }

    public static void swipeByPercentage(AndroidDriver driver,
                                         double startXPercent,
                                         double startYPercent,
                                         double endXPercent,
                                         double endYPercent,
                                         Duration duration) {
        Dimension size = driver.manage().window().getSize();
        int startX = (int) Math.round(size.width * startXPercent);
        int startY = (int) Math.round(size.height * startYPercent);
        int endX = (int) Math.round(size.width * endXPercent);
        int endY = (int) Math.round(size.height * endYPercent);
        if (isDebugEnabled()) {
            FlowLogger.step("GESTURE", "resolution=" + size.width + "x" + size.height
                    + ", swipe start=(" + startX + "," + startY + ")"
                    + ", end=(" + endX + "," + endY + ")");
        }

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(duration, PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipe));
    }

    private static void logDebug(Dimension size, String action, int x, int y) {
        if (!isDebugEnabled()) {
            return;
        }
        FlowLogger.step("GESTURE", "resolution=" + size.width + "x" + size.height
                + ", action=" + action + ", x=" + x + ", y=" + y);
    }

    private static boolean isDebugEnabled() {
        return ConfigManager.getBoolean("gestureDebugLogs", true);
    }
}
