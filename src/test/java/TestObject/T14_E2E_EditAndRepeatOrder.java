package TestObject;

import PageObject.P02_LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class T14_E2E_EditAndRepeatOrder extends BaseTest {

    @Test
    public void userCanEditAndRepeatOrderFromHistory() {
        new StartupFlow().completeStartupToLogin();

        LoginFlow loginFlow = new LoginFlow();
        loginFlow.loginFromCurrentScreen(
                System.getProperty("login.username", "01282349004"),
                System.getProperty("login.password", "@Testing08")
        );
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After login, user should be on home screen");

        WalletBalanceValidator walletBalanceValidator = new WalletBalanceValidator("T14");
        BigDecimal balanceBeforeTopUp = walletBalanceValidator.captureBalance("Before Top Up");

        TopUpFlow topUpFlow = new TopUpFlow();
        topUpFlow.completeTopUpFromCurrentSession();
        Assert.assertEquals(topUpFlow.getState(), TopUpFlow.TopUpState.SUCCESS, "Top-up did not reach success state");
        Assert.assertTrue(topUpFlow.isReturnedHome(), "Top-up did not navigate back to home/wallet");
        BigDecimal balanceAfterTopUp = walletBalanceValidator.captureBalance("After Top Up");
        walletBalanceValidator.assertIncreased("Top Up", balanceBeforeTopUp, balanceAfterTopUp);

        TradeTestData buyData = new TradeTestData(
                System.getProperty("e2e.buy.ticker", "OFH"),
                System.getProperty("e2e.buy.quantity", "1"),
                System.getProperty("e2e.buy.price", "0.590")
        );
        BuyFlow buyFlow = new BuyFlow();
        BigDecimal balanceBeforeBuy = walletBalanceValidator.captureBalance("Before Buy");
        buyFlow.placeLimitOrder(buyData);
        BigDecimal balanceAfterBuy = walletBalanceValidator.captureBalance("After Buy");
        walletBalanceValidator.assertDecreased("Buy", balanceBeforeBuy, balanceAfterBuy);

        OrderHistoryFlow orderHistoryFlow = new OrderHistoryFlow();
        String buyOrderId = orderHistoryFlow.captureLatestOrderId();
        String buyOrderStatus = orderHistoryFlow.getOrderStatusFromHistory(buyOrderId);
        Assert.assertFalse(buyOrderStatus.isBlank(), "Buy order status should be available in history");
        orderHistoryFlow.goHomeFromHistory();
        BigDecimal balanceBeforeEditedBuy = walletBalanceValidator.captureBalance("Before Edited Buy");
        orderHistoryFlow.editOrderAndUseMax(buyData.getTicker(), "Buy");
        BigDecimal balanceAfterEditedBuy = walletBalanceValidator.captureBalance("After Edited Buy");
        walletBalanceValidator.assertDecreased("Edited Buy", balanceBeforeEditedBuy, balanceAfterEditedBuy);

        TradeTestData sellData = new TradeTestData(
                System.getProperty("e2e.sell.ticker", "EFID"),
                System.getProperty("e2e.sell.quantity", "1"),
                System.getProperty("e2e.sell.price", "32.74"),
                System.getProperty("e2e.sell.settlement", "T+2")
        );
        BigDecimal balanceBeforeSell = walletBalanceValidator.captureBalance("Before Sell");
        new SellFlow().placeLimitOrder(sellData);
        BigDecimal balanceAfterSell = walletBalanceValidator.captureBalance("After Sell");
        walletBalanceValidator.assertIncreased("Sell", balanceBeforeSell, balanceAfterSell);
        String sellOrderId = orderHistoryFlow.captureLatestOrderId();
        String sellOrderStatus = orderHistoryFlow.getOrderStatusFromHistory(sellOrderId);
        Assert.assertFalse(sellOrderStatus.isBlank(), "Sell order status should be available in history");
        orderHistoryFlow.goHomeFromHistory();

        BigDecimal balanceBeforeRepeatedSell = walletBalanceValidator.captureBalance("Before Repeated Sell");
        orderHistoryFlow.repeatOrderFromHistory(sellData.getTicker(), "Sell");
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After repeat order flow, user should be on home screen");
        BigDecimal balanceAfterRepeatedSell = walletBalanceValidator.captureBalance("After Repeated Sell");
        walletBalanceValidator.assertIncreased("Repeated Sell", balanceBeforeRepeatedSell, balanceAfterRepeatedSell);
    }
}
