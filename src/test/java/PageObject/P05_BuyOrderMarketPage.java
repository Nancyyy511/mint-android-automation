package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

public class P05_BuyOrderMarketPage extends BasePage {

    // ===== Locators =====
    private final By marketPriceTab =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Market Price\")");

    private final By quantityField =
            AppiumBy.xpath("//android.widget.TextView[@text=\"Quantity\"]/following::android.widget.EditText[1]");
    private final By reviewOrderBtn =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Review Order\")");

    // ===== Actions =====
    public void chooseMarketPrice() {
        customWait().until(ExpectedConditions.elementToBeClickable(marketPriceTab)).click();
    }

    public void enterQuantity(String quantity) {

        WebElement qty =
                customWait().until(ExpectedConditions.elementToBeClickable(quantityField));

        qty.click();
        qty.clear();
        qty.sendKeys(quantity);

        try {
            driver().hideKeyboard();
        } catch (Exception ignored) {
        }
    }



    public void reviewOrder() {
        customWait().until(ExpectedConditions.elementToBeClickable(reviewOrderBtn)).click();
    }
    //-----------------Review Order--------------------------

    // ===== Locators =====
    private final By orderType =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Buy\")");

    private final By priceValue =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"EGP\")");

    private final By submitBtn =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Submit\")");

    // ===== Assertions =====
    public void assertBuyLimitOrderDetails(String ticker) {
        By reviewTicker = AppiumBy.androidUIAutomator("new UiSelector().text(\"" + ticker + "\")");
        Assert.assertTrue(isElementDisplayed(reviewTicker), "Ticker not visible");
        Assert.assertTrue(isElementDisplayed(orderType), "Order type not Buy");
        Assert.assertTrue(isElementDisplayed(priceValue), "Price not visible");
        Assert.assertTrue(isElementDisplayed(submitBtn), "Submit not visible");
    }

    // ===== Actions =====
    public void submitOrder() {
        customWait().until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }
    //---------- Go to home screen
    private final By goToHomeBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Home\")");

    public void goToHome() {
        WebElement btn = customWait().until(
                ExpectedConditions.visibilityOfElementLocated(goToHomeBtn)
        );
        btn.click();
    }


}
