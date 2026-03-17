package PageObject;

import TestObject.Core;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

public class P04_BuyOrderLimitPage extends Core {

    // ===== Locators =====


    private final By quantityField =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Quantity\")"
            );

    private final By quantityInput =
            AppiumBy.className("android.widget.EditText");

    private final By setPriceInput =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(1)"
            );

    private final By valueText =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"EGP\")"
            );


    private final By reviewOrderBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Review Order\").enabled(true)"
            );


    // ===== Actions =====

    public void enterQuantity(String qty) {

        WebElement quantity = wait().until(
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

    public void enterSetPrice(String price) {

        WebElement setPrice = wait().until(
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
        return wait().until(
                ExpectedConditions.visibilityOfElementLocated(valueText)
        ).getText();

    }


    public void scrollToReviewOrder() {

        int width = driver().manage().window().getSize().width;
        int height = driver().manage().window().getSize().height;

        int startX = width / 2;
        int startY = (int) (height * 0.7);
        int endY   = (int) (height * 0.3);

        new io.appium.java_client.TouchAction<>(driver())
                .press(io.appium.java_client.touch.offset.PointOption.point(startX, startY))
                .waitAction(io.appium.java_client.touch.WaitOptions.waitOptions(
                        java.time.Duration.ofMillis(600)))
                .moveTo(io.appium.java_client.touch.offset.PointOption.point(startX, endY))
                .release()
                .perform();
    }


    public void clickReviewOrder() {
        wait().until(
                ExpectedConditions.elementToBeClickable(reviewOrderBtn)
        ).click();
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




