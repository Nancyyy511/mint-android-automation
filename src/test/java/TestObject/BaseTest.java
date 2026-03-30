package TestObject;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.retry.RetryAnnotationTransformer;

@Listeners({TestListener.class, RetryAnnotationTransformer.class})
public class BaseTest {

    @BeforeMethod(alwaysRun = true)
    @Parameters({"deviceName", "udid", "systemPort"})
    public void setUp(@Optional String deviceName,
                      @Optional String udid,
                      @Optional String systemPort) throws Exception {
        Integer resolvedSystemPort = parseSystemPort(systemPort);
        DriverManager.configureSession(deviceName, udid, resolvedSystemPort);
        DriverManager.initializeDriver();
        DriverManager.SessionConfig sessionConfig = DriverManager.getSessionConfig();
        FlowLogger.step("BASE_TEST", "App launched successfully on "
                + sessionConfig.deviceName() + " (" + sessionConfig.udid() + "), systemPort="
                + sessionConfig.systemPort());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
        FlowLogger.step("BASE_TEST", "App closed");
    }

    private Integer parseSystemPort(String systemPort) {
        if (systemPort == null || systemPort.isBlank()) {
            return null;
        }
        return Integer.parseInt(systemPort.trim());
    }
}
