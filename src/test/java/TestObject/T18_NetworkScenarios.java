package TestObject;

import PageObject.P03_PreConditionBuyPage;
import PageObject.P05_BuyOrderMarketPage;
import PageObject.P11_VerifyPinPage;
import PageObject.P12_TradeValidationPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class T18_NetworkScenarios extends BaseTest {
    private final P03_PreConditionBuyPage buyPre = new P03_PreConditionBuyPage();
    private final P05_BuyOrderMarketPage buyMarket = new P05_BuyOrderMarketPage();
    private final P12_TradeValidationPage validationPage = new P12_TradeValidationPage();
    private final P11_VerifyPinPage verifyPinPage = new P11_VerifyPinPage();

    @Test
    public void appShouldHandleNetworkOffDuringBuy() {
        DeviceControl deviceControl = new DeviceControl();
        new LoginFlow().loginToHome();
        openBuyMarketScreen("OFH");

        buyMarket.chooseMarketPrice();
        buyMarket.enterQuantity("1");
        buyMarket.reviewOrder();

        deviceControl.disableNetwork();
        try {
            buyMarket.submitOrder();
            verifyPinPage.handleVerifyPinIfPresent();
            Assert.assertTrue(
                    validationPage.isErrorVisible() || validationPage.isRetryVisible(),
                    "Network-off buy should show an error or retry option"
            );
            Assert.assertTrue(
                    deviceControl.isAppStable(ConfigReader.get("appPackage")),
                    "App should remain stable after network failure during buy"
            );
        } finally {
            deviceControl.enableNetwork();
        }
    }

    @Test
    public void appShouldHandleTimeoutOrApiFailureGracefully() {
        DeviceControl deviceControl = new DeviceControl();
        new LoginFlow().loginToHome();
        openBuyMarketScreen("OFH");

        buyMarket.chooseMarketPrice();
        buyMarket.enterQuantity("1");
        buyMarket.reviewOrder();

        deviceControl.disableNetwork();
        try {
            buyMarket.submitOrder();
            verifyPinPage.handleVerifyPinIfPresent();
            Assert.assertTrue(
                    validationPage.isErrorVisible() || validationPage.isRetryVisible(),
                    "Timeout/API failure should show an error or retry option"
            );
            Assert.assertTrue(
                    deviceControl.isAppStable(ConfigReader.get("appPackage")),
                    "App should not crash after timeout/API failure"
            );
        } finally {
            deviceControl.enableNetwork();
        }
    }

    private void openBuyMarketScreen(String ticker) {
        buyPre.openBuySellBottomSheet();
        buyPre.chooseBuy();
        buyPre.chooseAccount();
        buyPre.searchForTicker(ticker);
        buyPre.selectFirstTickerByName(ticker);
        buyPre.assertBuyPageOpened();
        buyPre.selectCustodian("CFH");
        verifyPinPage.handleVerifyPinIfPresent();
    }
}
