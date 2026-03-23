package TestObject;

import org.testng.annotations.Test;

public class T04_BuyOrderLimitTest extends BaseTest {

    @Test(dataProvider = "buyLimitData", dataProviderClass = TradeDataProvider.class)
    public void buyLimitOrderSuccessfully(TradeTestData data) {
        new LoginFlow().loginToHome();
        new BuyFlow().placeLimitOrder(data);
    }
}
