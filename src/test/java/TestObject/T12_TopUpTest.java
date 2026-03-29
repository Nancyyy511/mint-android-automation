package TestObject;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class T12_TopUpTest extends BaseTest {

    @Test
    public void userCanCompleteTopUpSuccessfully() {
        new LoginFlow().loginToHome(
                System.getProperty("topup.username", "01282349004"),
                System.getProperty("topup.password", "@Testing08")
        );

        WalletBalanceValidator walletBalanceValidator = new WalletBalanceValidator("T12");
        BigDecimal balanceBeforeTopUp = walletBalanceValidator.captureBalance("Before Top Up");

        TopUpFlow topUpFlow = new TopUpFlow();
        topUpFlow.completeTopUpFromCurrentSession();

        BigDecimal balanceAfterTopUp = walletBalanceValidator.captureBalance("After Top Up");
        walletBalanceValidator.assertIncreased("Top Up", balanceBeforeTopUp, balanceAfterTopUp);

        Assert.assertEquals(topUpFlow.getState(), TopUpFlow.TopUpState.SUCCESS, "Top Up state was not SUCCESS");
        Assert.assertTrue(topUpFlow.isSuccessScreenDisplayed(), "Payment Successful screen was not displayed");
        Assert.assertTrue(topUpFlow.isReturnedHome(), "User was not returned to home after top up");
    }
}
