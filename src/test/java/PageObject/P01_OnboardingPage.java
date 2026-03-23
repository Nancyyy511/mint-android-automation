package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class P01_OnboardingPage extends BasePage {
    public enum OnboardingState {
        ONBOARDING_DONE,
        LOCATION_REQUIRED,
        NOT_VISIBLE
    }

    private final By nextButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Next\")");

    private final By letsGetTradingButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"get trading\")");

    private final By enableLocationButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Enable Location\")");

    private final By loginOption =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Log in\")");

    public boolean isOnboardingVisible() {
        return isAnyVisibleQuick(5, nextButton, letsGetTradingButton, enableLocationButton, loginOption);
    }

    public OnboardingState completeOnboarding() {
        System.out.println("Onboarding started");

        if (!isAnyVisibleQuick(8, nextButton, letsGetTradingButton, enableLocationButton, loginOption)) {
            System.out.println("Onboarding screen not visible");
            return OnboardingState.NOT_VISIBLE;
        }

        if (clickIfVisible(loginOption, 3)) {
            System.out.println("Login option clicked directly from onboarding");
            return OnboardingState.ONBOARDING_DONE;
        }

        int maxSwipes = 5;

        for (int i = 0; i < maxSwipes; i++) {
            if (isVisibleQuick(enableLocationButton, 1)) {
                System.out.println("Enable Location found during onboarding. Stopping swipes.");
                return OnboardingState.LOCATION_REQUIRED;
            }

            if (isVisibleQuick(letsGetTradingButton, 2)) {
                System.out.println("Let's get trading found during onboarding");
                break;
            }

            if (!isVisibleQuick(nextButton, 2)) {
                System.out.println("Onboarding navigation not ready yet. Re-checking current screen.");
                continue;
            }

            System.out.println("Onboarding swipe attempt " + (i + 1));
            safeSwipeLeft();
        }

        if (isVisibleQuick(enableLocationButton, 3)) {
            System.out.println("Enable Location displayed after onboarding swipes");
            return OnboardingState.LOCATION_REQUIRED;
        }

        if (!isVisibleQuick(letsGetTradingButton, 4)) {
            throw new RuntimeException("Neither Let's get trading nor Enable Location appeared after onboarding swipes");
        }

        try {
            waitForSeconds(5)
                    .until(ExpectedConditions.elementToBeClickable(letsGetTradingButton))
                    .click();
        } catch (Exception e) {
            tap(letsGetTradingButton);
        }
        System.out.println("Let's get trading clicked");

        if (isVisibleQuick(enableLocationButton, 5)) {
            System.out.println("Enable Location appeared after Let's get trading");
            return OnboardingState.LOCATION_REQUIRED;
        }

        if (clickIfVisible(loginOption, 5)) {
            System.out.println("Login option clicked after onboarding");
        } else {
            System.out.println("Login option not visible yet after Let's get trading. Handing control back to StartupFlow.");
        }

        System.out.println("Onboarding completed successfully");
        return OnboardingState.ONBOARDING_DONE;
    }

    public void completeOnboardingAndGoToLogin() {
        OnboardingState state = completeOnboarding();
        if (state == OnboardingState.LOCATION_REQUIRED) {
            return;
        }
        if (state == OnboardingState.NOT_VISIBLE) {
            throw new RuntimeException("Onboarding screen was not visible");
        }
    }
}
