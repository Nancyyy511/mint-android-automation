package TestObject;

import PageObject.P03_PreConditionBuyPage;
import PageObject.P04_BuyOrderLimitPage;
import PageObject.P05_BuyOrderMarketPage;
import org.testng.Assert;

public class BuyFlow {
    private final P03_PreConditionBuyPage buyPre = new P03_PreConditionBuyPage();
    private final P04_BuyOrderLimitPage buyLimit = new P04_BuyOrderLimitPage();
    private final P05_BuyOrderMarketPage buyMarket = new P05_BuyOrderMarketPage();

    public void placeLimitOrder(TradeTestData data) {
        FlowLogger.step("BUY_FLOW", "Placing buy limit order for " + data.getTicker());
        openBuyForTicker(data.getTicker());

        buyLimit.enterQuantity(data.getQuantity());
        buyLimit.enterSetPrice(data.getPrice());
        buyLimit.getValue();
        buyLimit.scrollToReviewOrder();
        buyLimit.clickReviewOrder();

        buyLimit.assertBuyLimitOrderDetails(data.getTicker());
        buyLimit.submitOrder();
        buyLimit.goToHome();
        Assert.assertTrue(new PageObject.P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after buy limit submit");
        FlowLogger.step("BUY_FLOW", "Buy limit order flow completed");
    }

    public void placeLimitOrderAndGoToHistory(TradeTestData data) {
        FlowLogger.step("BUY_FLOW", "Placing buy limit order for " + data.getTicker() + " and opening order history");
        openBuyForTicker(data.getTicker());

        buyLimit.enterQuantity(data.getQuantity());
        buyLimit.enterSetPrice(data.getPrice());
        buyLimit.getValue();
        buyLimit.scrollToReviewOrder();
        buyLimit.clickReviewOrder();

        buyLimit.assertBuyLimitOrderDetails(data.getTicker());
        buyLimit.submitOrder();
        buyLimit.goToHistory();
        FlowLogger.step("BUY_FLOW", "Buy limit order submitted and order history opened");
    }

    public void placeLimitOrderUsingMax(TradeTestData data) {
        FlowLogger.step("BUY_FLOW", "Placing buy limit order with Use Max for " + data.getTicker());
        openBuyForTicker(data.getTicker());

        buyLimit.enterSetPrice(data.getPrice());
        buyLimit.tapUseMax();
        buyLimit.confirmUseMax();
        buyLimit.getValue();
        buyLimit.scrollToReviewOrder();
        buyLimit.clickReviewOrder();

        buyLimit.assertBuyLimitOrderDetails(data.getTicker());
        buyLimit.submitOrder();
        buyLimit.goToHome();
        Assert.assertTrue(new PageObject.P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after buy limit submit");
        FlowLogger.step("BUY_FLOW", "Buy limit order with Use Max completed");
    }

    public void placeMarketOrder(TradeTestData data) {
        FlowLogger.step("BUY_FLOW", "Placing buy market order for " + data.getTicker());
        openBuyForTicker(data.getTicker());

        buyMarket.chooseMarketPrice();
        buyMarket.enterQuantity(data.getQuantity());
        buyMarket.reviewOrder();

        buyMarket.assertBuyLimitOrderDetails(data.getTicker());
        buyMarket.submitOrder();
        buyMarket.goToHome();
        Assert.assertTrue(new PageObject.P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after buy market submit");
        FlowLogger.step("BUY_FLOW", "Buy market order flow completed");
    }

    private void openBuyForTicker(String ticker) {
        FlowLogger.step("BUY_FLOW", "Opening Buy/Sell sheet and selecting Buy for ticker " + ticker);
        buyPre.openBuySellBottomSheet();
        buyPre.chooseBuy();
        buyPre.chooseAccount();
        buyPre.searchForTicker(ticker);
        buyPre.selectFirstTickerByName(ticker);
        buyPre.assertBuyPageOpened();
        buyPre.selectCustodian("CFH");
    }
}
