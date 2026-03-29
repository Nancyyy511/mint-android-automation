package PageObject;

import TestObject.FlowLogger;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Comparator;
import java.util.List;

public class P11_VerifyPinPage extends BasePage {
    private final By verifyPinTitle =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*verify\\s*pin.*\")");

    private final By verifyPinTitleAlt =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*enter\\s*pin.*\")");

    private final By pinDigitButtons =
            AppiumBy.className("android.widget.TextView");

    private final By verifyPinSubmitButton =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*(verify|continue|submit|confirm).*\")");

    public boolean isVerifyPinScreenDisplayed() {
        boolean titleVisible = isAnyVisibleQuick(2, verifyPinTitle, verifyPinTitleAlt);
        boolean keypadVisible = isVisibleQuick(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"0\")"),
                2
        );
        return titleVisible && keypadVisible;
    }

    public boolean handleVerifyPinIfPresent() {
        if (!isVerifyPinScreenDisplayed()) {
            return false;
        }

        String pin = System.getProperty("user.pin", "0000");
        FlowLogger.step("PIN_GUARD", "Verify PIN screen detected. Entering configured PIN.");

        for (char digit : pin.toCharArray()) {
            tapDigit(String.valueOf(digit));
            pause(300);
        }

        if (isVisibleQuick(verifyPinSubmitButton, 2)) {
            click(verifyPinSubmitButton);
        }

        waitForSeconds(12).until(driver -> !isVerifyPinScreenDisplayed());
        FlowLogger.step("PIN_GUARD", "Verify PIN handled successfully");
        return true;
    }

    private void tapDigit(String digit) {
        WebElement digitButton = waitForDigitButton(digit);
        clickElementReliably(digitButton, "PIN digit " + digit);
    }

    private WebElement waitForDigitButton(String digit) {
        return waitForSeconds(6).until(driver -> {
            List<WebElement> candidates = driver.findElements(pinDigitButtons).stream()
                    .filter(element -> {
                        try {
                            return element.isDisplayed() && digit.equals(element.getText().trim());
                        } catch (Exception ignored) {
                            return false;
                        }
                    })
                    .sorted(Comparator.comparingInt(element -> element.getRect().getY()))
                    .toList();

            if (candidates.isEmpty()) {
                return null;
            }

            return candidates.get(candidates.size() - 1);
        });
    }
}
