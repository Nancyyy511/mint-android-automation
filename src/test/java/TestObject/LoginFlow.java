package TestObject;

import PageObject.P02_LoginPage;
import org.testng.Assert;

public class LoginFlow {
    private final StartupFlow startupFlow = new StartupFlow();
    private final P02_LoginPage loginPage = new P02_LoginPage();

    public void loginToHome() {
        String username = System.getProperty("login.username", "01282349004");
        String password = System.getProperty("login.password", "@Testing09");
        loginToHome(username, password);
    }

    public void loginToHome(String username, String password) {
        FlowLogger.step("LOGIN_FLOW", "Starting startup/login flow");
        startupFlow.completeStartupToLogin();
        loginFromCurrentScreen(username, password);
    }

    public void loginFromCurrentScreen(String username, String password) {
        if (loginPage.isHomeDisplayed()) {
            FlowLogger.step("LOGIN_FLOW", "User already on home screen");
            return;
        }

        loginPage.waitForLoginScreen();
        loginPage.login(username, password);
        loginPage.handleSecurityQuestion();
        loginPage.enterPinZeroFourTimes();
        loginPage.waitForHomeScreen();

        Assert.assertTrue(loginPage.isHomeDisplayed(), "Expected home screen after login flow");
        FlowLogger.step("LOGIN_FLOW", "Login flow completed successfully");
    }
}
