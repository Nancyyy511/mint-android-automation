package TestObject;

import org.testng.Assert;
import org.testng.annotations.Test;

public class T12_TopUpTest extends BaseTest {

    @Test
    public void userCanCompleteTopUpSuccessfully() {
        TopUpFlow topUpFlow = new TopUpFlow();
        topUpFlow.completeTopUpFlow();

        Assert.assertEquals(topUpFlow.getState(), TopUpFlow.TopUpState.SUCCESS, "Top Up state was not SUCCESS");
        Assert.assertTrue(topUpFlow.isSuccessScreenDisplayed(), "Payment Successful screen was not displayed");
        Assert.assertTrue(topUpFlow.isReturnedHome(), "User was not returned to home after top up");
    }
}
