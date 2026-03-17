package PageObject;

import TestObject.Core;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

import java.util.Collections;

public class P05_BuyOrderMarketPage extends Core {

    // ===== Locators =====
    private final By marketPriceTab =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Market Price\")");

    private final By quantityField =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(0)"
            );
    private final By reviewOrderBtn =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Review Order\")");

    // ===== Actions =====
    public void chooseMarketPrice() {
        wait().until(ExpectedConditions.elementToBeClickable(marketPriceTab)).click();
    }

    public void enterQuantity(String quantity) {

        WebElement qty =
                wait().until(ExpectedConditions.elementToBeClickable(quantityField));

        qty.click();
        qty.clear();
        qty.sendKeys(quantity);

        try {
            driver().hideKeyboard();
        } catch (Exception ignored) {
        }
    }



    public void reviewOrder() {
        wait().until(ExpectedConditions.elementToBeClickable(reviewOrderBtn)).click();
    }
    //-----------------Review Order--------------------------

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
    public void assertBuyLimitOrderDetails() {
        Assert.assertTrue(driver().findElement(tickerName).isDisplayed(), "Ticker not visible");
        Assert.assertTrue(driver().findElement(orderType).isDisplayed(), "Order type not Buy");
        Assert.assertTrue(driver().findElement(priceValue).isDisplayed(), "Price not visible");
        Assert.assertTrue(driver().findElement(submitBtn).isDisplayed(), "Submit not visible");
    }

    // ===== Actions =====
    public void submitOrder() {
        wait().until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }
    //---------- Go to home screen
    private final By goToHomeBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Home\")");

    public void goToHome() {
        WebElement btn = wait().until(
                ExpectedConditions.visibilityOfElementLocated(goToHomeBtn)
        );
        btn.click();
    }


}
