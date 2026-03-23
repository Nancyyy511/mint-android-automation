package PageObject;

import TestObject.DriverManager;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

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

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver().perform(Collections.singletonList(tap));
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

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver().perform(Collections.singletonList(tap));
    }

    protected void safeSwipeLeft() {
        int width = driver().manage().window().getSize().width;
        int height = driver().manage().window().getSize().height;

        int startX = (int) (width * 0.85);
        int endX = (int) (width * 0.15);
        int y = height / 2;

        new TouchAction<>(driver())
                .press(PointOption.point(startX, y))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(100)))
                .moveTo(PointOption.point(endX, y))
                .release()
                .perform();
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
            waitForSeconds(seconds).until(ExpectedConditions.elementToBeClickable(locator)).click();
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
        int width = driver().manage().window().getSize().width;
        int height = driver().manage().window().getSize().height;

        int x = width / 2;
        int startY = (int) (height * 0.75);
        int endY = (int) (height * 0.4);

        new TouchAction<>(driver())
                .press(PointOption.point(x, startY))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(200)))
                .moveTo(PointOption.point(x, endY))
                .release()
                .perform();
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
        int width = driver().manage().window().getSize().width;
        int height = driver().manage().window().getSize().height;

        int startX = width / 2;
        int startY = (int) (height * 0.75);
        int endY = (int) (height * 0.25);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), startX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver().perform(Collections.singletonList(swipe));
    }

    protected void tapByScreenPercentage(double percentX, double percentY) {
        Dimension size = driver().manage().window().getSize();
        int x = (int) (size.width * percentX);
        int y = (int) (size.height * percentY);

        new TouchAction<>(driver())
                .tap(PointOption.point(x, y))
                .perform();
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
}
