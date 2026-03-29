package TestObject;

import PageObject.P03_PreConditionBuyPage;
import PageObject.P04_BuyOrderLimitPage;
import PageObject.P06_PreConditionSellPage;
import PageObject.P07_SellOrderLimitPage;
import PageObject.P11_VerifyPinPage;
import PageObject.P12_TradeValidationPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class T15_EdgeCases_Trading extends BaseTest {
    private final P03_PreConditionBuyPage buyPre = new P03_PreConditionBuyPage();
    private final P04_BuyOrderLimitPage buyLimit = new P04_BuyOrderLimitPage();
    private final P06_PreConditionSellPage sellPre = new P06_PreConditionSellPage();
    private final P07_SellOrderLimitPage sellLimit = new P07_SellOrderLimitPage();
    private final P12_TradeValidationPage validationPage = new P12_TradeValidationPage();
    private final P11_VerifyPinPage verifyPinPage = new P11_VerifyPinPage();

    @Test
    public void buyWithZeroQuantityShouldBeRejected() {
        new LoginFlow().loginToHome();
        openBuyLimitScreen("OFH");

        buyLimit.enterQuantity("0");
        buyLimit.enterSetPrice("0.590");
        validationPage.assertDisabledActionOrErrorVisible("Buy with zero quantity");
        Assert.assertTrue(
                validationPage.isErrorVisible() || !validationPage.isReviewOrderEnabled(),
                "Buy with zero quantity should show validation or keep review disabled"
        );
    }

    @Test
    public void buyBelowMinimumAmountShouldBeRejected() {
        new LoginFlow().loginToHome();
        openBuyLimitScreen("OFH");

        buyLimit.enterQuantity("1");
        buyLimit.enterSetPrice("0.0001");
        validationPage.assertDisabledActionOrErrorVisible("Buy below minimum amount");
    }

    @Test
    public void sellWithInsufficientSharesShouldShowErrorHandling() {
        new LoginFlow().loginToHome();
        openSellLimitScreen("OFH");

        sellLimit.chooseLimit();
        sellLimit.enterSellLimitOrder(999999, "0.590");
        validationPage.assertDisabledActionOrErrorVisible("Sell with insufficient shares");
    }

    @Test
    public void invalidBuyPricesShouldBeRejected() {
        new LoginFlow().loginToHome();

        openBuyLimitScreen("OFH");
        buyLimit.enterQuantity("1");
        buyLimit.enterSetPrice("999999");
        validationPage.assertDisabledActionOrErrorVisible("Buy with extremely high price");

        new LoginFlow().loginToHome();
        openBuyLimitScreen("OFH");
        buyLimit.enterQuantity("1");
        buyLimit.enterSetPrice("0.000001");
        validationPage.assertDisabledActionOrErrorVisible("Buy with extremely low price");
    }

    private void openBuyLimitScreen(String ticker) {
        verifyPinPage.handleVerifyPinIfPresent();
        buyPre.openBuySellBottomSheet();
        buyPre.chooseBuy();
        buyPre.chooseAccount();
        buyPre.searchForTicker(ticker);
        buyPre.selectFirstTickerByName(ticker);
        buyPre.assertBuyPageOpened();
        buyPre.selectCustodian("CFH");
        verifyPinPage.handleVerifyPinIfPresent();
    }

    private void openSellLimitScreen(String ticker) {
        verifyPinPage.handleVerifyPinIfPresent();
        sellPre.openBuySellBottomSheet();
        sellPre.chooseSell();
        sellPre.chooseAccount();
        sellPre.searchForTicker(ticker);
        sellPre.selectTicker(ticker);
        sellPre.assertSellPageOpened();
        sellPre.selectCustodian("CFH");
        verifyPinPage.handleVerifyPinIfPresent();
    }
}
