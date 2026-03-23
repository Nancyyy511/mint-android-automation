package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class P06_PreConditionSellPage extends BasePage {
    private static final double BUY_SELL_ICON_X_PERCENT = 0.503;
    private static final double BUY_SELL_ICON_Y_PERCENT = 0.911;

    private final By buySellIcon =
            AppiumBy.accessibilityId("Buy/Sell");

    private final By buySellIconFrame =
            AppiumBy.xpath("//android.widget.FrameLayout[@content-desc=\"Buy/Sell\"]");

    // ===== Bottom Sheet =====
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

    private final By custodianDropdownText =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Custodian\")"
            );

    private final By custodianDropdownSpinner =
            AppiumBy.className("android.widget.Spinner");

    // ===== Actions =====
    public void chooseSell() {
        customWait().until(ExpectedConditions.elementToBeClickable(sellOption)).click();
    }

    public void chooseAccount() {
        customWait().until(ExpectedConditions.elementToBeClickable(accountOption)).click();
    }

    public void searchForTicker(String ticker) {
        WebElement search = customWait().until(ExpectedConditions.elementToBeClickable(searchField));
        search.click();
        search.clear();
        search.sendKeys(ticker);
        driver().hideKeyboard();
    }

    public void selectTicker(String ticker) {
        customWait().until(ExpectedConditions.elementToBeClickable(tickerText(ticker))).click();
    }

    // ===== Assertion =====
    public void assertSellPageOpened() {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Sell\")")
        ));
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
        customWait().until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
    }

    public void assertTickerDisplayed() {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(tickerSymbol));
    }

    public void assertQuantityDisplayed() {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(quantityValue));
    }

    public void assertPriceDisplayed() {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(priceValue));
    }

    public void assertSettlementDisplayed(String settlement) {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"" + settlement + "\")"
                )
        ));
    }

    public void assertSubmitEnabled() {
        customWait().until(ExpectedConditions.elementToBeClickable(submitBtn));
    }

    // ===== Action =====
    public void submitSellOrder() {
        customWait().until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }
}
