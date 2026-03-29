package TestObject;

import PageObject.P02_LoginPage;
import PageObject.P06_PreConditionSellPage;
import PageObject.P07_SellOrderLimitPage;
import PageObject.P08_SellOrderMarketPage;
import PageObject.P11_VerifyPinPage;
import org.testng.Assert;

public class SellFlow {
    private final P06_PreConditionSellPage sellPre = new P06_PreConditionSellPage();
    private final P07_SellOrderLimitPage sellLimit = new P07_SellOrderLimitPage();
    private final P08_SellOrderMarketPage sellMarket = new P08_SellOrderMarketPage();
    private final P11_VerifyPinPage verifyPinPage = new P11_VerifyPinPage();

    public void placeLimitOrder(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell limit order for " + data.getTicker());
        verifyPinPage.handleVerifyPinIfPresent();
        openSellForTicker(data.getTicker());

        sellLimit.chooseLimit();
        sellLimit.selectSettlement(data.getSettlement());
        sellLimit.enterSellLimitOrder(Integer.parseInt(data.getQuantity()), data.getPrice());
        verifyPinPage.handleVerifyPinIfPresent();
        sellLimit.clickReviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        verifyPinPage.handleVerifyPinIfPresent();
        sellLimit.goToHome();
        verifyPinPage.handleVerifyPinIfPresent();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after sell limit submit");
        FlowLogger.step("SELL_FLOW", "Sell limit order flow completed");
    }

    public void placeLimitOrderAndGoToHistory(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell limit order for " + data.getTicker() + " and opening order history");
        verifyPinPage.handleVerifyPinIfPresent();
        openSellForTicker(data.getTicker());

        sellLimit.chooseLimit();
        sellLimit.selectSettlement(data.getSettlement());
        sellLimit.enterSellLimitOrder(Integer.parseInt(data.getQuantity()), data.getPrice());
        verifyPinPage.handleVerifyPinIfPresent();
        sellLimit.clickReviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        verifyPinPage.handleVerifyPinIfPresent();
        sellLimit.goToHistory();

        FlowLogger.step("SELL_FLOW", "Sell limit order submitted and order history opened");
    }

    public void placeLimitOrderUsingMax(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell limit order with Use Max for " + data.getTicker());
        verifyPinPage.handleVerifyPinIfPresent();
        openSellForTicker(data.getTicker());

        sellLimit.chooseLimit();
        sellLimit.selectSettlement(data.getSettlement());
        sellLimit.enterSellLimitPrice(data.getPrice());
        sellLimit.tapUseMax();
        sellLimit.confirmUseMax();
        sellLimit.scrollToReviewOrder();
        verifyPinPage.handleVerifyPinIfPresent();
        sellLimit.clickReviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        verifyPinPage.handleVerifyPinIfPresent();
        sellLimit.goToHome();
        verifyPinPage.handleVerifyPinIfPresent();

        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected navigation to Home after sell limit submit");
        FlowLogger.step("SELL_FLOW", "Sell limit order with Use Max completed");
    }

    public void placeMarketOrder(TradeTestData data) {
        FlowLogger.step("SELL_FLOW", "Placing sell market order for " + data.getTicker());
        verifyPinPage.handleVerifyPinIfPresent();
        openSellForTicker(data.getTicker());

        sellMarket.chooseMarket();
        sellMarket.chooseSettlement(data.getSettlement());
        sellMarket.enterQuantity(data.getQuantity());
        verifyPinPage.handleVerifyPinIfPresent();
        sellMarket.reviewOrder();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        verifyPinPage.handleVerifyPinIfPresent();
        sellMarket.goToHistory();

        FlowLogger.step("SELL_FLOW", "Sell market order flow completed");
    }

    private void openSellForTicker(String ticker) {
        FlowLogger.step("SELL_FLOW", "Opening Buy/Sell sheet and selecting Sell for ticker " + ticker);
        verifyPinPage.handleVerifyPinIfPresent();
        sellPre.openBuySellBottomSheet();
        sellPre.chooseSell();
        sellPre.chooseAccount();
        sellPre.searchForTicker(ticker);
        sellPre.selectTicker(ticker);
        sellPre.assertSellPageOpened();
        sellPre.selectCustodian("CFH");
        verifyPinPage.handleVerifyPinIfPresent();
    }
}
