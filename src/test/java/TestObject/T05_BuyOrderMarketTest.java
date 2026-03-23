package TestObject;

import org.testng.annotations.Test;

public class T05_BuyOrderMarketTest extends BaseTest {
    @Test(dataProvider = "buyMarketData", dataProviderClass = TradeDataProvider.class)
    public void buyMarketOrderSuccessfully(TradeTestData data) {
        new LoginFlow().loginToHome();
        new BuyFlow().placeMarketOrder(data);
    }
}
