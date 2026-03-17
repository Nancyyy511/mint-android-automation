package TestObject;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public final class DriverManager {

    private static final ThreadLocal<AndroidDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> WAIT = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void initializeDriver() throws MalformedURLException {
        if (getDriver() != null) {
            return;
        }

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(ConfigReader.get("platformName"));
        options.setCapability("udid", ConfigReader.get("udid"));
        options.setCapability("deviceName", ConfigReader.get("deviceName"));
        options.setAutomationName(ConfigReader.get("automationName"));
        options.setAppPackage(ConfigReader.get("appPackage"));
        options.setAppActivity(ConfigReader.get("appActivity"));
        options.setApp(ConfigReader.get("appPath"));
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

        AndroidDriver driver = new AndroidDriver(new URL(ConfigReader.get("appiumServer")), options);
        setDriver(driver);
        setWait(new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicitWaitSeconds", 6))));
    }

    public static AndroidDriver getDriver() {
        return DRIVER.get();
    }

    public static void setDriver(AndroidDriver driver) {
        if (driver == null) {
            DRIVER.remove();
            return;
        }
        DRIVER.set(driver);
    }

    public static WebDriverWait getWait() {
        return WAIT.get();
    }

    static void setWait(WebDriverWait wait) {
        if (wait == null) {
            WAIT.remove();
            return;
        }
        WAIT.set(wait);
    }

    public static void quitDriver() {
        AndroidDriver driver = getDriver();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            DRIVER.remove();
            WAIT.remove();
        }
    }
}
