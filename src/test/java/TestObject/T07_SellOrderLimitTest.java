package TestObject;

import PageObject.P01_OnboardingPage;
import PageObject.P02_LoginPage;
import PageObject.P06_PreConditionSellPage;
import PageObject.P07_SellOrderLimitPage;
import org.testng.annotations.Test;

public class T07_SellOrderLimitTest extends Core {
    @Test
    public void sellLimitOrderSuccessfully() {
        P01_OnboardingPage onboarding = new P01_OnboardingPage();
        onboarding.completeOnboardingAndGoToLogin();       // Choose Log in


        // ===== Login =====
        P02_LoginPage login = new P02_LoginPage();
        login.login("01282349004", "@Testing09");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();

        P06_PreConditionSellPage sell = new P06_PreConditionSellPage();
       P07_SellOrderLimitPage sellLimit = new P07_SellOrderLimitPage();

        sell.openBuySellBottomSheet();
        sell.chooseSell();
        sell.chooseAccount();
        sell.searchForTicker("NHPS");
        sell.selectTicker("NHPS");
        sell.assertSellPageOpened();

        sellLimit.chooseLimit();
        sellLimit.enterSellLimitOrder(1, "T+2");
        sellLimit.clickReviewOrder();
        sellLimit.submitOrder();
        sellLimit.goToHome();

        // ===== Assertions =====
        sell.assertReviewSellPageOpened();
        sell.assertTickerDisplayed();
        sell.assertQuantityDisplayed();
        sell.assertPriceDisplayed();
        sell.assertSettlementDisplayed("T+2");
        sell.assertSubmitEnabled();

        sell.submitSellOrder();
        sellLimit.goToHome();



    }
}
