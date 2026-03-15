package TestObject;

import PageObject.P03_PreConditionBuyPage;
import org.testng.annotations.Test;

public class T03_PreConditionBuyTest extends Core {

    //@Test(priority = 3)
    public void openBuyOrderPageSuccessfully() throws InterruptedException {

        P03_PreConditionBuyPage preCondition =
                new P03_PreConditionBuyPage();

        preCondition.openBuySellBottomSheet();
        preCondition.chooseBuy();
        preCondition.chooseAccount();

        Thread.sleep(1000);

        preCondition.searchForTicker("OFH");
        preCondition.selectFirstTickerByName("OFH");
        preCondition.assertBuyPageOpened();

    }
}
