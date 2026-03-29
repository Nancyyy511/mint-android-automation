package TestObject;

import PageObject.P02_LoginPage;
import PageObject.P11_VerifyPinPage;
import PageObject.P04_BuyOrderLimitPage;
import PageObject.P09_ReviewOrderPage;
import org.testng.Assert;

import java.util.List;

public class OrderHistoryFlow {
    private final P09_ReviewOrderPage orderHistoryPage = new P09_ReviewOrderPage();
    private final P04_BuyOrderLimitPage buyLimitPage = new P04_BuyOrderLimitPage();
    private final P11_VerifyPinPage verifyPinPage = new P11_VerifyPinPage();

    public void editOrderAndUseMax(String ticker) {
        editOrderAndUseMax(ticker, "Buy");
    }

    public void editOrderAndUseMax(String ticker, String side) {
        FlowLogger.step("ORDER_HISTORY", "Editing latest order for " + ticker + " and applying Use Max");
        verifyPinPage.handleVerifyPinIfPresent();
        orderHistoryPage.openHistoryFromCurrentScreen();
        orderHistoryPage.openLatestOrderForTicker(ticker, side);
        orderHistoryPage.assertTickerDisplayed(ticker);
        orderHistoryPage.clickEdit();
        verifyPinPage.handleVerifyPinIfPresent();

        buyLimitPage.tapUseMax();
        buyLimitPage.confirmUseMax();
        buyLimitPage.scrollToReviewOrder();
        buyLimitPage.clickReviewOrder();
        buyLimitPage.assertBuyLimitOrderDetails(ticker);
        buyLimitPage.submitOrder();
        verifyPinPage.handleVerifyPinIfPresent();
        orderHistoryPage.goHome();
        verifyPinPage.handleVerifyPinIfPresent();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after editing order");
    }

    public void repeatOrderFromHistory(String ticker) {
        repeatOrderFromHistory(ticker, "Sell");
    }

    public void repeatOrderFromHistory(String ticker, String side) {
        FlowLogger.step("ORDER_HISTORY", "Repeating latest order for " + ticker);
        verifyPinPage.handleVerifyPinIfPresent();
        orderHistoryPage.openHistoryFromCurrentScreen();
        orderHistoryPage.openLatestOrderForTicker(ticker, side);
        orderHistoryPage.assertTickerDisplayed(ticker);
        orderHistoryPage.clickRepeat();
        orderHistoryPage.completeRepeatOrderSubmission();
        verifyPinPage.handleVerifyPinIfPresent();
        orderHistoryPage.goHome();
        verifyPinPage.handleVerifyPinIfPresent();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after repeating order");
    }

    public String captureLatestOrderId() {
        verifyPinPage.handleVerifyPinIfPresent();
        String orderId = orderHistoryPage.openLatestVisibleOrderAndGetId();
        FlowLogger.step("ORDER_HISTORY", "Latest order id captured from history: " + orderId);
        return orderId;
    }

    public String getOrderStatusFromHistory(String orderId) {
        verifyPinPage.handleVerifyPinIfPresent();
        String status = orderHistoryPage.getOrderStatusFromHistory(orderId);
        FlowLogger.step("ORDER_HISTORY", "Order status for [" + orderId + "] is " + status);
        return status;
    }

    public List<String> getVisibleOrderSummaries() {
        verifyPinPage.handleVerifyPinIfPresent();
        return orderHistoryPage.getVisibleOrderSummaries();
    }

    public boolean isFilterAvailable() {
        return orderHistoryPage.isFilterAvailable();
    }

    public void applyFilterIfAvailable(String filterValue) {
        orderHistoryPage.applyFilterIfAvailable(filterValue);
    }

    public void clearFilterIfAvailable() {
        orderHistoryPage.clearFilterIfAvailable();
    }

    public void goHomeFromHistory() {
        orderHistoryPage.goHome();
        verifyPinPage.handleVerifyPinIfPresent();
    }
}
