package TestObject;

import PageObject.P02_LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class T14_E2E_EditAndRepeatOrder extends BaseTest {

    @Test
    public void userCanEditAndRepeatOrderFromHistory() {
        new StartupFlow().completeStartupToLogin();

        LoginFlow loginFlow = new LoginFlow();
        loginFlow.loginFromCurrentScreen(
                System.getProperty("login.username", "01282349004"),
                System.getProperty("login.password", "@Testing08")
        );
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After login, user should be on home screen");

        TopUpFlow topUpFlow = new TopUpFlow();
        topUpFlow.completeTopUpFromCurrentSession();
        Assert.assertEquals(topUpFlow.getState(), TopUpFlow.TopUpState.SUCCESS, "Top-up did not reach success state");
        Assert.assertTrue(topUpFlow.isReturnedHome(), "Top-up did not navigate back to home/wallet");

        TradeTestData buyData = new TradeTestData(
                System.getProperty("e2e.buy.ticker", "OFH"),
                System.getProperty("e2e.buy.quantity", "1"),
                System.getProperty("e2e.buy.price", "0.590")
        );
        BuyFlow buyFlow = new BuyFlow();
        buyFlow.placeLimitOrderAndGoToHistory(buyData);

        OrderHistoryFlow orderHistoryFlow = new OrderHistoryFlow();
        orderHistoryFlow.editOrderAndUseMax(buyData.getTicker(), "Buy");

        TradeTestData sellData = new TradeTestData(
                System.getProperty("e2e.sell.ticker", "EFID"),
                System.getProperty("e2e.sell.quantity", "1"),
                System.getProperty("e2e.sell.price", "32.74"),
                System.getProperty("e2e.sell.settlement", "T+2")
        );
        new SellFlow().placeLimitOrderAndGoToHistory(sellData);

        orderHistoryFlow.repeatOrderFromHistory(sellData.getTicker(), "Sell");
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After repeat order flow, user should be on home screen");
    }
}
