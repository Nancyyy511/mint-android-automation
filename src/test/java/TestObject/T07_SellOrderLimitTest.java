package TestObject;

import org.testng.annotations.Test;

public class T07_SellOrderLimitTest extends BaseTest {
    @Test(dataProvider = "sellLimitData", dataProviderClass = TradeDataProvider.class)
    public void sellLimitOrderSuccessfully(TradeTestData data) {
        new LoginFlow().loginToHome();
        new SellFlow().placeLimitOrder(data);
    }
}
