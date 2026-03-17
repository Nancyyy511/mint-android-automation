package TestObject;

import PageObject.P01_OnboardingPage;
import PageObject.P02_LoginPage;
import PageObject.P03_PreConditionBuyPage;
import PageObject.P04_BuyOrderLimitPage;
import org.testng.annotations.Test;

public class T04_BuyOrderLimitTest extends BaseTest {

    @Test
    public void buyLimitOrderSuccessfully() throws InterruptedException {
        P01_OnboardingPage onboarding = new P01_OnboardingPage();
        onboarding.completeOnboardingAndGoToLogin();       // Choose Log in


        // ===== Login =====
        P02_LoginPage login = new P02_LoginPage();
        login.login("01282349004", "@Testing09");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();


        P03_PreConditionBuyPage preConditionBuyPage =
                new P03_PreConditionBuyPage();
        P04_BuyOrderLimitPage buyOrderLimitPage =
                new P04_BuyOrderLimitPage();

        preConditionBuyPage.openBuySellBottomSheet();
        preConditionBuyPage.chooseBuy();
        preConditionBuyPage.chooseAccount();

        Thread.sleep(1000);


        preConditionBuyPage.searchForTicker("OFH");
        Thread.sleep(1000);


        preConditionBuyPage.selectFirstTickerByName("OFH");
        preConditionBuyPage.assertBuyPageOpened();

        Thread.sleep(1000);

        buyOrderLimitPage.enterQuantity("1");
        buyOrderLimitPage.enterSetPrice("0.590");
        buyOrderLimitPage.getValue();
        buyOrderLimitPage.scrollToReviewOrder();
        buyOrderLimitPage.clickReviewOrder();

        buyOrderLimitPage.assertBuyLimitOrderDetails();
        buyOrderLimitPage.submitOrder();
        buyOrderLimitPage.goToHome();
    }
}
