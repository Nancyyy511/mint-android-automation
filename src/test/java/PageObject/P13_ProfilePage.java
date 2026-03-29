package PageObject;

import TestObject.FlowLogger;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class P13_ProfilePage extends BasePage {
    private static final double PROFILE_AVATAR_X_PERCENT = 0.08;
    private static final double PROFILE_AVATAR_Y_PERCENT = 0.08;

    private final By profileAvatar =
            AppiumBy.className("android.widget.ImageView");

    private final By profileScreenMarker =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*(help\\s*&\\s*support|profile\\s*settings|logout).*\")");

    private final By logoutButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Logout\")");

    private final By logoutButtonAlt =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Logout\")");

    public void openProfileFromHome() {
        FlowLogger.step("SESSION", "Opening profile screen from Home");

        if (isProfileScreenDisplayed()) {
            return;
        }

        try {
            WebElement avatar = waitForSeconds(5).until(driver ->
                    driver.findElements(profileAvatar).stream()
                            .filter(WebElement::isDisplayed)
                            .findFirst()
                            .orElse(null)
            );
            clickElementReliably(avatar, "Home profile avatar");
        } catch (Exception exception) {
            tapByScreenPercentage(PROFILE_AVATAR_X_PERCENT, PROFILE_AVATAR_Y_PERCENT);
        }

        waitForSeconds(10).until(driver -> isProfileScreenDisplayed());
    }

    public boolean isProfileScreenDisplayed() {
        return isVisibleQuick(profileScreenMarker, 4);
    }

    public void logout() {
        FlowLogger.step("SESSION", "Scrolling profile screen to locate Logout");

        for (int attempt = 1; attempt <= 6; attempt++) {
            if (clickFirstVisible(2, logoutButton, logoutButtonAlt)) {
                return;
            }
            scrollDown();
            pause(500);
        }

        throw new RuntimeException("Logout button was not found on the profile screen");
    }
}
