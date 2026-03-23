package TestObject;

import PageObject.P02_LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class T02_LoginTest extends BaseTest {

    //@Test
    public void userCanLoginSuccessfully() {
        P02_LoginPage login = new P02_LoginPage();

        new StartupFlow().completeStartupToLogin();

        login.login("01282349004", "@Testing08");
        login.handleSecurityQuestion();
        login.enterPinZeroFourTimes();
        login.waitForHomeScreen();

        Assert.assertTrue(
                login.isHomeDisplayed(),
                "Home screen was NOT displayed"
        );
    }
}
