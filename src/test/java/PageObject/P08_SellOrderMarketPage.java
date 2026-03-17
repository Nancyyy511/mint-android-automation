package PageObject;

import TestObject.Core;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class P08_SellOrderMarketPage extends Core {

    private final By marketTab =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Market Price\")");

    private final By quantityInput =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(0)"
            );

    private final By reviewBtn =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Review Order\")");

    private By settlementOption(String t) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + t + "\")");
    }

    public void chooseMarket() {
        wait().until(ExpectedConditions.elementToBeClickable(marketTab)).click();
    }

    public void enterQuantity(String qty) {
        wait().until(ExpectedConditions.elementToBeClickable(quantityInput)).sendKeys(qty);
    }

    public void chooseSettlement(String settlement) {
        wait().until(ExpectedConditions.elementToBeClickable(settlementOption(settlement))).click();
    }

    public void reviewOrder() {
        wait().until(ExpectedConditions.elementToBeClickable(reviewBtn)).click();
    }

    //---------- Go to home screen
    private final By goToHistoryBtn =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"History\")");

    public void goToHistory() {
        WebElement btn = wait().until(
                ExpectedConditions.visibilityOfElementLocated(goToHistoryBtn)
        );
        btn.click();
    }
}
