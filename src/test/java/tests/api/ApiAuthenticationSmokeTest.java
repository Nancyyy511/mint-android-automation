package tests.api;

import api.client.AuthApi;
import api.models.ApiEnvelope;
import api.models.LoginData;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.CrossPlatformReportingListener;

@Listeners(CrossPlatformReportingListener.class)
public class ApiAuthenticationSmokeTest {

    @Test(groups = "api")
    public void loginApiReturnsGuestToken() {
        ApiEnvelope<LoginData> envelope = new AuthApi().login();

        Assert.assertNotNull(envelope, "API envelope should not be null");
        Assert.assertTrue(envelope.isStatus(), "Login API should return status=true");
        Assert.assertNotNull(envelope.getData(), "Login data should not be null");
        Assert.assertNotNull(envelope.getData().getGuestToken(), "Guest token should not be null");
        Assert.assertFalse(envelope.getData().getGuestToken().isBlank(), "Guest token should not be blank");
    }
}
