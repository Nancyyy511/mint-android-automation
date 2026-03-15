package TestObject;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.touch.TapOptions;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.net.URL;
import java.time.Duration;
import java.util.Collections;

public class Core {

    protected static AndroidDriver driver;
    protected static WebDriverWait wait;

    @BeforeSuite


    public void openApp() throws Exception {

        if (driver == null) {

            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setCapability("udid", "ZLIBRWOB6DL7Q8OB");
            options.setCapability("deviceName", "Redmi 13C"); //

            options.setAutomationName("UiAutomator2");
            options.setAppPackage("com.cf_holding.mint.app");
            options.setAppActivity("com.cf_holding.mint.app.ui.main.MainActivity");
            options.setApp("C:\\Users\\nawny\\Downloads\\Mint_production_2.1.3.apk");
            options.autoGrantPermissions();
            options.setCapability("unicodeKeyboard", false);
            options.setCapability("resetKeyboard", true);
            options.setCapability("newCommandTimeout", 300);
            options.setCapability("uiautomator2ServerInstallTimeout", 60000);
            options.setCapability("uiautomator2ServerLaunchTimeout", 60000);
            options.setCapability("adbExecTimeout", 60000);
            options.setCapability("autoAcceptAlerts", true);
            options.setCapability("disableWindowAnimation", true);
            options.setCapability("ignoreHiddenApiPolicyError", true);


            driver = new AndroidDriver(
                    new URL("http://127.0.0.1:4723/wd/hub"),
                    options
            );

            wait = new WebDriverWait(driver, Duration.ofSeconds(6));
            System.out.println("App launched successfully");
        }
    }

    // ========= Helper Methods =========

    protected static WebElement waitForElement(By locator) {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator))
                .sendKeys(text);
    }

    protected void fastClick(By locator) {
        try {
            driver.findElement(locator).click();
        } catch (Exception e) {
            tap(locator);
        }
    }



    protected void tap(By locator) {

        WebElement element = wait.until(
                ExpectedConditions.elementToBeClickable(locator)
        );

        int x = element.getRect().getX() + element.getRect().getWidth() / 2;
        int y = element.getRect().getY() + element.getRect().getHeight() / 2;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(
                Duration.ZERO,
                PointerInput.Origin.viewport(),
                x, y
        ));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(tap));
    }

    public void tapOutside() {

        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int x = width / 2;
        int y = (int) (height * 0.15);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(tap));
    }

    protected void safeSwipeLeft() {

        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int startX = (int) (width * 0.85);
        int endX   = (int) (width * 0.15);
        int y      = height / 2;

        new TouchAction<>(driver)
                .press(PointOption.point(startX, y))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(100)))
                .moveTo(PointOption.point(endX, y))
                .release()
                .perform();
    }


    protected boolean isElementDisplayed(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    protected boolean isVisibleQuick(By locator, int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    protected void swipeUpSmall() {
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int x = width / 2;
        int startY = (int) (height * 0.75);
        int endY   = (int) (height * 0.4);

        new TouchAction<>(driver)
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
        return Double.parseDouble(
                text.replaceAll("[^0-9.]", "")
        );
    }
    protected void scrollDown() {

        int width  = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int startX = width / 2;
        int startY = (int) (height * 0.75);
        int endY   = (int) (height * 0.25);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500),
                PointerInput.Origin.viewport(), startX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }



    // ========= Close App =========

    @AfterSuite
    public void closeApp() throws InterruptedException {
        if (driver != null) {
           //Thread.sleep(1000);
           // driver.quit();
            //driver = null;
            System.out.println("App closed");
        }
    }
}
