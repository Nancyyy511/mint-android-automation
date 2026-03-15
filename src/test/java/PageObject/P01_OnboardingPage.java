package PageObject;

import TestObject.Core;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;

public class P01_OnboardingPage extends Core {
    // locators
    private final By nextButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Next\")");

    private final By letsGetTradingButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"get trading\")");

    private final By loginOption =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Log in\")");

    // Actions
    public void completeOnboardingAndGoToLogin() {

        int maxSwipes = 4;

        for (int i = 0; i < maxSwipes; i++) {

            if (isVisibleQuick(letsGetTradingButton, 2)) {
                break;
            }

            safeSwipeLeft();

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }

        if (!isVisibleQuick(letsGetTradingButton, 3)) {
            throw new RuntimeException("Let's get trading not visible after swipes");
        }

        // Click Let's get trading (fast)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(letsGetTradingButton))
                    .click();
        } catch (Exception e) {
            tap(letsGetTradingButton);
        }

        // Login option
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(loginOption))
                    .click();
        } catch (Exception e) {
            tap(loginOption);
        }

        System.out.println("Onboarding completed successfully");
    }
}