package TestObject;

import org.testng.annotations.Test;

public class T08_SellOrderMarketTest extends BaseTest {
    @Test(dataProvider = "sellMarketData", dataProviderClass = TradeDataProvider.class)
    public void sellMarketOrderSuccessfully(TradeTestData data) {
        new LoginFlow().loginToHome();
        new SellFlow().placeMarketOrder(data);
    }
}
