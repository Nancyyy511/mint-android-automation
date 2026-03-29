package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.testng.Assert;

public class P12_TradeValidationPage extends BasePage {
    private final By reviewOrderButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Review Order\")");

    private final By submitButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Submit\")");

    private final By retryButton =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*(retry|try again).*\")");

    private final By genericErrorText =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textMatches(\"(?i).*(error|invalid|minimum|insufficient|rejected|reject|failed|timeout|try again|not enough|quantity|price).*\")"
            );

    private final By toastMessage =
            AppiumBy.xpath("//android.widget.Toast[1]");

    public boolean isReviewOrderEnabled() {
        return isActionEnabled(reviewOrderButton);
    }

    public boolean isSubmitEnabled() {
        return isActionEnabled(submitButton);
    }

    public boolean isRetryVisible() {
        return isVisibleQuick(retryButton, 3);
    }

    public boolean isErrorVisible() {
        return isAnyVisibleQuick(3, genericErrorText, toastMessage);
    }

    public String getVisibleErrorMessage() {
        if (isVisibleQuick(genericErrorText, 3)) {
            return getText(genericErrorText);
        }
        if (isVisibleQuick(toastMessage, 2)) {
            return getText(toastMessage);
        }
        return "";
    }

    public void assertDisabledActionOrErrorVisible(String scenario) {
        boolean actionDisabled = !isReviewOrderEnabled() && !isSubmitEnabled();
        boolean errorVisible = isErrorVisible();
        Assert.assertTrue(
                actionDisabled || errorVisible,
                scenario + " should show an error or keep the action disabled"
        );
    }

    private boolean isActionEnabled(By locator) {
        try {
            String enabled = waitForSeconds(2).until(driver ->
                    driver.findElements(locator).isEmpty() ? null : driver.findElements(locator).get(0).getAttribute("enabled")
            );
            return "true".equalsIgnoreCase(enabled);
        } catch (Exception ignored) {
            return false;
        }
    }
}
