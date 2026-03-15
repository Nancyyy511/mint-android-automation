package TestObject;

import PageObject.P06_PreConditionSellPage;
import org.testng.annotations.Test;

public class T06_PreConditionSellTest extends Core{
    //@Test
    public void openSellOrderSuccessfully() {

        P06_PreConditionSellPage sell = new P06_PreConditionSellPage();

        sell.openBuySellBottomSheet();
        sell.chooseSell();
        sell.chooseAccount();
        sell.searchForTicker("OFH");
        sell.selectTicker("OFH");
        sell.assertSellPageOpened();
    }

}
