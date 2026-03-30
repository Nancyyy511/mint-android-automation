package PageObject;

import TestObject.FlowLogger;
import api.utils.RetryUtils;
import TestObject.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.AllureUtils;
import utils.ScreenshotUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import utils.GestureUtils;

public class BasePage {

    protected AndroidDriver driver() {
        AndroidDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException("Android driver is not initialized for the current thread.");
        }
        return driver;
    }

    protected WebDriverWait customWait() {
        WebDriverWait wait = DriverManager.getWait();
        if (wait == null) {
            throw new IllegalStateException("WebDriverWait is not initialized for the current thread.");
        }
        return wait;
    }

    protected WebDriverWait waitForSeconds(long seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    protected WebElement waitForElement(By locator) {
        return customWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForElement(By locator, long seconds) {
        return waitForSeconds(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForVisible(By locator, long seconds) {
        return waitForSeconds(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator, long seconds) {
        return waitForSeconds(seconds).until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitForPresence(By locator, long seconds) {
        return waitForSeconds(seconds).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected void click(By locator) {
        customWait().until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(locator)).sendKeys(text);
    }

    protected void fastClick(By locator) {
        try {
            driver().findElement(locator).click();
        } catch (Exception exception) {
            tap(locator);
        }
    }

    protected void tap(By locator) {
        WebElement element = customWait().until(ExpectedConditions.elementToBeClickable(locator));

        int x = element.getRect().getX() + element.getRect().getWidth() / 2;
        int y = element.getRect().getY() + element.getRect().getHeight() / 2;
        GestureUtils.tap(driver(), x, y);
    }

    protected void clickElementReliably(WebElement element, String elementName) {
        try {
            element.click();
            System.out.println(elementName + " clicked using WebElement.click()");
            return;
        } catch (WebDriverException exception) {
            System.out.println(elementName + " standard click failed. Trying clickGesture.");
        }

        try {
            driver().executeScript("mobile: clickGesture", Map.of(
                    "elementId", ((RemoteWebElement) element).getId()
            ));
            System.out.println(elementName + " clicked using mobile: clickGesture");
            return;
        } catch (Exception exception) {
            System.out.println(elementName + " clickGesture failed. Falling back to tapAt.");
        }

        int centerX = element.getRect().getX() + element.getRect().getWidth() / 2;
        int centerY = element.getRect().getY() + element.getRect().getHeight() / 2;
        tapAt(centerX, centerY);
        System.out.println(elementName + " clicked using element-center tap fallback");
    }

    protected void clickElementByGesture(WebElement element, String elementName) {
        try {
            driver().executeScript("mobile: clickGesture", Map.of(
                    "elementId", ((RemoteWebElement) element).getId()
            ));
            System.out.println(elementName + " clicked using mobile: clickGesture");
            return;
        } catch (Exception exception) {
            System.out.println(elementName + " clickGesture failed. Falling back to element-center tap.");
        }

        int centerX = element.getRect().getX() + element.getRect().getWidth() / 2;
        int centerY = element.getRect().getY() + element.getRect().getHeight() / 2;
        tapAt(centerX, centerY);
        System.out.println(elementName + " clicked using element-center tap");
    }

    protected void tapOutside() {
        int width = driver().manage().window().getSize().width;
        int height = driver().manage().window().getSize().height;

        int x = width / 2;
        int y = (int) (height * 0.15);

        tapAt(x, y);
    }

    protected void tapAt(int x, int y) {
        System.out.println("Tapping coordinates x=" + x + ", y=" + y);
        GestureUtils.tap(driver(), x, y);
    }

    protected void clickGestureAt(int x, int y) {
        System.out.println("Click gesture at x=" + x + ", y=" + y);
        try {
            driver().executeScript("mobile: clickGesture", Map.of(
                    "x", x,
                    "y", y
            ));
        } catch (Exception exception) {
            System.out.println("mobile: clickGesture by coordinates failed. Falling back to tapAt.");
            tapAt(x, y);
        }
    }

    protected void safeSwipeLeft() {
        GestureUtils.swipeByPercentage(driver(), 0.85, 0.50, 0.15, 0.50, Duration.ofMillis(200));
    }

    protected boolean isElementDisplayed(By locator) {
        return !driver().findElements(locator).isEmpty();
    }

    protected boolean isVisibleQuick(By locator, int seconds) {
        try {
            waitForSeconds(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    protected boolean clickIfVisible(By locator, int seconds) {
        try {
            WebElement element = waitForClickable(locator, seconds);
            clickElementReliably(element, "Clickable element " + locator);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    protected boolean clickFirstVisible(int seconds, By... locators) {
        for (By locator : locators) {
            if (clickIfVisible(locator, seconds)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAnyVisibleQuick(int seconds, By... locators) {
        for (By locator : locators) {
            if (isVisibleQuick(locator, seconds)) {
                return true;
            }
        }
        return false;
    }

    protected void swipeUpSmall() {
        GestureUtils.swipeByPercentage(driver(), 0.50, 0.75, 0.50, 0.40, Duration.ofMillis(250));
    }

    protected String getText(By locator) {
        return waitForElement(locator).getText().trim();
    }

    protected void clearAndType(By locator, String value) {
        WebElement element = waitForElement(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected double parseNumber(String text) {
        return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
    }

    protected void scrollDown() {
        GestureUtils.swipeByPercentage(driver(), 0.50, 0.75, 0.50, 0.25, Duration.ofMillis(500));
    }

    protected boolean scrollToText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String regex = toLooseRegex(text);
        List<String> commands = List.of(
                "new UiScrollable(new UiSelector().scrollable(true)).setMaxSearchSwipes(6)"
                        + ".scrollIntoView(new UiSelector().textMatches(\"" + escapeUiAutomator(regex) + "\"))",
                "new UiScrollable(new UiSelector().scrollable(true)).setMaxSearchSwipes(6)"
                        + ".scrollIntoView(new UiSelector().textContains(\"" + escapeUiAutomator(firstToken(text)) + "\"))"
        );

        for (String command : commands) {
            try {
                driver().findElement(io.appium.java_client.AppiumBy.androidUIAutomator(command));
                FlowLogger.step("SCROLL", "Scrolled to text='" + text + "'");
                return true;
            } catch (Exception ignored) {
            }
        }

        FlowLogger.step("SCROLL", "Could not scroll to text='" + text + "'");
        return false;
    }

    protected <T> T retryAction(String description, Supplier<T> action, int maxAttempts, int delayMs) {
        return RetryUtils.retry(() -> {
            FlowLogger.step("UI_RETRY", "Running action: " + description);
            return action.get();
        }, maxAttempts, delayMs);
    }

    protected void logCurrentScreen(String context) {
        String activity;
        try {
            activity = driver().currentActivity();
        } catch (Exception exception) {
            activity = "unknown";
        }

        FlowLogger.step("UI_STATE", context + " | activity=" + activity
                + " | visibleTexts=" + visibleTextSnapshot());
    }

    protected void logAvailableElements(String context) {
        FlowLogger.step("UI_STATE", context + " | availableElements=" + visibleElementSnapshot());
    }

    protected void captureUiDiagnostics(String label) {
        logCurrentScreen(label);
        logAvailableElements(label);
        try {
            var screenshot = ScreenshotUtils.capture(label);
            AllureUtils.attachFile(label + " screenshot", screenshot, "image/png");
        } catch (Exception exception) {
            FlowLogger.step("UI_STATE", "Screenshot capture failed for " + label + ": " + exception.getMessage());
        }
    }

    protected void tapByScreenPercentage(double percentX, double percentY) {
        GestureUtils.tapByPercentage(driver(), percentX, percentY);
    }

    protected void hideKeyboardIfVisible() {
        try {
            driver().hideKeyboard();
        } catch (Exception ignored) {
        }
    }

    protected void dismissKeyboardOverlay() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (!isKeyboardVisible()) {
                return;
            }

            try {
                driver().hideKeyboard();
                System.out.println("Keyboard hidden using hideKeyboard()");
            } catch (Exception ignored) {
            }

            if (!isKeyboardVisible()) {
                return;
            }

            try {
                driver().pressKey(new KeyEvent(AndroidKey.BACK));
                System.out.println("Keyboard dismissed using BACK key");
            } catch (Exception ignored) {
            }

            if (!isKeyboardVisible()) {
                return;
            }

            tapByScreenPercentage(0.04, 0.98);
            System.out.println("Keyboard dismiss fallback tapped near bottom-left");
            pause(500);
        }
    }

    protected void dismissFloatingInputToolbar() {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                tapOutside();
                System.out.println("Tapped outside to dismiss floating input toolbar");
            } catch (Exception ignored) {
            }

            try {
                tapByScreenPercentage(0.95, 0.2);
                System.out.println("Tapped top-right area to dismiss floating input toolbar");
            } catch (Exception ignored) {
            }

            pause(250);
        }
    }

    protected boolean isKeyboardVisible() {
        try {
            return driver().isKeyboardShown();
        } catch (Exception exception) {
            return false;
        }
    }

    protected void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Pause was interrupted", exception);
        }
    }

    protected void sendAndroidKeyEvent(int keyCode, String keyName) {
        try {
            driver().executeScript("mobile: shell", Map.of(
                    "command", "input",
                    "args", java.util.List.of("keyevent", String.valueOf(keyCode))
            ));
            System.out.println("Android key event sent: " + keyName + " (" + keyCode + ")");
        } catch (Exception exception) {
            throw new RuntimeException("Could not send Android key event: " + keyName, exception);
        }
    }

    private String visibleTextSnapshot() {
        List<String> texts = driver().findElements(By.className("android.widget.TextView")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .map(String::trim)
                .limit(12)
                .toList();
        return texts.isEmpty() ? "[]" : texts.toString();
    }

    private String visibleElementSnapshot() {
        List<String> values = new ArrayList<>();
        for (WebElement element : driver().findElements(By.xpath("//*[@text or @content-desc]"))) {
            try {
                if (!element.isDisplayed()) {
                    continue;
                }
                String text = element.getText();
                String contentDesc = element.getAttribute("contentDescription");
                String value = (text == null || text.isBlank()) ? contentDesc : text;
                if (value != null && !value.isBlank()) {
                    values.add(value.trim());
                }
            } catch (Exception ignored) {
            }
            if (values.size() >= 15) {
                break;
            }
        }
        return values.isEmpty() ? "[]" : values.toString();
    }

    protected String toLooseRegex(String text) {
        String[] parts = text.trim().split("\\s+");
        StringBuilder builder = new StringBuilder("(?i).*");
        for (String part : parts) {
            builder.append(java.util.regex.Pattern.quote(part)).append(".*");
        }
        return builder.toString();
    }

    protected String escapeUiAutomator(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    protected String firstToken(String text) {
        String[] parts = text.trim().split("\\s+");
        return parts.length == 0 ? text.trim() : parts[0];
    }
}
