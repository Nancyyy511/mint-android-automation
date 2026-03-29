package TestObject;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class T20_MultiOrders extends BaseTest {

    @Test
    public void multipleOrdersShouldAppearSortedAndFilterableInHistory() {
        new LoginFlow().loginToHome();

        TradeTestData firstBuy = new TradeTestData("OFH", "1", "0.590");
        TradeTestData secondBuy = new TradeTestData("EFID", "1", "32.74");
        TradeTestData sellOrder = new TradeTestData("OFH", "1", "0.590", "T+2");

        new BuyFlow().placeLimitOrder(firstBuy);
        new BuyFlow().placeLimitOrder(secondBuy);
        new SellFlow().placeLimitOrder(sellOrder);

        OrderHistoryFlow historyFlow = new OrderHistoryFlow();
        List<String> orders = historyFlow.getVisibleOrderSummaries();

        Assert.assertTrue(orders.size() >= 3, "Expected at least three visible orders in history");
        Assert.assertTrue(
                orders.get(0).contains(sellOrder.getTicker()) || orders.get(0).contains(secondBuy.getTicker()),
                "Latest orders should appear first in history"
        );

        if (historyFlow.isFilterAvailable()) {
            historyFlow.applyFilterIfAvailable("Buy");
            List<String> filteredOrders = historyFlow.getVisibleOrderSummaries();
            Assert.assertFalse(filteredOrders.isEmpty(), "Filtered order list should not be empty");
            historyFlow.clearFilterIfAvailable();
        }
    }
}
