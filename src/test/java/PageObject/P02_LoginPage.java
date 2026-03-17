package PageObject;

import TestObject.Core;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;

public class P02_LoginPage extends Core {

    // ===== Locators =====
    private final By usernameField =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(0)"
            );

    private final By passwordField =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(1)"
            );

    private final By loginButton =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.Button\").instance(1)"
            );


    private final By verifySecurityTitle =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Security\")"
            );

    private final By verifyButton =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Verify\")"
            );

    private final By pinZeroButton =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"0\")"
            );

    private final By homeIcon =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionContains(\"Home\")"
            );

    // ===== Actions =====

    public void login(String user, String pass) {

        waitForElement(usernameField).clear();
        waitForElement(usernameField).sendKeys(user);

        waitForElement(passwordField).clear();
        waitForElement(passwordField).sendKeys(pass);

        try { driver().hideKeyboard(); } catch (Exception ignored) {}

        WebElement login =
                new WebDriverWait(driver(), Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfElementLocated(loginButton));

        login.click();
    }

    public void handleSecurityQuestion() {

        if (!isVisibleQuick(verifySecurityTitle, 3)) {
            return;
        }

        wait().until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.className("android.widget.EditText")
        )).sendKeys("1");

        tap(verifyButton);
    }

    protected void tapNumericZero() {

        Dimension size = driver().manage().window().getSize();

        int x = size.width / 2;
        int y = (int) (size.height * 0.95);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(
                Duration.ZERO,
                PointerInput.Origin.viewport(),
                x, y
        ));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(new org.openqa.selenium.interactions.Pause(finger, Duration.ofMillis(150)));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver().perform(Collections.singletonList(tap));
    }


    public void enterPinZeroFourTimes() {

        if (!isVisibleQuick(pinZeroButton, 5)) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            tapNumericZero();

            try {
                driver().hideKeyboard();
            } catch (Exception ignored) {}



            // small wait
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
        }
    }

    // ===== Assertion =====
    public boolean isHomeDisplayed() {
        return isVisibleQuick(homeIcon, 10);
    }
    public boolean isAnyPostLoginScreenDisplayed() {
        return isVisibleQuick(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().descriptionContains(\"Home\")"
                ), 5
        )
                || isVisibleQuick(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"PIN\")"
                ), 5
        )
                || isVisibleQuick(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"Verify\")"
                ), 5
        );
    }

}
