package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class P03_PreConditionBuyPage extends BasePage {
    private static final double BUY_SELL_ICON_X_PERCENT = 0.503;
    private static final double BUY_SELL_ICON_Y_PERCENT = 0.911;

    // ===== Locators =====
    private final By buySellIcon =
            AppiumBy.accessibilityId("Buy/Sell");

    private final By buySellIconFrame =
            AppiumBy.xpath("//android.widget.FrameLayout[@content-desc=\"Buy/Sell\"]");

    private final By buyOption =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Buy\")"
            );

    private final By accountOption =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"8221001\")"
            );

    private final By searchField =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").focusable(true)"
            );

    private By tickerText(String ticker) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector()"
                        + ".className(\"android.widget.TextView\")"
                        + ".text(\"" + ticker + "\")"
        );
    }

    private final By buyOrderHeader =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Buy\")"
            );

    private final By quantitySection =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Quantity\")"
            );

    private final By custodianDropdownText =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Custodian\")"
            );

    private final By custodianDropdownSpinner =
            AppiumBy.className("android.widget.Spinner");



    /*public void openBuySellBottomSheet() {
        wait.until(ExpectedConditions.elementToBeClickable(openBuySellBottomSheet)).click();
    }*/
    // ===== Actions =====

    public void openBuySellBottomSheet() {
        hideKeyboardIfVisible();

        try {
            WebElement icon = waitForBuySellIcon();
            clickElementReliably(icon, "Buy/Sell icon");
        } catch (Exception e) {
            // Fallback to image-derived bottom-center Buy/Sell coordinates.
            tapByScreenPercentage(BUY_SELL_ICON_X_PERCENT, BUY_SELL_ICON_Y_PERCENT);
            pause(800);
            return;
        }
    }

    private WebElement waitForBuySellIcon() {
        try {
            return customWait().until(ExpectedConditions.visibilityOfElementLocated(buySellIcon));
        } catch (Exception ignored) {
            return customWait().until(ExpectedConditions.visibilityOfElementLocated(buySellIconFrame));
        }
    }

    public void chooseBuy() {
        customWait().until(ExpectedConditions.elementToBeClickable(buyOption)).click();
    }

    public void chooseAccount() {
        customWait().until(ExpectedConditions.elementToBeClickable(accountOption)).click();
    }

    public void searchForTicker(String ticker) {

        WebElement search = customWait().until(
                ExpectedConditions.elementToBeClickable(searchField)
        );

        search.click();
        search.clear();
        search.sendKeys(ticker);


        driver().hideKeyboard();

        System.out.println("Ticker Typed: " + ticker);
    }

    public void selectFirstTickerByName(String ticker) {

        WebElement tickerE1 = customWait().until(
                ExpectedConditions.elementToBeClickable(tickerText(ticker))

        );

        tickerE1.click();


        System.out.println("Ticker selected: "+ ticker);
    }

    public void assertBuyPageOpened() {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(buyOrderHeader));
        customWait().until(ExpectedConditions.visibilityOfElementLocated(quantitySection));
    }

    public void selectCustodian(String custodianName) {
        System.out.println("Selecting custodian: " + custodianName);

        By custodianOption = AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"" + custodianName + "\")"
        );

        try {
            if (!clickFirstVisible(5, custodianDropdownText, custodianDropdownSpinner)) {
                tapByScreenPercentage(0.50, 0.42);
            }

            waitForSeconds(10).until(driver ->
                    !driver.findElements(custodianOption).isEmpty()
            );

            if (!clickIfVisible(custodianOption, 5)) {
                waitForSeconds(5)
                        .until(ExpectedConditions.elementToBeClickable(custodianOption))
                        .click();
            }
        } catch (Exception exception) {
            // Fallback: open dropdown and choose a center-list item by coordinates.
            tapByScreenPercentage(0.50, 0.42);
            waitForSeconds(4).until(driver ->
                    !driver.findElements(custodianOption).isEmpty()
                            || !driver.findElements(AppiumBy.className("android.widget.ListView")).isEmpty()
            );
            if (!clickIfVisible(custodianOption, 3)) {
                tapByScreenPercentage(0.50, 0.58);
            }
        }

        waitForSeconds(8).until(driver ->
                !driver.findElements(AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"" + custodianName + "\")"
                )).isEmpty()
        );
    }

}


