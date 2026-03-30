package core.driver;

import TestObject.DriverManager;
import TestObject.FlowLogger;
import core.config.ConfigManager;
import io.appium.java_client.android.options.UiAutomator2Options;

public final class AndroidCapabilitiesBuilder {
    private AndroidCapabilitiesBuilder() {
    }

    public static UiAutomator2Options build(DriverManager.SessionConfig sessionConfig) {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(ConfigManager.getPlatformName());
        options.setCapability("udid", sessionConfig.udid());
        options.setCapability("deviceName", sessionConfig.deviceName());
        options.setAutomationName(ConfigManager.getRequired("automationName"));
        options.setAppPackage(ConfigManager.getAppPackage());
        options.setAppActivity(ConfigManager.getAppActivity());

        if (ConfigManager.getInt("useAppBinary", 0) == 1) {
            FlowLogger.step("DRIVER", "Warning: APK installation is enabled via useAppBinary=1. This will slow session startup.");
            options.setApp(ConfigManager.getRequired("appPath"));
        }

        options.autoGrantPermissions();
        options.setCapability("newCommandTimeout", 300);
        options.setCapability("uiautomator2ServerInstallTimeout", ConfigManager.getInt("uiautomator2ServerInstallTimeoutMs", 120000));
        options.setCapability("uiautomator2ServerLaunchTimeout", ConfigManager.getInt("uiautomator2ServerLaunchTimeoutMs", 60000));
        options.setCapability("androidInstallTimeout", ConfigManager.getInt("androidInstallTimeoutMs", 120000));
        options.setCapability("adbExecTimeout", sessionConfig.adbExecTimeoutMs());
        options.setCapability("noReset", true);
        options.setCapability("fullReset", false);
        options.setCapability("dontStopAppOnReset", true);
        options.setCapability("skipDeviceInitialization", true);
        options.setCapability("skipServerInstallation", ConfigManager.getBoolean("skipServerInstallation", false));
        options.setCapability("disableWindowAnimation", true);
        options.setCapability("ignoreHiddenApiPolicyError", true);
        options.setCapability("systemPort", sessionConfig.systemPort());

        String remoteAdbHost = ConfigManager.getOptional("remoteAdbHost", "");
        if (!remoteAdbHost.isBlank()) {
            options.setCapability("remoteAdbHost", remoteAdbHost);
        }

        return options;
    }
}
