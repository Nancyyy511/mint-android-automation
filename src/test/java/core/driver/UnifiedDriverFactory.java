package core.driver;

import TestObject.ConfigReader;
import TestObject.DriverManager;
import core.config.PlatformContext;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class UnifiedDriverFactory {
    private static final ThreadLocal<IOSDriver> IOS_DRIVER = new ThreadLocal<>();

    private UnifiedDriverFactory() {
    }

    public static AppiumDriver getDriver() {
        return isIos() ? IOS_DRIVER.get() : DriverManager.getDriver();
    }

    public static AppiumDriver initializeDriver(Method method) throws MalformedURLException {
        if (isIos()) {
            return createIosDriver(method == null ? "iOS Session" : method.getName());
        }
        return DriverManager.getDriver();
    }

    public static IOSDriver createIosDriver(String sessionName) throws MalformedURLException {
        IOSDriver existingDriver = IOS_DRIVER.get();
        if (existingDriver != null) {
            return existingDriver;
        }

        String userName = getRequired("browserstack.user", "BROWSERSTACK_USERNAME");
        String accessKey = getRequired("browserstack.key", "BROWSERSTACK_ACCESS_KEY");
        String appId = getRequired("browserstack.app", "BROWSERSTACK_APP_ID");
        String deviceName = getOptional("ios.deviceName", "BROWSERSTACK_DEVICE", "iPhone 13");
        String platformVersion = getOptional("ios.platformVersion", "BROWSERSTACK_OS_VERSION", "17");
        String projectName = getOptional("browserstack.project", "BROWSERSTACK_PROJECT", "Mint iOS");
        String buildName = getOptional("browserstack.build", "BROWSERSTACK_BUILD", "Unified iOS Build");
        String hubUrlValue = getOptional("browserstack.hubUrl", "BROWSERSTACK_HUB_URL",
                "https://hub-cloud.browserstack.com/wd/hub");

        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");
        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        options.setApp(appId);
        options.setCapability("autoAcceptAlerts", true);
        options.setCapability("noReset", false);

        Map<String, Object> browserStackOptions = new HashMap<>();
        browserStackOptions.put("userName", userName);
        browserStackOptions.put("accessKey", accessKey);
        browserStackOptions.put("projectName", projectName);
        browserStackOptions.put("buildName", buildName);
        browserStackOptions.put("sessionName", sessionName);
        options.setCapability("bstack:options", browserStackOptions);

        IOSDriver driver = new IOSDriver(new URL(hubUrlValue), options);
        IOS_DRIVER.set(driver);
        return driver;
    }

    public static void quitDriver() {
        if (!isIos()) {
            return;
        }

        IOSDriver driver = IOS_DRIVER.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            IOS_DRIVER.remove();
        }
    }

    private static boolean isIos() {
        return "ios".equalsIgnoreCase(PlatformContext.getPlatformOrDefault());
    }

    private static String getRequired(String propertyKey, String envKey) {
        String value = getOptional(propertyKey, envKey, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required iOS configuration value. property="
                    + propertyKey + ", env=" + envKey);
        }
        return value;
    }

    private static String getOptional(String propertyKey, String envKey, String defaultValue) {
        String fromProperty = System.getProperty(propertyKey);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty.trim();
        }

        String fromConfig = ConfigReader.getOptional(propertyKey, "");
        if (!fromConfig.isBlank()) {
            return fromConfig.trim();
        }

        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }

        return defaultValue;
    }
}
