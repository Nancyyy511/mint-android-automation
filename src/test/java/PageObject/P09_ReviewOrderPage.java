package PageObject;

import TestObject.FlowLogger;
import api.utils.RetryUtils;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class P09_ReviewOrderPage extends BasePage {

    private final By historyActionButton =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*go to orders history.*\")");

    private final By historyNavButton =
            AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"History\")");

    private final By historyTabText =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*(order\\s*)?history.*\")");

    private final By historyTitle =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*order history.*\")");

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

    private final By orderStatusExecuted =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Executed\")");

    private final By orderStatusRejected =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Rejected\")");

    private final By orderIdLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*order\\s*id.*\")");

    private final By filterButton =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*filter.*\")");

    private final By clearFilterButton =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*(clear|reset).*\")");

    private final By orderTypeLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*(buy|sell).*\")");

    private By tickerText(String ticker) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + ticker + "\")");
    }

    private By orderRowText(String ticker, String side) {
        return AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + ticker + " - " + side + "\")");
    }

    public void openHistoryFromCurrentScreen() {
        RetryUtils.retry(() -> {
            logCurrentScreen("Before navigation to Order History");
            if (isOnHistoryScreen()) {
                FlowLogger.step("ORDER_HISTORY", "Already on Order History screen");
                return true;
            }

            FlowLogger.step("ORDER_HISTORY", "Navigation strategy A: direct history click");
            if (tryDirectHistoryNavigation()) {
                return true;
            }

            FlowLogger.step("ORDER_HISTORY", "Navigation strategy B: back then history");
            if (tryBackThenHistoryNavigation()) {
                return true;
            }

            FlowLogger.step("ORDER_HISTORY", "Navigation strategy C: back to home/history fallback");
            if (tryHomeThenHistoryNavigation()) {
                return true;
            }

            captureUiDiagnostics("order-history-navigation-failure");
            throw new RuntimeException("Could not navigate to Order History from the current screen.");
        }, 3, 1000);
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
                        || isElementDisplayed(orderStatusExecuted)
                        || isElementDisplayed(orderStatusRejected)
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

    public String openLatestVisibleOrderAndGetId() {
        openHistoryFromCurrentScreen();

        WebElement latestOrder = findLatestVisibleOrderRow();
        clickElementReliably(latestOrder, "Latest visible order");
        waitForSeconds(8).until(driver ->
                isElementDisplayed(orderStatusPending)
                        || isElementDisplayed(orderStatusExecuted)
                        || isElementDisplayed(orderStatusRejected)
                        || isElementDisplayed(editButton)
                        || isElementDisplayed(repeatButton)
        );
        return extractOrderIdFromCurrentDetails();
    }

    public String getOrderStatusFromHistory(String orderId) {
        if (isVisibleQuick(orderStatusPending, 2)) {
            return "Pending";
        }
        if (isVisibleQuick(orderStatusExecuted, 2)) {
            return "Executed";
        }
        if (isVisibleQuick(orderStatusRejected, 2)) {
            return "Rejected";
        }

        openHistoryFromCurrentScreen();

        if (orderId != null && !orderId.isBlank() && isVisibleQuick(orderIdText(orderId), 4)) {
            click(orderIdText(orderId));
            waitForOrderDetails(orderId);
        } else if (orderId == null || orderId.isBlank()) {
            openLatestVisibleOrderAndGetId();
        }

        if (isVisibleQuick(orderStatusPending, 3)) {
            return "Pending";
        }
        if (isVisibleQuick(orderStatusExecuted, 3)) {
            return "Executed";
        }
        if (isVisibleQuick(orderStatusRejected, 3)) {
            return "Rejected";
        }
        throw new RuntimeException("Could not resolve order status from order history details");
    }

    public String extractOrderIdFromCurrentDetails() {
        if (isVisibleQuick(orderIdLabel, 3)) {
            try {
                WebElement label = waitForElement(orderIdLabel, 5);
                List<WebElement> candidates = driver().findElements(AppiumBy.className("android.widget.TextView")).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(element -> {
                            try {
                                String text = element.getText();
                                return text != null && text.matches(".*\\d{4,}.*");
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                        .sorted(Comparator.comparingInt(element ->
                                Math.abs((element.getRect().getY() + element.getRect().getHeight() / 2)
                                        - (label.getRect().getY() + label.getRect().getHeight() / 2))
                        ))
                        .toList();

                if (!candidates.isEmpty()) {
                    return candidates.get(0).getText().trim();
                }
            } catch (Exception ignored) {
            }
        }

        List<WebElement> numericTexts = driver().findElements(AppiumBy.className("android.widget.TextView")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> {
                    try {
                        String text = element.getText();
                        return text != null && text.matches(".*\\d{4,}.*");
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .toList();

        if (!numericTexts.isEmpty()) {
            return numericTexts.get(0).getText().trim();
        }

        throw new RuntimeException("Order ID was not found on the order details screen");
    }

    public List<String> getVisibleOrderSummaries() {
        return getVisibleOrders().stream()
                .map(VisibleOrder::summary)
                .toList();
    }

    public List<VisibleOrder> getVisibleOrders() {
        openHistoryFromCurrentScreen();
        List<VisibleOrder> orders = new java.util.ArrayList<>();
        for (WebElement element : driver().findElements(AppiumBy.className("android.widget.TextView"))) {
            try {
                if (!element.isDisplayed()) {
                    continue;
                }
                VisibleOrder order = toVisibleOrder(element);
                if (order != null && !order.summary().isBlank() && !orders.contains(order)) {
                    orders.add(order);
                }
            } catch (Exception ignored) {
            }
        }
        return orders;
    }

    public void openVisibleOrder(VisibleOrder order) {
        openHistoryFromCurrentScreen();

        WebElement orderElement = driver().findElements(AppiumBy.className("android.widget.TextView")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> {
                    try {
                        return order.summary().equals(normalize(element.getText()));
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Visible order row not found anymore: " + order.summary()));

        clickElementReliably(orderElement, "History order " + order.summary());
        waitForSeconds(8).until(driver ->
                isElementDisplayed(editButton)
                        || isElementDisplayed(repeatButton)
                        || isElementDisplayed(orderStatusPending)
                        || isElementDisplayed(orderStatusExecuted)
                        || isElementDisplayed(orderStatusRejected)
        );
    }

    public boolean isFilterAvailable() {
        return isVisibleQuick(filterButton, 3);
    }

    public void applyFilterIfAvailable(String filterValue) {
        if (!isFilterAvailable()) {
            return;
        }
        click(filterButton);
        By filterOption = AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + filterValue + "\")");
        click(filterOption);
        pause(500);
    }

    public void clearFilterIfAvailable() {
        if (isVisibleQuick(clearFilterButton, 2)) {
            click(clearFilterButton);
        }
    }

    public void goBackToHistoryFromDetails() {
        RetryUtils.retry(() -> {
            if (isOnHistoryScreen()) {
                return true;
            }
            sendAndroidKeyEvent(4, "BACK");
            if (!isOnHistoryScreen()) {
                tryDirectHistoryNavigation();
            }
            customWait().until(driver -> isOnHistoryScreen());
            return true;
        }, 3, 750);
    }

    public void scrollOrderHistory() {
        scrollDown();
        pause(700);
    }

    public String getCurrentOrderType() {
        List<String> visibleTexts = driver().findElements(AppiumBy.className("android.widget.TextView")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .toList();

        for (String text : visibleTexts) {
            String normalized = text.toUpperCase(Locale.ROOT);
            if (normalized.contains("BUY")) {
                return "BUY";
            }
            if (normalized.contains("SELL")) {
                return "SELL";
            }
        }

        if (isVisibleQuick(orderTypeLabel, 2)) {
            String fallback = getText(orderTypeLabel).toUpperCase(Locale.ROOT);
            if (fallback.contains("BUY")) {
                return "BUY";
            }
            if (fallback.contains("SELL")) {
                return "SELL";
            }
        }

        throw new RuntimeException("Could not detect order type from order details");
    }

    public String getCurrentOrderStatusText() {
        List<String> visibleTexts = driver().findElements(AppiumBy.className("android.widget.TextView")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .map(String::trim)
                .toList();

        for (String text : visibleTexts) {
            String normalized = text.toUpperCase(Locale.ROOT);
            if (normalized.contains("PENDING")) {
                return "Pending";
            }
            if (normalized.contains("EXPIRED")) {
                return "Expired";
            }
            if (normalized.contains("REJECTED")) {
                return "Rejected";
            }
            if (normalized.contains("EXECUTED") || normalized.contains("FULFILLED")) {
                return "Fulfilled";
            }
        }

        return "Unknown";
    }

    private WebElement findLatestVisibleOrderRow() {
        List<WebElement> rows = driver().findElements(AppiumBy.className("android.widget.TextView")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> toVisibleOrder(element) != null)
                .sorted(Comparator.comparingInt(element -> element.getRect().getY()))
                .toList();

        if (rows.isEmpty()) {
            throw new RuntimeException("No visible orders were found in Order History");
        }
        return rows.get(0);
    }

    private By orderIdText(String orderId) {
        return AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + orderId + "\")");
    }

    private VisibleOrder toVisibleOrder(WebElement element) {
        try {
            String text = normalize(element.getText());
            if (text.isBlank()) {
                return null;
            }

            String upper = text.toUpperCase(Locale.ROOT);
            if (!upper.contains("BUY") && !upper.contains("SELL")) {
                return null;
            }

            return new VisibleOrder(text, upper.contains("BUY") ? "BUY" : "SELL");
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u2014', '-').replace("â€”", "-").trim();
    }

    public record VisibleOrder(String summary, String side) {
    }

    private boolean tryDirectHistoryNavigation() {
        if (isOnHistoryScreen()) {
            return true;
        }

        if (clickFirstVisible(3, historyActionButton, historyNavButton, historyTabText)) {
            customWait().until(driver -> isOnHistoryScreen());
            return true;
        }

        scrollToText("history");
        if (clickFirstVisible(3, historyActionButton, historyNavButton, historyTabText)) {
            customWait().until(driver -> isOnHistoryScreen());
            return true;
        }

        return false;
    }

    private boolean tryBackThenHistoryNavigation() {
        logCurrentScreen("Before back navigation to Order History");
        sendAndroidKeyEvent(4, "BACK");
        pause(500);
        if (isOnHistoryScreen()) {
            return true;
        }
        return tryDirectHistoryNavigation();
    }

    private boolean tryHomeThenHistoryNavigation() {
        logCurrentScreen("Before home fallback to Order History");
        if (isElementDisplayed(homeButton) || isElementDisplayed(homeNavButton)) {
            clickFirstVisible(3, homeButton, homeNavButton);
            pause(500);
        } else {
            sendAndroidKeyEvent(4, "BACK");
            pause(500);
        }
        return tryDirectHistoryNavigation();
    }

    private boolean isOnHistoryScreen() {
        return isVisibleQuick(historyTitle, 2);
    }
}
