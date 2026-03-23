package TestObject;

import PageObject.P02_LoginPage;
import PageObject.P04_BuyOrderLimitPage;
import PageObject.P09_ReviewOrderPage;
import org.testng.Assert;

public class OrderHistoryFlow {
    private final P09_ReviewOrderPage orderHistoryPage = new P09_ReviewOrderPage();
    private final P04_BuyOrderLimitPage buyLimitPage = new P04_BuyOrderLimitPage();

    public void editOrderAndUseMax(String ticker) {
        editOrderAndUseMax(ticker, "Buy");
    }

    public void editOrderAndUseMax(String ticker, String side) {
        FlowLogger.step("ORDER_HISTORY", "Editing latest order for " + ticker + " and applying Use Max");
        orderHistoryPage.openHistoryFromCurrentScreen();
        orderHistoryPage.openLatestOrderForTicker(ticker, side);
        orderHistoryPage.assertTickerDisplayed(ticker);
        orderHistoryPage.clickEdit();

        buyLimitPage.tapUseMax();
        buyLimitPage.confirmUseMax();
        buyLimitPage.scrollToReviewOrder();
        buyLimitPage.clickReviewOrder();
        buyLimitPage.assertBuyLimitOrderDetails(ticker);
        buyLimitPage.submitOrder();
        orderHistoryPage.goHome();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after editing order");
    }

    public void repeatOrderFromHistory(String ticker) {
        repeatOrderFromHistory(ticker, "Sell");
    }

    public void repeatOrderFromHistory(String ticker, String side) {
        FlowLogger.step("ORDER_HISTORY", "Repeating latest order for " + ticker);
        orderHistoryPage.openHistoryFromCurrentScreen();
        orderHistoryPage.openLatestOrderForTicker(ticker, side);
        orderHistoryPage.assertTickerDisplayed(ticker);
        orderHistoryPage.clickRepeat();
        orderHistoryPage.completeRepeatOrderSubmission();
        orderHistoryPage.goHome();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after repeating order");
    }
}
