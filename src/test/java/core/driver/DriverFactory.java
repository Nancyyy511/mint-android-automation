package core.driver;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class DriverFactory {
    private static final ThreadLocal<AndroidDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> WAIT = new ThreadLocal<>();

    private DriverFactory() {
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

    public static void setWait(WebDriverWait wait) {
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
            clear();
        }
    }

    public static void clear() {
        DRIVER.remove();
        WAIT.remove();
    }
}
