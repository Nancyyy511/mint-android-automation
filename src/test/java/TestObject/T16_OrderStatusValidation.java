package TestObject;

import org.testng.Assert;
import org.testng.annotations.Test;

public class T16_OrderStatusValidation extends BaseTest {

    @Test
    public void limitOrderShouldAppearAsPendingInHistory() {
        new LoginFlow().loginToHome();

        TradeTestData buyData = new TradeTestData(
                System.getProperty("t16.buy.ticker", "OFH"),
                System.getProperty("t16.buy.quantity", "1"),
                System.getProperty("t16.buy.price", "0.590")
        );
        new BuyFlow().placeLimitOrder(buyData);

        OrderHistoryFlow historyFlow = new OrderHistoryFlow();
        String orderId = historyFlow.captureLatestOrderId();
        String status = historyFlow.getOrderStatusFromHistory(orderId);
        Assert.assertEquals(status, "Pending", "Expected latest limit buy order to be Pending");
    }

    @Test
    public void marketSellOrderShouldAppearAsExecutedInHistory() {
        new LoginFlow().loginToHome();

        TradeTestData sellData = new TradeTestData(
                System.getProperty("t16.sell.ticker", "OFH"),
                System.getProperty("t16.sell.quantity", "1"),
                System.getProperty("t16.sell.price", "0.590"),
                System.getProperty("t16.sell.settlement", "T+0")
        );
        new SellFlow().placeMarketOrder(sellData);

        OrderHistoryFlow historyFlow = new OrderHistoryFlow();
        String orderId = historyFlow.captureLatestOrderId();
        String status = historyFlow.getOrderStatusFromHistory(orderId);
        Assert.assertEquals(status, "Executed", "Expected latest market sell order to be Executed");
    }

    @Test
    public void invalidPriceOrderShouldAppearAsRejectedInHistory() {
        new LoginFlow().loginToHome();

        TradeTestData rejectedData = new TradeTestData(
                System.getProperty("t16.rejected.ticker", "OFH"),
                System.getProperty("t16.rejected.quantity", "1"),
                System.getProperty("t16.rejected.price", "999999")
        );
        new BuyFlow().placeLimitOrderAndGoToHistory(rejectedData);

        OrderHistoryFlow historyFlow = new OrderHistoryFlow();
        String orderId = historyFlow.captureLatestOrderId();
        String status = historyFlow.getOrderStatusFromHistory(orderId);
        Assert.assertEquals(status, "Rejected", "Expected invalid price order to be Rejected");
    }
}
