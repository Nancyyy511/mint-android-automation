package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import utils.GestureUtils;

import java.time.Duration;

public class P07_SellOrderLimitPage extends BasePage {

    // ===== Tabs =====
    private final By limitTab =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Limit Price\")"
            );

    // ===== Fields =====

    private final By quantityInput =
            AppiumBy.xpath("//android.widget.TextView[@text=\"Quantity\"]/following::android.widget.EditText[1]");

    // Market price
    private final By marketPrice =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textMatches(\"[0-9]+(\\\\.[0-9]+)?\")"
            );

    private final By priceInput =
            AppiumBy.xpath("//android.widget.TextView[@text=\"Set price\"]/following::android.widget.EditText[1]");

    private final By useMaxButton =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textMatches(\"(?i)use max\")"
            );

    private final By useMaxConfirmationTitle =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Are You Sure\")"
            );

    private final By useMaxConfirmationButton =
            AppiumBy.xpath("//*[(@text=\"Use max\" or @text=\"Use Max\") and (@class=\"android.widget.Button\" or @class=\"android.widget.TextView\")]");

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
    private final By goToHistoryBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Go To Orders History\")"
            );

    // ================= ACTIONS =================

    public void chooseLimit() {
        customWait().until(ExpectedConditions.elementToBeClickable(limitTab)).click();
    }

    public void enterSellLimitOrder(int quantity, String price) {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(limitTab));

        WebElement qty = customWait().until(ExpectedConditions.elementToBeClickable(quantityInput));
        qty.click();
        qty.clear();
        qty.sendKeys(String.valueOf(quantity));
        try { driver().hideKeyboard(); } catch (Exception ignored) {}

        WebElement priceField = customWait().until(ExpectedConditions.elementToBeClickable(priceInput));
        priceField.click();
        priceField.clear();
        priceField.sendKeys(price);
        try { driver().hideKeyboard(); } catch (Exception ignored) {}

        scrollToReviewOrder();
    }

    public void enterSellLimitPrice(String price) {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(limitTab));

        WebElement priceField = customWait().until(ExpectedConditions.elementToBeClickable(priceInput));
        priceField.click();
        priceField.clear();
        priceField.sendKeys(price);
        try { driver().hideKeyboard(); } catch (Exception ignored) {}
        tapOutside();
    }

    public void tapUseMax() {
        WebElement useMax = customWait().until(
                ExpectedConditions.elementToBeClickable(useMaxButton)
        );
        clickElementReliably(useMax, "Sell Use Max");
        customWait().until(ExpectedConditions.visibilityOfElementLocated(useMaxConfirmationTitle));
    }

    public void confirmUseMax() {
        WebElement confirm = customWait().until(
                ExpectedConditions.elementToBeClickable(useMaxConfirmationButton)
        );
        clickElementReliably(confirm, "Sell Use Max confirmation");
    }


    public void selectSettlement(String settlement) {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(limitTab));
        scrollToReviewOrder();
        customWait().until(
                ExpectedConditions.elementToBeClickable(
                        settlementOption(settlement)
                )
        ).click();
        pause(500);
    }
    public void scrollToReviewOrder() {
        GestureUtils.swipeByPercentage(driver(), 0.50, 0.75, 0.50, 0.35, Duration.ofMillis(600));
    }

    public void clickReviewOrder() {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(reviewOrderBtn));
        customWait().until(ExpectedConditions.elementToBeClickable(reviewOrderBtn)).click();
    }




    public void goToHome() {
        WebElement btn = customWait().until(
                ExpectedConditions.visibilityOfElementLocated(goToHomeBtn)
        );
        btn.click();
    }

    public void goToHistory() {
        WebElement btn = customWait().until(
                ExpectedConditions.visibilityOfElementLocated(goToHistoryBtn)
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
        Assert.assertTrue(driver().findElement(tickerName).isDisplayed(), "Ticker not visible");
        Assert.assertTrue(driver().findElement(orderType).isDisplayed(), "Order type not Buy");
        Assert.assertTrue(driver().findElement(priceValue).isDisplayed(), "Price not visible");
        Assert.assertTrue(driver().findElement(submitBtn).isDisplayed(), "Submit not visible");
    }
    public void submitOrder() {
        customWait().until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }




}
