package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import utils.GestureUtils;

import java.time.Duration;

public class P04_BuyOrderLimitPage extends BasePage {

    // ===== Locators =====
    private final By quantityInput =
            AppiumBy.xpath("//android.widget.TextView[@text=\"Quantity\"]/following::android.widget.EditText[1]");

    private final By setPriceInput =
            AppiumBy.xpath("//android.widget.TextView[@text=\"Set price\"]/following::android.widget.EditText[1]");

    private final By valueText =
            AppiumBy.xpath("//android.widget.TextView[@text=\"Value\"]/following::android.widget.TextView[contains(@text,\"EGP\")][1]");

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


    private final By reviewOrderBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Review Order\").enabled(true)"
            );


    // ===== Actions =====

    public void enterQuantity(String qty) {
        WebElement quantity = customWait().until(
                ExpectedConditions.elementToBeClickable(quantityInput)
        );

        quantity.click();
        quantity.clear();
        quantity.sendKeys(qty);
        try {
            driver().hideKeyboard();
        } catch (Exception ignored) {}

        tapOutside();


    }

    public void tapUseMax() {
        WebElement useMax = customWait().until(
                ExpectedConditions.elementToBeClickable(useMaxButton)
        );
        clickElementReliably(useMax, "Use Max");
        customWait().until(ExpectedConditions.visibilityOfElementLocated(useMaxConfirmationTitle));
    }

    public void confirmUseMax() {
        WebElement confirm = customWait().until(
                ExpectedConditions.elementToBeClickable(useMaxConfirmationButton)
        );
        clickElementReliably(confirm, "Use Max confirmation");
    }

    public void enterSetPrice(String price) {

        WebElement setPrice = customWait().until(
                ExpectedConditions.elementToBeClickable(setPriceInput)
        );

        setPrice.click();
        setPrice.clear();
        setPrice.sendKeys(price);
        try {
            driver().hideKeyboard();
        } catch (Exception ignored) {}

        tapOutside();
    }

    public String getValue() {
        return customWait().until(
                ExpectedConditions.visibilityOfElementLocated(valueText)
        ).getText();

    }


    public void scrollToReviewOrder() {
        GestureUtils.swipeByPercentage(driver(), 0.50, 0.70, 0.50, 0.30, Duration.ofMillis(600));
    }


    public void clickReviewOrder() {
        customWait().until(
                ExpectedConditions.elementToBeClickable(reviewOrderBtn)
        ).click();
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

        private final By goToHistoryBtn =
                AppiumBy.androidUIAutomator(
                        "new UiSelector().text(\"Go To Orders History\")");

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
}




