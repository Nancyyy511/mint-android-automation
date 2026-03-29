package TestObject;

import PageObject.P02_LoginPage;
import api.client.OrdersApi;
import api.client.TransactionsApi;
import api.models.DepositRequest;
import api.utils.AuthTokenProvider;
import api.utils.RetryUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class T13_E2E_TradingFlow_UsingMaxButton extends BaseTest {
    private static final String DEFAULT_TOPUP_RECEIPT_PATH = "C:\\Users\\nawny\\Downloads\\receipt (1).png";
    private static final String DEFAULT_TOPUP_DATE = "2025-08-11";
    private static final String DEFAULT_TOPUP_AMOUNT = "100";

    @Test
    public void userCanCompleteFullTradingJourneyUsingMaxButton() {
        String apiToken = AuthTokenProvider.getAuthToken();
        OrdersApi ordersApi = new OrdersApi();
        TransactionsApi transactionsApi = new TransactionsApi();

        new StartupFlow().completeStartupToLogin();

        LoginFlow loginFlow = new LoginFlow();
        loginFlow.loginFromCurrentScreen(
                System.getProperty("login.username", "01282349004"),
                System.getProperty("login.password", "@Testing08")
        );
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After login, user should be on home screen");

        WalletBalanceValidator walletBalanceValidator = new WalletBalanceValidator("T13");
        BigDecimal uiBalanceBeforeTopUp = walletBalanceValidator.captureUiBalance("Before Top Up");

        TopUpFlow topUpFlow = new TopUpFlow();
        topUpFlow.completeTopUpFromCurrentSession();
        Assert.assertEquals(topUpFlow.getState(), TopUpFlow.TopUpState.SUCCESS, "Top-up did not reach success state");
        Assert.assertTrue(topUpFlow.isReturnedHome(), "Top-up did not navigate back to home/wallet");
        BigDecimal uiBalanceAfterTopUp = walletBalanceValidator.captureUiBalance("After Top Up");
        walletBalanceValidator.assertIncreased("Top Up", uiBalanceBeforeTopUp, uiBalanceAfterTopUp);

        DepositRequest depositRequest = new DepositRequest(
                System.getProperty("api.accountId", "8221001"),
                System.getProperty("topup.amount", DEFAULT_TOPUP_AMOUNT),
                System.getProperty("topup.apiDate", DEFAULT_TOPUP_DATE),
                resolveReceiptPath()
        );
        transactionsApi.deposit(apiToken, depositRequest);

        TradeTestData buyData = new TradeTestData(
                System.getProperty("e2e.buy.ticker", "OFH"),
                System.getProperty("e2e.buy.quantity", "1"),
                System.getProperty("e2e.buy.price", "0.590")
        );
        BigDecimal uiBalanceBeforeBuy = walletBalanceValidator.captureUiBalance("Before Buy");
        BigDecimal apiBalanceBeforeBuy = walletBalanceValidator.captureApiBalance(apiToken, "Before Buy");
        new BuyFlow().placeLimitOrderUsingMax(buyData);
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After buy flow, user should be on home screen");
        BigDecimal uiBalanceAfterBuy = walletBalanceValidator.captureUiBalance("After Buy");
        BigDecimal apiBalanceAfterBuy = walletBalanceValidator.captureApiBalance(apiToken, "After Buy");
        walletBalanceValidator.assertBalanceDecreased(
                "Buy",
                uiBalanceBeforeBuy,
                uiBalanceAfterBuy,
                apiBalanceBeforeBuy,
                apiBalanceAfterBuy
        );
        OrderHistoryFlow orderHistoryFlow = new OrderHistoryFlow();
        String buyOrderId = orderHistoryFlow.captureLatestOrderId();
        RetryUtils.waitForCondition(
                () -> "EXECUTED".equalsIgnoreCase(ordersApi.getOrderStatus(apiToken, buyOrderId)),
                Duration.ofSeconds(45),
                Duration.ofSeconds(3)
        );
        String buyOrderStatus = ordersApi.getOrderStatus(apiToken, buyOrderId);
        Assert.assertEquals(buyOrderStatus, "EXECUTED", "Buy order status should become EXECUTED");
        orderHistoryFlow.goHomeFromHistory();

        TradeTestData sellData = new TradeTestData(
                System.getProperty("e2e.sell.ticker", "EFID"),
                System.getProperty("e2e.sell.quantity", "1"),
                System.getProperty("e2e.sell.price", "32.74"),
                System.getProperty("e2e.sell.settlement", "T+2")
        );
        BigDecimal uiBalanceBeforeSell = walletBalanceValidator.captureUiBalance("Before Sell");
        BigDecimal apiBalanceBeforeSell = walletBalanceValidator.captureApiBalance(apiToken, "Before Sell");
        new SellFlow().placeLimitOrderUsingMax(sellData);
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "After sell flow, user should be on home screen");
        BigDecimal uiBalanceAfterSell = walletBalanceValidator.captureUiBalance("After Sell");
        BigDecimal apiBalanceAfterSell = walletBalanceValidator.captureApiBalance(apiToken, "After Sell");
        walletBalanceValidator.assertBalanceIncreased(
                "Sell",
                uiBalanceBeforeSell,
                uiBalanceAfterSell,
                apiBalanceBeforeSell,
                apiBalanceAfterSell
        );
        String sellOrderId = orderHistoryFlow.captureLatestOrderId();
        String sellOrderStatus = ordersApi.getOrderStatus(apiToken, sellOrderId);
        Assert.assertFalse(sellOrderStatus.isBlank(), "Sell order status should be available from API");
        orderHistoryFlow.goHomeFromHistory();
    }

    private Path resolveReceiptPath() {
        return Paths.get(System.getProperty("topup.receiptPath", DEFAULT_TOPUP_RECEIPT_PATH));
    }
}
