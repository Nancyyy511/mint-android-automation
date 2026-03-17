package TestObject;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;

@Deprecated
public class Core {

    @Deprecated
    protected static void setDriver(AndroidDriver androidDriver, WebDriverWait webDriverWait) {
        DriverManager.setDriver(androidDriver);
        DriverManager.setWait(webDriverWait);
    }

    protected static AndroidDriver getDriver() {
        AndroidDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException("Android driver is not initialized for the current thread.");
        }
        return driver;
    }

    protected static WebDriverWait getWait() {
        WebDriverWait wait = DriverManager.getWait();
        if (wait == null) {
            throw new IllegalStateException("WebDriverWait is not initialized for the current thread.");
        }
        return wait;
    }

    protected AndroidDriver driver() {
        return getDriver();
    }

    protected WebDriverWait wait() {
        return getWait();
    }

    protected static WebElement waitForElement(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        getWait().until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        getWait().until(ExpectedConditions.visibilityOfElementLocated(locator)).sendKeys(text);
    }

    protected void fastClick(By locator) {
        try {
            getDriver().findElement(locator).click();
        } catch (Exception exception) {
            tap(locator);
        }
    }

    protected void tap(By locator) {
        WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(locator));

        int x = element.getRect().getX() + element.getRect().getWidth() / 2;
        int y = element.getRect().getY() + element.getRect().getHeight() / 2;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        getDriver().perform(Collections.singletonList(tap));
    }

    public void tapOutside() {
        int width = getDriver().manage().window().getSize().width;
        int height = getDriver().manage().window().getSize().height;

        int x = width / 2;
        int y = (int) (height * 0.15);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        getDriver().perform(Collections.singletonList(tap));
    }

    protected void safeSwipeLeft() {
        int width = getDriver().manage().window().getSize().width;
        int height = getDriver().manage().window().getSize().height;

        int startX = (int) (width * 0.85);
        int endX = (int) (width * 0.15);
        int y = height / 2;

        new TouchAction<>(getDriver())
                .press(PointOption.point(startX, y))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(100)))
                .moveTo(PointOption.point(endX, y))
                .release()
                .perform();
    }

    protected boolean isElementDisplayed(By locator) {
        return !getDriver().findElements(locator).isEmpty();
    }

    protected boolean isVisibleQuick(By locator, int seconds) {
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    protected void swipeUpSmall() {
        int width = getDriver().manage().window().getSize().width;
        int height = getDriver().manage().window().getSize().height;

        int x = width / 2;
        int startY = (int) (height * 0.75);
        int endY = (int) (height * 0.4);

        new TouchAction<>(getDriver())
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
        WebElement el = waitForElement(locator);
        el.clear();
        el.sendKeys(value);
    }

    protected double parseNumber(String text) {
        return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
    }

    protected void scrollDown() {
        int width = getDriver().manage().window().getSize().width;
        int height = getDriver().manage().window().getSize().height;

        int startX = width / 2;
        int startY = (int) (height * 0.75);
        int endY = (int) (height * 0.25);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), startX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        getDriver().perform(Collections.singletonList(swipe));
    }
}
