package PageObject;

import TestObject.Core;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

public class P07_SellOrderLimitPage extends Core {

    // ===== Tabs =====
    private final By limitTab =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Limit Price\")"
            );

    // ===== Fields =====

    // Quantity input ( EditText)
    private final By quantityInput =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(0)"
            );

    // Market price
    private final By marketPrice =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textMatches(\"[0-9]+(\\\\.[0-9]+)?\")"
            );

    //  price input
    private final By PriceInput =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(1)"
            );

    // Value field (readonly)
    private final By valueField =
            AppiumBy.id("com.cf_holding.mint.app:id/Value");

    // ===== Settlement =====
    private By settlementOption(String t) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"" + t + "\")"
        );
    }

    // ===== Buttons =====
    private final By reviewOrderBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Review Order\").enabled(true)"
            );
    private final By goToHomeBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Home\")"
            );

    // ================= ACTIONS =================

    public void chooseLimit() {
        wait.until(ExpectedConditions.elementToBeClickable(limitTab)).click();
    }

    public void enterSellLimitOrder(int quantity, String settlement) {

        wait.until(ExpectedConditions.visibilityOfElementLocated(limitTab));
        // Settlement
        scrollToReviewOrder();

        selectSettlement(settlement);

        // Quantity
        WebElement qty = wait.until(ExpectedConditions.elementToBeClickable(quantityInput));
        qty.click();
        qty.clear();
        qty.sendKeys(String.valueOf(quantity));
        try { driver.hideKeyboard(); } catch (Exception ignored) {}

        // Price
        WebElement priceInput = wait.until(ExpectedConditions.elementToBeClickable(PriceInput));
        priceInput.click();
        priceInput.clear();
        priceInput.sendKeys("94.00");
        try { driver.hideKeyboard(); } catch (Exception ignored) {}


        // Scroll
        scrollToReviewOrder();
    }


    public void selectSettlement(String settlement) {
        wait.until(
                ExpectedConditions.elementToBeClickable(
                        settlementOption(settlement)
                )
        ).click();
    }
    public void scrollToReviewOrder() {

        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int startX = width / 2;
        int startY = (int) (height * 0.75);
        int endY   = (int) (height * 0.35);

        new io.appium.java_client.TouchAction<>(driver)
                .press(io.appium.java_client.touch.offset.PointOption.point(startX, startY))
                .waitAction(io.appium.java_client.touch.WaitOptions.waitOptions(
                        java.time.Duration.ofMillis(600)))
                .moveTo(io.appium.java_client.touch.offset.PointOption.point(startX, endY))
                .release()
                .perform();
    }

    public void clickReviewOrder() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(reviewOrderBtn));
        wait.until(ExpectedConditions.elementToBeClickable(reviewOrderBtn)).click();
    }




    public void goToHome() {
        WebElement btn = wait.until(
                ExpectedConditions.visibilityOfElementLocated(goToHomeBtn)
        );
        btn.click();
    }


    // ===== Locators =====
    private final By tickerName =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"OFH\")");

    private final By orderType =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Buy\")");

    private final By priceValue =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"EGP\")");

    private final By submitBtn =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Submit\")");

    // ===== Assertions =====
    public void assertSellLimitOrderDetails() {
        Assert.assertTrue(driver.findElement(tickerName).isDisplayed(), "Ticker not visible");
        Assert.assertTrue(driver.findElement(orderType).isDisplayed(), "Order type not Buy");
        Assert.assertTrue(driver.findElement(priceValue).isDisplayed(), "Price not visible");
        Assert.assertTrue(driver.findElement(submitBtn).isDisplayed(), "Submit not visible");
    }
    public void submitOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }




}
