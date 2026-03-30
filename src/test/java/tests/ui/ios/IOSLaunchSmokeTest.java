package tests.ui.ios;

import core.config.PlatformContext;
import core.driver.UnifiedDriverFactory;
import io.appium.java_client.AppiumDriver;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.CrossPlatformReportingListener;

import java.lang.reflect.Method;

@Listeners(CrossPlatformReportingListener.class)
public class IOSLaunchSmokeTest {
    private AppiumDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) throws Exception {
        PlatformContext.setPlatform("ios");
        driver = UnifiedDriverFactory.initializeDriver(method);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        UnifiedDriverFactory.quitDriver();
        PlatformContext.clear();
    }

    @Test(groups = "ios")
    public void appLaunchesSuccessfullyOnIos() {
        Assert.assertNotNull(driver, "iOS driver should be initialized");
        Assert.assertNotNull(driver.getSessionId(), "iOS session should be created successfully");

        String pageSource = driver.getPageSource();
        Assert.assertNotNull(pageSource, "Page source should be available after app launch");
        Assert.assertFalse(pageSource.isBlank(), "Page source should not be blank after app launch");
    }
}
