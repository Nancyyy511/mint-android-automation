package TestObject;

import PageObject.*;
import org.testng.annotations.Test;

//Buy Market Sell Limit
//T10_EndToEndTradingFlow1  → Buy Market + Sell Limit
public class T10_EndToEndTradingFlowTest1 extends BaseTest{
    @Test
    public void userCanBuyMarketThenSellSuccessfully() {

        new StartupFlow().completeStartupToLogin();


        // ===== Login =====
        P02_LoginPage login = new P02_LoginPage();
        login.login("01282349004", "@Testing08");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();
        login.waitForHomeScreen();


        // ===== BUY MARKET =====
        P03_PreConditionBuyPage buyPre = new P03_PreConditionBuyPage();
        P05_BuyOrderMarketPage buyMarket = new P05_BuyOrderMarketPage();

        buyPre.openBuySellBottomSheet();
        buyPre.chooseBuy();
        buyPre.chooseAccount();
        buyPre.searchForTicker("OFH");
        buyPre.selectFirstTickerByName("OFH");
        buyPre.assertBuyPageOpened();

        buyMarket.chooseMarketPrice();
        buyMarket.enterQuantity("1");
        buyMarket.reviewOrder();
        buyMarket.assertBuyLimitOrderDetails("OFH");
        buyMarket.submitOrder();



        // ===== GO HOME =====
        buyMarket.goToHome();

        // ===== SELL LIMIT =====
        P06_PreConditionSellPage sellPre = new P06_PreConditionSellPage();
        P07_SellOrderLimitPage sellLimit = new P07_SellOrderLimitPage();

        sellPre.openBuySellBottomSheet();
        sellPre.chooseSell();
        sellPre.chooseAccount();
        sellPre.searchForTicker("OFH");
        sellPre.selectTicker("OFH");
        sellPre.assertSellPageOpened();

        sellLimit.chooseLimit();
        sellLimit.enterSellLimitOrder(1, "T+2");
        sellLimit.clickReviewOrder();
        sellLimit.goToHome();

        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
    }

}
