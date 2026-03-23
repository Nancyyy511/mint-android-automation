package TestObject;

import org.testng.annotations.Test;

public class T13_RefactoredTradingFlowTest extends BaseTest {

    @Test(dataProvider = "buyLimitData", dataProviderClass = TradeDataProvider.class)
    public void userCanPlaceBuyLimitOrderWithFlow(TradeTestData data) {
        new LoginFlow().loginToHome();
        new BuyFlow().placeLimitOrder(data);
    }

    @Test(dataProvider = "sellLimitData", dataProviderClass = TradeDataProvider.class)
    public void userCanPlaceSellLimitOrderWithFlow(TradeTestData data) {
        new LoginFlow().loginToHome();
        new SellFlow().placeLimitOrder(data);
    }
}

