package TestObject;

import PageObject.P03_PreConditionBuyPage;
import org.testng.annotations.Test;

public class T03_PreConditionBuyTest extends BaseTest {

    //@Test(priority = 3)
    public void openBuyOrderPageSuccessfully() {

        P03_PreConditionBuyPage preCondition =
                new P03_PreConditionBuyPage();

        preCondition.openBuySellBottomSheet();
        preCondition.chooseBuy();
        preCondition.chooseAccount();
        preCondition.searchForTicker("OFH");
        preCondition.selectFirstTickerByName("OFH");
        preCondition.assertBuyPageOpened();

    }
}
