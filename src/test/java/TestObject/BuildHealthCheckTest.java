package TestObject;

import PageObject.P02_LoginPage;
import PageObject.P11_VerifyPinPage;
import api.utils.RetryUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BuildHealthCheckTest extends BaseTest {
    private static final String TEST_NAME = "BuildHealthCheckTest";

    private final StartupFlow startupFlow = new StartupFlow();
    private final P02_LoginPage loginPage = new P02_LoginPage();
    private final P11_VerifyPinPage verifyPinPage = new P11_VerifyPinPage();
    private final HealthCheckService healthCheckService = new HealthCheckService(TEST_NAME);

    @Test
    public void buildHealthCheckTest() {
        int minimumOrders = Integer.parseInt(System.getProperty("healthCheck.minOrders", "2"));
        int maximumOrders = Integer.parseInt(System.getProperty("healthCheck.maxOrders", "8"));
        int maxPasses = Integer.parseInt(System.getProperty("healthCheck.maxPasses", "4"));
        boolean enableRecovery = Boolean.parseBoolean(System.getProperty("healthCheck.enableRecovery", "true"));
        boolean triggerCrash = Boolean.parseBoolean(System.getProperty("healthCheck.triggerCrash", "false"));
        String username = System.getProperty("login.username", "01282349004");
        String password = System.getProperty("login.password", "@Testing08");

        OrderInspectionService orderInspectionService = new OrderInspectionService(healthCheckService, enableRecovery);

        healthCheckService.log("INFO", "HEALTH_CHECK", "test-start", null,
                "Starting build health check. minOrders=" + minimumOrders
                        + ", maxOrders=" + maximumOrders
                        + ", maxPasses=" + maxPasses
                        + ", enableRecovery=" + enableRecovery
                        + ", triggerCrash=" + triggerCrash
                        + ", udid=" + DriverManager.getSessionConfig().udid());

        healthCheckService.validateAppStability("after app launch", null);

        healthCheckService.log("INFO", "STEP", "startup-flow", null, "Completing startup flow");
        RetryUtils.retry(() -> {
            startupFlow.completeStartupToLogin();
            return true;
        }, 2, 1000);
        healthCheckService.validateAppStability("after startup flow", null);

        AppState startupState = healthCheckService.detectCurrentAppState();
        Assert.assertTrue(startupState == AppState.LOGIN || startupState == AppState.UNKNOWN,
                "Expected login-oriented startup state but found " + startupState);

        performLogin(username, password);
        healthCheckService.markLoginCompleted(true);
        healthCheckService.validateAppStability("after login", null);
        healthCheckService.ensureHomeState();

        if (triggerCrash && CrashUtils.isCrashTestingEnabled()) {
            healthCheckService.runCrashRecoveryCheck(username, password);
        }

        int validatedOrders = orderInspectionService.inspectOrders(minimumOrders, maximumOrders, maxPasses);
        Assert.assertTrue(validatedOrders >= minimumOrders,
                "Expected to validate at least " + minimumOrders + " stable orders, but validated " + validatedOrders);

        healthCheckService.log("INFO", "HEALTH_CHECK", "test-complete", null,
                "Build health check completed successfully. validatedOrders=" + validatedOrders);
    }

    private void performLogin(String username, String password) {
        healthCheckService.log("INFO", "STEP", "login", null, "Performing login");
        RetryUtils.retry(() -> {
            loginPage.waitForLoginScreen();
            loginPage.login(username, password);

            AppState state = healthCheckService.detectCurrentAppState();
            Assert.assertTrue(
                    state == AppState.HOME || state == AppState.PIN || state == AppState.SECURITY_QUESTION,
                    "Expected post-login state to be HOME/PIN/SECURITY_QUESTION but found " + state
            );

            loginPage.handleSecurityQuestion();
            loginPage.enterPinZeroFourTimes();
            verifyPinPage.handleVerifyPinIfPresent();
            loginPage.waitForHomeScreen();
            return true;
        }, 2, 1000);

        healthCheckService.log("INFO", "HEALTH_CHECK", "login-complete", null, "Login completed successfully");
    }
}
