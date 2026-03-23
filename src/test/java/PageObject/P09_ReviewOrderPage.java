package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class P09_ReviewOrderPage extends BasePage {

    private final By historyActionButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Go To Orders History\")");

    private final By historyNavButton =
            AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"History\")");

    private final By historyTitle =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Order History\")");

    private final By editButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Update Order\")");

    private final By repeatButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Repeat order\")");

    private final By submitButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Submit\")");

    private final By reviewOrderButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Review Order\")");

    private final By homeButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Back to Home\")");

    private final By homeNavButton =
            AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Home\")");

    private final By orderStatusPending =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Pending\")");

    private By tickerText(String ticker) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"" + ticker + "\")"
        );
    }

    private By orderRowText(String ticker, String side) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"" + ticker + " \u2014 " + side + "\")"
        );
    }

    public void openHistoryFromCurrentScreen() {
        if (isVisibleQuick(historyTitle, 3)) {
            return;
        }

        if (!clickFirstVisible(5, historyActionButton, historyNavButton)) {
            throw new RuntimeException("Could not navigate to Order History from the current screen.");
        }

        customWait().until(ExpectedConditions.visibilityOfElementLocated(historyTitle));
    }

    public void openLatestOrderForTicker(String ticker) {
        openLatestOrderForTicker(ticker, null);
    }

    public void openLatestOrderForTicker(String ticker, String side) {
        customWait().until(ExpectedConditions.visibilityOfElementLocated(historyTitle));

        By rowLocator = side == null ? tickerText(ticker) : orderRowText(ticker, side);
        List<WebElement> matchingOrders = driver().findElements(rowLocator).stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed();
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .toList();

        if (matchingOrders.isEmpty()) {
            throw new RuntimeException(
                    "No visible order found in history for ticker: " + ticker + (side == null ? "" : " and side: " + side)
            );
        }

        clickElementReliably(matchingOrders.get(0), "History order " + ticker + (side == null ? "" : " " + side));
        waitForOrderDetails(ticker);
    }

    public void waitForOrderDetails(String ticker) {
        By detailTicker = tickerText(ticker);
        customWait().until(driver ->
                isElementDisplayed(detailTicker)
                        || isElementDisplayed(editButton)
                        || isElementDisplayed(repeatButton)
                        || isElementDisplayed(orderStatusPending)
        );
    }

    public void assertTickerDisplayed(String ticker) {
        if (!isElementDisplayed(tickerText(ticker))) {
            throw new RuntimeException("Expected ticker not displayed on order details screen: " + ticker);
        }
    }

    public void clickEdit() {
        WebElement button = customWait().until(ExpectedConditions.elementToBeClickable(editButton));
        clickElementReliably(button, "Update Order button");
    }

    public void clickRepeat() {
        WebElement button = customWait().until(ExpectedConditions.elementToBeClickable(repeatButton));
        clickElementReliably(button, "Repeat button");
    }

    public void completeRepeatOrderSubmission() {
        System.out.println("[ORDER_HISTORY] Resolving repeat-order destination screen");

        boolean landedOnRepeatTarget = waitForRepeatActionSurface();
        if (!landedOnRepeatTarget) {
            throw new RuntimeException("Repeat order did not open a screen with Review Order or Submit.");
        }

        if (isElementDisplayed(reviewOrderButton)) {
            System.out.println("[ORDER_HISTORY] Repeat flow opened order form. Clicking Review Order first");
            WebElement reviewButton = customWait().until(ExpectedConditions.elementToBeClickable(reviewOrderButton));
            clickElementReliably(reviewButton, "Review Order button");
        } else {
            System.out.println("[ORDER_HISTORY] Repeat flow opened review screen directly");
        }

        WebElement button = customWait().until(ExpectedConditions.elementToBeClickable(submitButton));
        clickElementReliably(button, "Submit button");
    }

    private boolean waitForRepeatActionSurface() {
        for (int attempt = 1; attempt <= 4; attempt++) {
            if (isElementDisplayed(submitButton) || isElementDisplayed(reviewOrderButton)) {
                System.out.println("[ORDER_HISTORY] Repeat target screen detected on attempt " + attempt);
                return true;
            }

            System.out.println("[ORDER_HISTORY] Repeat target screen not ready. Scrolling attempt " + attempt);
            try {
                scrollDown();
            } catch (Exception ignored) {
            }
            pause(700);
        }

        return isElementDisplayed(submitButton) || isElementDisplayed(reviewOrderButton);
    }

    public void submitOrder() {
        WebElement button = customWait().until(ExpectedConditions.elementToBeClickable(submitButton));
        clickElementReliably(button, "Submit button");
    }

    public void goHome() {
        if (clickFirstVisible(5, homeButton, homeNavButton)) {
            return;
        }
        throw new RuntimeException("Could not navigate back to Home from the current screen.");
    }
}
