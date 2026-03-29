package TestObject;

import PageObject.*;
import api.client.OrdersApi;
import api.client.WalletApi;
import api.utils.AuthTokenProvider;
import api.utils.RetryUtils;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.Duration;

import org.testng.Assert;

//Buy Market Sell Limit
//T10_EndToEndTradingFlow1  → Buy Market + Sell Limit
public class T10_EndToEndTradingFlowTest1 extends BaseTest{
    @Test
    public void userCanBuyMarketThenSellSuccessfully() {
        WalletApi walletApi = new WalletApi();
        OrdersApi ordersApi = new OrdersApi();
        String apiToken = AuthTokenProvider.getAuthToken();
        String username = System.getProperty("login.username", "01282349004");
        String password = System.getProperty("login.password", "@Testing08");

        new StartupFlow().completeStartupToLogin();


        // ===== Login =====
        P02_LoginPage login = new P02_LoginPage();
        login.login(username, password);
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();
        login.waitForHomeScreen();

        BigDecimal balanceBeforeBuy = walletApi.getBalance(apiToken);
        FlowLogger.step("T10", "API balance before buy: " + balanceBeforeBuy.toPlainString());


        // ===== BUY MARKET =====
        P03_PreConditionBuyPage buyPre = new P03_PreConditionBuyPage();
        P05_BuyOrderMarketPage buyMarket = new P05_BuyOrderMarketPage();

        buyPre.openBuySellBottomSheet();
        buyPre.chooseBuy();
        buyPre.chooseAccount();
        buyPre.searchForTicker("OFH");
        buyPre.selectFirstTickerByName("OFH");
        buyPre.assertBuyPageOpened();

        buyMarket.chooseMarketPrice();
        buyMarket.enterQuantity("1");
        buyMarket.reviewOrder();
        buyMarket.assertBuyLimitOrderDetails("OFH");
        buyMarket.submitOrder();



        // ===== GO HOME =====
        buyMarket.goToHome();
        BigDecimal balanceAfterBuy = walletApi.getBalance(apiToken);
        FlowLogger.step("T10", "API balance after buy: " + balanceAfterBuy.toPlainString());
        Assert.assertTrue(
                balanceAfterBuy.compareTo(balanceBeforeBuy) < 0,
                "Buy should decrease API wallet balance. before=" + balanceBeforeBuy + ", after=" + balanceAfterBuy
        );

        OrderHistoryFlow orderHistoryFlow = new OrderHistoryFlow();
        String buyOrderId = orderHistoryFlow.captureLatestOrderId();
        RetryUtils.waitForCondition(
                () -> "EXECUTED".equalsIgnoreCase(ordersApi.getOrderStatus(apiToken, buyOrderId)),
                Duration.ofSeconds(30),
                Duration.ofSeconds(3)
        );
        String latestBuyOrderStatus = ordersApi.getOrderStatus(apiToken, buyOrderId);
        FlowLogger.step("T10", "Buy order from API. id=" + buyOrderId
                + ", status=" + latestBuyOrderStatus);
        Assert.assertEquals(latestBuyOrderStatus, "EXECUTED",
                "Buy order should become EXECUTED");
        orderHistoryFlow.goHomeFromHistory();

        // ===== SELL LIMIT =====
        BigDecimal balanceBeforeSell = walletApi.getBalance(apiToken);
        P06_PreConditionSellPage sellPre = new P06_PreConditionSellPage();
        P07_SellOrderLimitPage sellLimit = new P07_SellOrderLimitPage();

        sellPre.openBuySellBottomSheet();
        sellPre.chooseSell();
        sellPre.chooseAccount();
        sellPre.searchForTicker("OFH");
        sellPre.selectTicker("OFH");
        sellPre.assertSellPageOpened();

        sellLimit.chooseLimit();
        sellLimit.enterSellLimitOrder(1, "T+2");
        sellLimit.clickReviewOrder();
        sellPre.assertReviewSellPageOpened();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        sellLimit.goToHome();

        BigDecimal balanceAfterSell = walletApi.getBalance(apiToken);
        Assert.assertTrue(
                balanceAfterSell.compareTo(balanceBeforeSell) >= 0,
                "Sell should not reduce API wallet balance. before=" + balanceBeforeSell + ", after=" + balanceAfterSell
        );
    }

}
