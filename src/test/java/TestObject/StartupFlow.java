package TestObject;

import PageObject.P00_LocationPermissionPage;
import PageObject.P01_OnboardingPage;
import PageObject.P02_LoginPage;
import PageObject.P01_OnboardingPage.OnboardingState;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class StartupFlow {

    private final P01_OnboardingPage onboardingPage = new P01_OnboardingPage();
    private final P00_LocationPermissionPage locationPermissionPage = new P00_LocationPermissionPage();
    private final P02_LoginPage loginPage = new P02_LoginPage();

    public void completeStartupToLogin() {
        System.out.println("Startup flow started");

        int maxCycles = 8;

        for (int cycle = 1; cycle <= maxCycles; cycle++) {
            boolean loginOptionVisible = loginPage.isLoginOptionVisible();
            boolean loginFormVisible = loginPage.isCredentialsFormVisible();
            boolean loginVisible = loginOptionVisible || loginFormVisible;
            boolean permissionPopupVisible = locationPermissionPage.isPermissionPopupVisible();
            boolean enableLocationVisible = locationPermissionPage.isEnableLocationVisible();
            boolean onboardingVisible = onboardingPage.isOnboardingVisible();

            System.out.println(
                    "Startup cycle " + cycle
                            + " visibility: onboarding=" + onboardingVisible
                            + ", enableLocation=" + enableLocationVisible
                            + ", permissionPopup=" + permissionPopupVisible
                            + ", loginOption=" + loginOptionVisible
                            + ", loginForm=" + loginFormVisible
            );

            if (loginFormVisible) {
                loginPage.waitForLoginScreen();
                return;
            }

            if (loginOptionVisible) {
                System.out.println("Auth choice screen detected. Clicking Log in.");
                loginPage.openLoginFormIfVisible();
                waitBetweenCycles();
                continue;
            }

            if (permissionPopupVisible) {
                System.out.println("Location permission popup detected");
                locationPermissionPage.allowLocationPermission();
                waitBetweenCycles();
                continue;
            }

            if (enableLocationVisible) {
                System.out.println("Enable Location detected");
                locationPermissionPage.clickEnableLocationIfVisible();
                locationPermissionPage.allowLocationPermission();
                locationPermissionPage.skipLocationIfStillVisible();
                waitBetweenCycles();
                continue;
            }

            if (onboardingVisible) {
                OnboardingState state = onboardingPage.completeOnboarding();
                if (state == OnboardingState.LOCATION_REQUIRED) {
                    System.out.println("Onboarding requires location step");
                    locationPermissionPage.clickEnableLocationIfVisible();
                    locationPermissionPage.allowLocationPermission();
                    locationPermissionPage.skipLocationIfStillVisible();
                }
                waitBetweenCycles();
                continue;
            }

            System.out.println("Startup cycle " + cycle + ": no known startup screen detected. App may still be on splash/loading.");
            waitBetweenCycles();
        }

        if (loginPage.isCredentialsFormVisible()) {
            loginPage.waitForLoginScreen();
            return;
        }

        throw new RuntimeException("Startup flow did not reach login screen");
    }

    private void waitBetweenCycles() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(3))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(driver ->
                            onboardingPage.isOnboardingVisible()
                                    || locationPermissionPage.isEnableLocationVisible()
                                    || locationPermissionPage.isPermissionPopupVisible()
                                    || loginPage.isLoginOptionVisible()
                                    || loginPage.isCredentialsFormVisible()
                    );
        } catch (Exception ignored) {
            // Keep cycling; transient loading states are expected during startup.
        }
    }
}
