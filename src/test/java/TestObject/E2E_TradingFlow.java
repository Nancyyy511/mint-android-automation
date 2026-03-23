package TestObject;

import PageObject.P02_LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

public class E2E_TradingFlow extends BaseTest {

    @Test
    public void userCanCompleteFullTradingJourney() {
        String step = "Initialization";

        try {
            step = "Onboarding + Location + Navigate to Login";
            FlowLogger.step("E2E", "Starting onboarding");
            new StartupFlow().completeStartupToLogin();

            step = "Login";
            FlowLogger.step("E2E", "Starting login");
            LoginFlow loginFlow = new LoginFlow();
            loginFlow.loginFromCurrentScreen(
                    System.getProperty("login.username", "01282349004"),
                    System.getProperty("login.password", "@Testing09")
            );
            Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After login, user should be on home screen");
            FlowLogger.step("E2E", "Login successful");

            step = "Top-up";
            FlowLogger.step("E2E", "Starting top-up");
            TopUpFlow topUpFlow = new TopUpFlow();
            topUpFlow.completeTopUpFromCurrentSession();
            Assert.assertEquals(topUpFlow.getState(), TopUpFlow.TopUpState.SUCCESS, "Top-up did not reach success state");
            Assert.assertTrue(topUpFlow.isReturnedHome(), "Top-up did not navigate back to home/wallet");
            FlowLogger.step("E2E", "Top-up completed");

            step = "Buy Limit Order";
            FlowLogger.step("E2E", "Starting buy flow");
            TradeTestData buyData = new TradeTestData(
                    System.getProperty("e2e.buy.ticker", "OFH"),
                    System.getProperty("e2e.buy.quantity", "1"),
                    System.getProperty("e2e.buy.price", "0.590")
            );
            new BuyFlow().placeLimitOrder(buyData);
            Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After buy flow, user should be on home screen");
            FlowLogger.step("E2E", "Buy order placed");

            step = "Sell Limit Order";
            FlowLogger.step("E2E", "Starting sell flow");
            TradeTestData sellData = new TradeTestData(
                    System.getProperty("e2e.sell.ticker", "NHPS"),
                    System.getProperty("e2e.sell.quantity", "1"),
                    System.getProperty("e2e.sell.price", "94.00"),
                    System.getProperty("e2e.sell.settlement", "T+2")
            );
            new SellFlow().placeLimitOrder(sellData);
            Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After sell flow, user should be on home screen");
            FlowLogger.step("E2E", "Sell order placed");
        } catch (Exception exception) {
            captureFailureArtifacts("E2E_TradingFlow");
            FlowLogger.step("E2E", "Failed at step: " + step + " | Reason: " + exception.getMessage());
            throw new RuntimeException("[E2E] Failed at step: " + step + " | Reason: " + exception.getMessage(), exception);
        }
    }

    private void captureFailureArtifacts(String testName) {
        try {
            Path screenshot = ScreenshotUtils.capture(testName);
            if (screenshot != null) {
                FlowLogger.step("E2E", "Screenshot captured: " + screenshot);
            }
        } catch (Exception exception) {
            FlowLogger.step("E2E", "Screenshot capture failed: " + exception.getMessage());
        }
    }
}
