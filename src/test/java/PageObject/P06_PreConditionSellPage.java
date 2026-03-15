package PageObject;

import TestObject.Core;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.Collections;

public class P06_PreConditionSellPage extends Core {

    // ===== Bottom Sheet =====
    public void openBuySellBottomSheet() {
        try { driver.hideKeyboard(); } catch (Exception ignored) {}

        try {
            // Try dynamic element tap first
            WebElement icon = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    AppiumBy.xpath("//android.widget.FrameLayout[@content-desc=\"Buy/Sell\"]")
            ));

            int centerX = icon.getLocation().getX() + icon.getSize().getWidth() / 2;
            int centerY = icon.getLocation().getY() + icon.getSize().getHeight() / 2;

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence tap = new Sequence(finger, 1);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY));
            tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(new org.openqa.selenium.interactions.Pause(finger, Duration.ofMillis(150)));
            tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Collections.singletonList(tap));

            System.out.println("Buy/Sell icon tapped dynamically by center");

        } catch (Exception e) {
            // Fallback: tap by screen percentage
            tapByScreenPercentage(0.5, 0.95);
            System.out.println("Buy/Sell icon tapped by screen percentage fallback");
        }
    }
    public void tapByScreenPercentage(double percentX, double percentY) {
        Dimension size = driver.manage().window().getSize();
        int x = (int) (size.width * percentX);
        int y = (int) (size.height * percentY);

        new TouchAction<>(driver)
                .tap(PointOption.point(x, y))
                .perform();

        System.out.println("Tapped screen at " + percentX + ", " + percentY);
    }



    // ===== Locators =====
    private final By sellOption =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Sell\")");

    private final By accountOption =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"8221001\")");

    private final By searchField =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").focusable(true)"
            );

    private By tickerText(String ticker) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.TextView\").text(\"" + ticker + "\")"
        );
    }

    // ===== Actions =====
    public void chooseSell() {
        wait.until(ExpectedConditions.elementToBeClickable(sellOption)).click();
    }

    public void chooseAccount() {
        wait.until(ExpectedConditions.elementToBeClickable(accountOption)).click();
    }

    public void searchForTicker(String ticker) {
        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(searchField));
        search.click();
        search.clear();
        search.sendKeys(ticker);
        driver.hideKeyboard();
    }

    public void selectTicker(String ticker) {
        wait.until(ExpectedConditions.elementToBeClickable(tickerText(ticker))).click();
    }

    // ===== Assertion =====
    public void assertSellPageOpened() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Sell\")")
        ));
    }

    //---------Review Order Assertions---------

    // ===== Locators =====
    private final By pageTitle =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Sell\")"
            );

    private final By tickerSymbol =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.TextView\").textContains(\"NHPS\")"
            );

    private final By orderType =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Sell\")"
            );

    private final By quantityValue =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Shares\")"
            );



    private final By priceValue =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"EGP\")"
            );

    private final By settlementValue =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"T+\")"
            );

    private final By submitBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Submit\").enabled(true)"
            );



    // ===== Assertions =====
    public void assertReviewSellPageOpened() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
    }

    public void assertTickerDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(tickerSymbol));
    }

    public void assertQuantityDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(quantityValue));
    }

    public void assertPriceDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(priceValue));
    }

    public void assertSettlementDisplayed(String settlement) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"" + settlement + "\")"
                )
        ));
    }

    public void assertSubmitEnabled() {
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
    }

    // ===== Action =====
    public void submitSellOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }
}
