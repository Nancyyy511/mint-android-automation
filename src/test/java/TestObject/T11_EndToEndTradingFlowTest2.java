package TestObject;

import PageObject.*;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import org.testng.Assert;

// Buy Market + Sell Market
public class T11_EndToEndTradingFlowTest2 extends BaseTest {

    @Test
    public void userCanBuyMarketThenSellMarketSuccessfully() {

        new StartupFlow().completeStartupToLogin();


        // ===== Login =====
        P02_LoginPage login = new P02_LoginPage();
        login.login("01282349004", "@Testing08");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();
        login.waitForHomeScreen();
        WalletBalanceValidator walletBalanceValidator = new WalletBalanceValidator("T11");
        BigDecimal balanceBeforeBuy = walletBalanceValidator.captureBalance("Before Buy");
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
        buyMarket.enterQuantity("5");
        buyMarket.reviewOrder();
        buyMarket.submitOrder();

        // ===== GO HOME =====
        buyMarket.goToHome();
        BigDecimal balanceAfterBuy = walletBalanceValidator.captureBalance("After Buy");
        walletBalanceValidator.assertDecreased("Buy", balanceBeforeBuy, balanceAfterBuy);
        OrderHistoryFlow orderHistoryFlow = new OrderHistoryFlow();
        String buyOrderId = orderHistoryFlow.captureLatestOrderId();
        String buyOrderStatus = orderHistoryFlow.getOrderStatusFromHistory(buyOrderId);
        Assert.assertFalse(buyOrderStatus.isBlank(), "Buy order status should be available in history");
        orderHistoryFlow.goHomeFromHistory();

        // ===== SELL MARKET =====
        BigDecimal balanceBeforeSell = walletBalanceValidator.captureBalance("Before Sell");
        P06_PreConditionSellPage sellPre = new P06_PreConditionSellPage();
        P08_SellOrderMarketPage sellMarket = new P08_SellOrderMarketPage();

        sellPre.openBuySellBottomSheet();
        sellPre.chooseSell();
        sellPre.chooseAccount();
        sellPre.searchForTicker("OFH");
        sellPre.selectTicker("OFH");
        sellPre.assertSellPageOpened();

        sellMarket.chooseMarket();
        sellMarket.chooseSettlement("T+0");
        sellMarket.enterQuantity("5");
        sellMarket.reviewOrder();

        // ===== Assertions =====
        sellPre.assertReviewSellPageOpened();
        sellPre.assertTickerDisplayed();
        sellPre.assertQuantityDisplayed();
        sellPre.assertSubmitEnabled();
        sellPre.submitSellOrder();
        sellMarket.goToHome();

        BigDecimal balanceAfterSell = walletBalanceValidator.captureBalance("After Sell");
        walletBalanceValidator.assertIncreased("Sell", balanceBeforeSell, balanceAfterSell);
        String sellOrderId = orderHistoryFlow.captureLatestOrderId();
        String sellOrderStatus = orderHistoryFlow.getOrderStatusFromHistory(sellOrderId);
        Assert.assertFalse(sellOrderStatus.isBlank(), "Sell order status should be available in history");
        orderHistoryFlow.goHomeFromHistory();

    }
}
