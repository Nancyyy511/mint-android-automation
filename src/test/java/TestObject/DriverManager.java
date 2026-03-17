package TestObject;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public final class DriverManager {

    private static final String APP_PATH = "C:\\Users\\nawny\\Downloads\\Mint_production_2.1.3.apk";

    private static AndroidDriver driver;
    private static WebDriverWait wait;

    private DriverManager() {
    }

    public static void initializeDriver() throws MalformedURLException {
        if (driver != null) {
            return;
        }

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(ConfigReader.getProperty("platformName"));
        options.setCapability("udid", ConfigReader.getProperty("udid"));
        options.setCapability("deviceName", ConfigReader.getProperty("deviceName"));
        options.setAutomationName("UiAutomator2");
        options.setAppPackage(ConfigReader.getProperty("appPackage"));
        options.setAppActivity(ConfigReader.getProperty("appActivity"));
        options.setApp(APP_PATH);
        options.autoGrantPermissions();
        options.setCapability("unicodeKeyboard", false);
        options.setCapability("resetKeyboard", true);
        options.setCapability("newCommandTimeout", 300);
        options.setCapability("uiautomator2ServerInstallTimeout", 60000);
        options.setCapability("uiautomator2ServerLaunchTimeout", 60000);
        options.setCapability("adbExecTimeout", 60000);
        options.setCapability("autoAcceptAlerts", true);
        options.setCapability("disableWindowAnimation", true);
        options.setCapability("ignoreHiddenApiPolicyError", true);

        driver = new AndroidDriver(new URL(ConfigReader.getProperty("appiumServer")), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(6));
    }

    public static AndroidDriver getDriver() {
        return driver;
    }

    public static WebDriverWait getWait() {
        return wait;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            wait = null;
        }
    }
}
