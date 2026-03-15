package TestObject;

import PageObject.P01_OnboardingPage;
import PageObject.P02_LoginPage;
import PageObject.P06_PreConditionSellPage;
import PageObject.P08_SellOrderMarketPage;
import org.testng.annotations.Test;

public class T08_SellOrderMarketTest extends Core{
    @Test
    public void sellMarketOrderSuccessfully() {
        P01_OnboardingPage onboarding = new P01_OnboardingPage();
        onboarding.completeOnboardingAndGoToLogin();       // Choose Log in


        // ===== Login =====
        P02_LoginPage login = new P02_LoginPage();
        login.login("01282349004", "@Testing09");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();
    P06_PreConditionSellPage sell = new P06_PreConditionSellPage();
    P08_SellOrderMarketPage sellMarket = new P08_SellOrderMarketPage();


        sell.openBuySellBottomSheet();
        sell.chooseSell();
        sell.chooseAccount();
        sell.searchForTicker("NHPS");
        sell.selectTicker("NHPS");
        sell.assertSellPageOpened();


        sellMarket.chooseMarket();
        sellMarket.chooseSettlement("T+2");
        sellMarket.enterQuantity("1");
        sellMarket.reviewOrder();

        // ===== Assertions =====
        sell.assertReviewSellPageOpened();
        sell.assertTickerDisplayed();
        sell.assertQuantityDisplayed();
        sell.assertPriceDisplayed();
        sell.assertSettlementDisplayed("T+2");
        sell.assertSubmitEnabled();

        sell.submitSellOrder();

        sellMarket.goToHistory();

    }


}
