package TestObject;

import PageObject.P03_PreConditionBuyPage;
import PageObject.P05_BuyOrderMarketPage;
import org.testng.annotations.Test;

public class T05_BuyOrderMarketTest extends BaseTest {
   @Test
    public void buyMarketOrderSuccessfully() throws InterruptedException {
        P03_PreConditionBuyPage preConditionBuyPage =
                new P03_PreConditionBuyPage();
        P05_BuyOrderMarketPage marketPage =
                new P05_BuyOrderMarketPage();

        preConditionBuyPage.openBuySellBottomSheet();
        preConditionBuyPage.chooseBuy();
        preConditionBuyPage.chooseAccount();

        Thread.sleep(1000);


        preConditionBuyPage.searchForTicker("OFH");
        Thread.sleep(1000);


        preConditionBuyPage.selectFirstTickerByName("OFH");
        preConditionBuyPage.assertBuyPageOpened();

        Thread.sleep(1000);
        marketPage.chooseMarketPrice();
        marketPage.enterQuantity("5");
        marketPage.reviewOrder();

        marketPage.assertBuyLimitOrderDetails();
        marketPage.submitOrder();
        marketPage.goToHome();




    }
}
