package TestObject;

import PageObject.P02_LoginPage;
import PageObject.P06_PreConditionSellPage;
import PageObject.P07_SellOrderLimitPage;
import PageObject.P08_SellOrderMarketPage;
import org.testng.Assert;

public class SellFlow {
    private final P06_PreConditionSellPage sellPre = new P06_PreConditionSellPage();
    private final P07_SellOrderLimitPage sellLimit = new P07_SellOrderLimitPage();
    private final P08_SellOrderMarketPage sellMarket = new P08_SellOrderMarketPage();

    public void placeLimitOrder(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell limit order for " + data.getTicker());
        openSellForTicker(data.getTicker());

        sellLimit.chooseLimit();
        sellLimit.selectSettlement(data.getSettlement());
        sellLimit.enterSellLimitOrder(Integer.parseInt(data.getQuantity()), data.getPrice());
        sellLimit.clickReviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        sellLimit.goToHome();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after sell limit submit");
        FlowLogger.step("SELL_FLOW", "Sell limit order flow completed");
    }

    public void placeLimitOrderAndGoToHistory(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell limit order for " + data.getTicker() + " and opening order history");
        openSellForTicker(data.getTicker());

        sellLimit.chooseLimit();
        sellLimit.selectSettlement(data.getSettlement());
        sellLimit.enterSellLimitOrder(Integer.parseInt(data.getQuantity()), data.getPrice());
        sellLimit.clickReviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        sellLimit.goToHistory();

        FlowLogger.step("SELL_FLOW", "Sell limit order submitted and order history opened");
    }

    public void placeLimitOrderUsingMax(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell limit order with Use Max for " + data.getTicker());
        openSellForTicker(data.getTicker());

        sellLimit.chooseLimit();
        sellLimit.selectSettlement(data.getSettlement());
        sellLimit.enterSellLimitPrice(data.getPrice());
        sellLimit.tapUseMax();
        sellLimit.confirmUseMax();
        sellLimit.scrollToReviewOrder();
        sellLimit.clickReviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        sellLimit.goToHome();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after sell limit submit");
        FlowLogger.step("SELL_FLOW", "Sell limit order with Use Max completed");
    }

    public void placeMarketOrder(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell market order for " + data.getTicker());
        openSellForTicker(data.getTicker());

        sellMarket.chooseMarket();
        sellMarket.chooseSettlement(data.getSettlement());
        sellMarket.enterQuantity(data.getQuantity());
        sellMarket.reviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        sellMarket.goToHistory();

        FlowLogger.step("SELL_FLOW", "Sell market order flow completed");
    }

    private void openSellForTicker(String ticker) {
        FlowLogger.step("SELL_FLOW", "Opening Buy/Sell sheet and selecting Sell for ticker " + ticker);
        sellPre.openBuySellBottomSheet();
        sellPre.chooseSell();
        sellPre.chooseAccount();
        sellPre.searchForTicker(ticker);
        sellPre.selectTicker(ticker);
        sellPre.assertSellPageOpened();
        sellPre.selectCustodian("CFH");
    }
}
