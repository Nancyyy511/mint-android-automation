package TestObject;

import io.appium.java_client.android.AndroidDriver;

import java.util.List;
import java.util.Map;

public class DeviceControl {
    private final AndroidDriver driver;

    public DeviceControl() {
        this.driver = DriverManager.getDriver();
        if (this.driver == null) {
            throw new IllegalStateException("Android driver is not initialized for device control");
        }
    }

    public void disableNetwork() {
        FlowLogger.step("DEVICE", "Disabling Wi-Fi and mobile data");
        runShell("svc", "wifi", "disable");
        runShell("svc", "data", "disable");
    }

    public void enableNetwork() {
        FlowLogger.step("DEVICE", "Enabling Wi-Fi and mobile data");
        runShell("svc", "wifi", "enable");
        runShell("svc", "data", "enable");
    }

    public boolean isAppStable(String expectedPackage) {
        try {
            return expectedPackage.equalsIgnoreCase(driver.getCurrentPackage());
        } catch (Exception exception) {
            return false;
        }
    }

    private void runShell(String command, String... args) {
        driver.executeScript("mobile: shell", Map.of(
                "command", command,
                "args", List.of(args)
        ));
    }
}
