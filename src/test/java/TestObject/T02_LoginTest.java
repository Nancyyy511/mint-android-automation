package TestObject;

import PageObject.P01_OnboardingPage;
import PageObject.P02_LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class T02_LoginTest extends BaseTest {

    //@Test
    public void userCanLoginSuccessfully() {

        P01_OnboardingPage onboarding = new P01_OnboardingPage();
        P02_LoginPage login = new P02_LoginPage();

        onboarding.completeOnboardingAndGoToLogin();

        login.login("01282349004", "@Testing09");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();

        Assert.assertTrue(
                login.isHomeDisplayed(),
                "Home screen was NOT displayed"
        );
    }
}
