package TestObject;

import PageObject.*;
import org.testng.annotations.Test;

// Buy Market + Sell Market
public class T11_EndToEndTradingFlowTest2 extends Core {

    @Test
    public void userCanBuyMarketThenSellMarketSuccessfully() {

        // ===== Onboarding =====
        P01_OnboardingPage onboarding = new P01_OnboardingPage();
        onboarding.completeOnboardingAndGoToLogin();      // Choose Log in


        // ===== Login =====
        P02_LoginPage login = new P02_LoginPage();
        login.login("01282349004", "@Testing09");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();
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
        buyMarket.enterQuantity("5");
        buyMarket.reviewOrder();
        buyMarket.submitOrder();

        // ===== GO HOME =====
        buyMarket.goToHome();

        // ===== SELL MARKET =====
        P06_PreConditionSellPage sellPre = new P06_PreConditionSellPage();
        P08_SellOrderMarketPage sellMarket = new P08_SellOrderMarketPage();

        sellPre.openBuySellBottomSheet();
        sellPre.chooseSell();
        sellPre.chooseAccount();4
        sellPre.searchForTicker("OFH");
        sellPre.selectTicker("OFH");
        sellPre.assertSellPageOpened();

        sellMarket.chooseMarket();
        sellMarket.chooseSettlement("T+0");
        sellMarket.enterQuantity("5");
        sellMarket.reviewOrder();
        sellPre.submitSellOrder();
        sellMarket.goToHistory();

        // ===== Assertions =====
        sellPre.assertReviewSellPageOpened();
        sellPre.assertTickerDisplayed();
        sellPre.assertQuantityDisplayed();
        sellPre.assertSubmitEnabled();


    }
}
