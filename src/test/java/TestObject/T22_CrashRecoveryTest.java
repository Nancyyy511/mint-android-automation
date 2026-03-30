package TestObject;

import PageObject.P02_LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class T22_CrashRecoveryTest extends BaseTest {
    private final P02_LoginPage loginPage = new P02_LoginPage();

    @Test
    public void appCanRecoverAfterIntentionalCrash() {
        if (!CrashUtils.isCrashTestingEnabled()) {
            throw new SkipException("Crash testing is disabled. Enable with -DcrashTestingEnabled=true");
        }

        String username = System.getProperty("login.username", "01282349004");
        String password = System.getProperty("login.password", "@Testing08");

        new LoginFlow().loginToHome(username, password);
        Assert.assertTrue(loginPage.isHomeDisplayed(), "User should be on Home before crash trigger");

        CrashUtils.triggerCrash(DriverManager.getDriver(), CrashUtils.CrashMode.AM_CRASH);
        Assert.assertTrue(
                CrashUtils.didAppCrash(DriverManager.getDriver()) || CrashUtils.isDriverSessionLost(),
                "App crash should be detected by package change or lost session"
        );

        CrashUtils.relaunchAppAfterCrash();

        if (loginPage.isLoginScreenDisplayed()) {
            new LoginFlow().loginFromCurrentScreen(username, password);
        }

        Assert.assertTrue(loginPage.isHomeDisplayed(), "App should relaunch and recover to a usable state");
    }
}
