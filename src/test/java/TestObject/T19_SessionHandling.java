package TestObject;

import PageObject.P02_LoginPage;
import PageObject.P11_VerifyPinPage;
import PageObject.P13_ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class T19_SessionHandling extends BaseTest {
    private final P13_ProfilePage profilePage = new P13_ProfilePage();
    private final P02_LoginPage loginPage = new P02_LoginPage();
    private final P11_VerifyPinPage verifyPinPage = new P11_VerifyPinPage();

    @Test
    public void userCanLogoutAndLoginAgain() {
        String username = System.getProperty("login.username", "01282349004");
        String password = System.getProperty("login.password", "@Testing08");

        new LoginFlow().loginToHome(username, password);
        profilePage.openProfileFromHome();
        profilePage.logout();

        Assert.assertTrue(loginPage.isLoginScreenDisplayed(), "User should be redirected to login screen after logout");

        new LoginFlow().loginFromCurrentScreen(username, password);
        Assert.assertTrue(loginPage.isHomeDisplayed(), "User should return to Home after re-login");
    }

    @Test
    public void sessionExpirationAfterInactivityShouldBeRecoverable() {
        String username = System.getProperty("login.username", "01282349004");
        String password = System.getProperty("login.password", "@Testing08");

        new LoginFlow().loginToHome(username, password);

        int inactivitySeconds = Integer.parseInt(System.getProperty("session.inactivitySeconds", "30"));
        DriverManager.getDriver().runAppInBackground(Duration.ofSeconds(inactivitySeconds));

        verifyPinPage.handleVerifyPinIfPresent();
        if (loginPage.isLoginScreenDisplayed()) {
            new LoginFlow().loginFromCurrentScreen(username, password);
        }

        Assert.assertTrue(
                loginPage.isHomeDisplayed() || loginPage.isLoginScreenDisplayed(),
                "After inactivity, app should be on login or recoverable home state"
        );

        if (loginPage.isLoginScreenDisplayed()) {
            new LoginFlow().loginFromCurrentScreen(username, password);
        }

        Assert.assertTrue(loginPage.isHomeDisplayed(), "Session should be restorable after inactivity");
    }
}
