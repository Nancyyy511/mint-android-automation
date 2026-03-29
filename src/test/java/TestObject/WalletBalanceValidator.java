package TestObject;

import PageObject.P10_TopUpPage;
import api.client.WalletApi;
import io.qameta.allure.Step;
import org.testng.Assert;

import java.math.BigDecimal;

public class WalletBalanceValidator {
    private final String flowName;
    private final P10_TopUpPage topUpPage = new P10_TopUpPage();
    private final WalletApi walletApi = new WalletApi();

    public WalletBalanceValidator(String flowName) {
        this.flowName = flowName;
    }

    @Step("Capture UI balance for {checkpoint}")
    public BigDecimal captureUiBalance(String checkpoint) {
        BigDecimal balance = topUpPage.getWalletBalance();
        System.out.println("[UI][BALANCE] " + checkpoint + ": " + balance.toPlainString());
        FlowLogger.step(flowName, checkpoint + " UI wallet balance: " + balance.toPlainString() + " EGP");
        return balance;
    }

    @Step("Capture API balance")
    public BigDecimal captureApiBalance(String token, String checkpoint) {
        BigDecimal balance = walletApi.getBalance(token);
        System.out.println("[API][BALANCE] " + checkpoint + ": " + balance.toPlainString());
        FlowLogger.step(flowName, checkpoint + " API wallet balance: " + balance.toPlainString() + " EGP");
        return balance;
    }

    public BigDecimal captureBalance(String checkpoint) {
        return captureUiBalance(checkpoint);
    }

    public void assertBalanceDecreased(String action,
                                       BigDecimal uiBefore,
                                       BigDecimal uiAfter,
                                       BigDecimal apiBefore,
                                       BigDecimal apiAfter) {
        assertUiDecreased(action, uiBefore, uiAfter);
        assertApiDecreased(action, apiBefore, apiAfter);
    }

    public void assertBalanceIncreased(String action,
                                       BigDecimal uiBefore,
                                       BigDecimal uiAfter,
                                       BigDecimal apiBefore,
                                       BigDecimal apiAfter) {
        assertUiIncreased(action, uiBefore, uiAfter);
        assertApiIncreased(action, apiBefore, apiAfter);
    }

    public void assertDecreased(String action, BigDecimal before, BigDecimal after) {
        assertUiDecreased(action, before, after);
    }

    public void assertIncreased(String action, BigDecimal before, BigDecimal after) {
        assertUiIncreased(action, before, after);
    }

    private void assertUiDecreased(String action, BigDecimal before, BigDecimal after) {
        FlowLogger.step(flowName, action + " UI wallet delta: " + after.subtract(before).toPlainString() + " EGP");
        Assert.assertTrue(
                after.compareTo(before) < 0,
                action + " should decrease UI wallet balance. before=" + before.toPlainString() + ", after=" + after.toPlainString()
        );
        System.out.println("[ASSERT] UI decreased OK for " + action);
    }

    private void assertApiDecreased(String action, BigDecimal before, BigDecimal after) {
        FlowLogger.step(flowName, action + " API wallet delta: " + after.subtract(before).toPlainString() + " EGP");
        Assert.assertTrue(
                after.compareTo(before) < 0,
                action + " should decrease API wallet balance. before=" + before.toPlainString() + ", after=" + after.toPlainString()
        );
        System.out.println("[ASSERT] API decreased OK for " + action);
    }

    private void assertUiIncreased(String action, BigDecimal before, BigDecimal after) {
        FlowLogger.step(flowName, action + " UI wallet delta: +" + after.subtract(before).toPlainString() + " EGP");
        Assert.assertTrue(
                after.compareTo(before) > 0,
                action + " should increase UI wallet balance. before=" + before.toPlainString() + ", after=" + after.toPlainString()
        );
        System.out.println("[ASSERT] UI increased OK for " + action);
    }

    private void assertApiIncreased(String action, BigDecimal before, BigDecimal after) {
        FlowLogger.step(flowName, action + " API wallet delta: +" + after.subtract(before).toPlainString() + " EGP");
        Assert.assertTrue(
                after.compareTo(before) > 0,
                action + " should increase API wallet balance. before=" + before.toPlainString() + ", after=" + after.toPlainString()
        );
        System.out.println("[ASSERT] API increased OK for " + action);
    }
}
