package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.Comparator;
import java.util.List;
import java.time.Duration;
import java.time.Instant;

public class P02_LoginPage extends BasePage {

    public enum PostLoginState {
        HOME,
        PIN,
        SECURITY_QUESTION,
        LOGIN_FORM,
        UNKNOWN
    }

    // ===== Locators =====
    private final By loginOption =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Log in\")"
            );

    private final By loginOptionAlt =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Login\")"
            );

    private final By signUpOption =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Sign\")"
            );

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

    private final By invalidCredentialsMessage =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textMatches(\"(?i).*(invalid|incorrect|failed|try again).*\")"
            );

    private final By pinZeroButton =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"0\")"
            );

    private final By pinZeroButtonAlt =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"0\")"
            );

    private final By homeIcon =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionContains(\"Home\")"
            );

    private final By walletNavIcon =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Wallet\")"
            );

    // ===== Actions =====

    public void login(String user, String pass) {
        openLoginFormIfVisible();

        System.out.println("Starting credential entry");
        waitForCredentialsForm();

        WebElement username = waitForElement(usernameField, 15);
        username.clear();
        username.sendKeys(user);
        System.out.println("Username entered");

        WebElement password = waitForElement(passwordField, 15);
        password.clear();
        password.sendKeys(pass);
        System.out.println("Password entered");

        hideKeyboardIfVisible();

        WebElement login =
                waitForSeconds(15)
                        .until(ExpectedConditions.presenceOfElementLocated(loginButton));

        login.click();
        System.out.println("Login button clicked");
        waitForPostLoginTransition();
    }

    public boolean isLoginOptionVisible() {
        return isAnyVisibleQuick(5, loginOption, loginOptionAlt);
    }

    public boolean isCredentialsFormVisible() {
        return isAnyVisibleQuick(5, usernameField, passwordField);
    }

    public boolean isLoginScreenDisplayed() {
        return isLoginOptionVisible() || isCredentialsFormVisible();
    }

    public void openLoginFormIfVisible() {
        if (isLoginOptionVisible() && !isCredentialsFormVisible()) {
            if (!clickFirstVisible(3, loginOption, loginOptionAlt)) {
                tap(loginOption);
            }
            System.out.println("Login option clicked from auth choice screen");
            waitForCredentialsForm();
        }
    }

    public void waitForLoginScreen() {
        if (isLoginOptionVisible() && !isCredentialsFormVisible()) {
            System.out.println("Auth choice screen reached");
            return;
        }
        if (isCredentialsFormVisible()) {
            System.out.println("Login credentials screen reached");
            return;
        }
        throw new RuntimeException("Login screen was not reached");
    }

    public void handleSecurityQuestion() {
        if (!isVisibleQuick(verifySecurityTitle, 10)) {
            System.out.println("Security question screen not displayed");
            return;
        }

        System.out.println("Security question screen detected");
        WebElement answerField = waitForSeconds(10).until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.className("android.widget.EditText")
        ));
        answerField.clear();
        answerField.sendKeys("1");
        System.out.println("Security answer entered");

        if (!clickIfVisible(verifyButton, 5)) {
            tap(verifyButton);
        }
        System.out.println("Verify button clicked");
        waitForPostLoginTransition();
    }

    protected void tapNumericZero() {
        WebElement zeroButton = findPinZeroButtonForCurrentSurface();
        clickElementByGesture(zeroButton, "PIN zero");
        pause(600);
    }

    private void waitForAnyZeroButton() {
        waitForSeconds(6).until(driver -> !driver.findElements(pinZeroButtonAlt).isEmpty());
    }

    private WebElement findPinZeroButtonForCurrentSurface() {
        waitForAnyZeroButton();
        List<WebElement> zeroCandidates = driver().findElements(pinZeroButtonAlt).stream()
                .filter(WebElement::isDisplayed)
                .toList();

        if (zeroCandidates.isEmpty()) {
            throw new RuntimeException("PIN zero button is not visible on the current screen.");
        }

        if (isKeyboardVisible()) {
            WebElement keyboardZero = zeroCandidates.stream()
                    .max(Comparator.comparingInt((WebElement element) ->
                            element.getRect().getY() + (element.getRect().getHeight() / 2)
                    ))
                    .orElseThrow(() -> new RuntimeException("Could not locate hardware keyboard zero button."));

            int centerX = keyboardZero.getRect().getX() + keyboardZero.getRect().getWidth() / 2;
            int centerY = keyboardZero.getRect().getY() + keyboardZero.getRect().getHeight() / 2;
            System.out.println("Selected hardware keyboard zero candidate at x=" + centerX + ", y=" + centerY);
            return keyboardZero;
        }

        int screenHeight = driver().manage().window().getSize().height;
        int screenWidth = driver().manage().window().getSize().width;
        int minPinAreaY = (int) (screenHeight * 0.55);
        int maxPinAreaY = (int) (screenHeight * 0.96);
        int centerX = screenWidth / 2;

        WebElement appKeypadZero = zeroCandidates.stream()
                .filter(element -> {
                    int elementCenterY = element.getRect().getY() + (element.getRect().getHeight() / 2);
                    return elementCenterY >= minPinAreaY && elementCenterY <= maxPinAreaY;
                })
                .min(Comparator
                        .comparingInt((WebElement element) ->
                                Math.abs((element.getRect().getX() + (element.getRect().getWidth() / 2)) - centerX)
                        )
                        .thenComparing(Comparator.comparingInt((WebElement element) ->
                                element.getRect().getY() + (element.getRect().getHeight() / 2)
                        ).reversed()))
                .orElse(null);

        if (appKeypadZero == null) {
            WebElement fallbackZero = zeroCandidates.stream()
                    .max(Comparator.comparingInt((WebElement element) ->
                            element.getRect().getY() + (element.getRect().getHeight() / 2)
                    ))
                    .orElseThrow(() -> new RuntimeException("Could not locate any visible PIN zero button."));

            int fallbackX = fallbackZero.getRect().getX() + fallbackZero.getRect().getWidth() / 2;
            int fallbackY = fallbackZero.getRect().getY() + fallbackZero.getRect().getHeight() / 2;
            System.out.println(
                    "No app keypad zero found in band [" + minPinAreaY + ", " + maxPinAreaY
                            + "]. Falling back to lowest visible zero at x=" + fallbackX + ", y=" + fallbackY
            );
            return fallbackZero;
        }

        int selectedX = appKeypadZero.getRect().getX() + appKeypadZero.getRect().getWidth() / 2;
        int selectedY = appKeypadZero.getRect().getY() + appKeypadZero.getRect().getHeight() / 2;
        System.out.println(
                "Selected app keypad zero candidate at x=" + selectedX + ", y=" + selectedY
                        + " within keypad band [" + minPinAreaY + ", " + maxPinAreaY + "]"
        );
        return appKeypadZero;
    }


    public void enterPinZeroFourTimes() {
        if (!isVisibleQuick(pinZeroButton, 10)) {
            System.out.println("PIN screen not displayed");
            return;
        }

        System.out.println("PIN screen detected");
        for (int i = 0; i < 4; i++) {
            waitForAnyZeroButton();
            tapNumericZero();
            pause(1200);
            System.out.println("PIN digit entered: " + (i + 1));
        }

        PostLoginState postPinState = waitForPostPinState();
        if (postPinState == PostLoginState.HOME) {
            System.out.println("PIN completed and home screen reached");
            return;
        }

        if (postPinState == PostLoginState.PIN) {
            throw new RuntimeException("PIN entry did not complete successfully. PIN screen is still displayed.");
        }

        if (postPinState == PostLoginState.SECURITY_QUESTION) {
            throw new RuntimeException("Security question screen appeared after PIN entry.");
        }

        if (postPinState == PostLoginState.LOGIN_FORM) {
            throw new RuntimeException("App returned to login form after PIN entry.");
        }

        throw new RuntimeException("Unknown state after PIN entry.");
    }

    // ===== Assertion =====
    public boolean isHomeDisplayed() {
        return isAnyVisibleQuick(10, homeIcon, walletNavIcon);
    }

    public void waitForCredentialsForm() {
        waitForSeconds(15).until(driver ->
                !driver.findElements(usernameField).isEmpty() || !driver.findElements(passwordField).isEmpty()
        );
        System.out.println("Login credentials screen reached");
    }

    public void waitForPostLoginTransition() {
        Duration maxTimeout = Duration.ofSeconds(45);
        Duration pollingInterval = Duration.ofSeconds(1);
        Instant startedAt = Instant.now();
        final long[] lastLoggedSecond = {-1};

        System.out.println(
                "[LOGIN][WAIT] Waiting for post-login transition. timeout="
                        + maxTimeout.toSeconds() + "s, poll=" + pollingInterval.toMillis() + "ms"
        );

        try {
            new FluentWait<>(driver())
                    .withTimeout(maxTimeout)
                    .pollingEvery(pollingInterval)
                    .ignoring(NoSuchElementException.class)
                    .ignoring(StaleElementReferenceException.class)
                    .until(driver -> {
                        boolean securityVisible = isVisibleNow(verifySecurityTitle);
                        boolean pinVisible = isVisibleNow(pinZeroButton) || isVisibleNow(pinZeroButtonAlt);
                        boolean homeVisible = isVisibleNow(homeIcon) || isVisibleNow(walletNavIcon);
                        boolean invalidCredentialsVisible = isVisibleNow(invalidCredentialsMessage);
                        boolean loginFormStillVisible = isVisibleNow(usernameField) || isVisibleNow(passwordField);

                        long elapsedSeconds = Duration.between(startedAt, Instant.now()).toSeconds();
                        if (elapsedSeconds != lastLoggedSecond[0] && (elapsedSeconds == 0 || elapsedSeconds % 5 == 0)) {
                            lastLoggedSecond[0] = elapsedSeconds;
                            System.out.println(
                                    "[LOGIN][WAIT] elapsed=" + elapsedSeconds + "s"
                                            + " | security=" + securityVisible
                                            + " | pin=" + pinVisible
                                            + " | home=" + homeVisible
                                            + " | invalidCredentials=" + invalidCredentialsVisible
                                            + " | loginFormStillVisible=" + loginFormStillVisible
                            );
                        }

                        return securityVisible || pinVisible || homeVisible || invalidCredentialsVisible;
                    });
        } catch (TimeoutException exception) {
            System.out.println(
                    "[LOGIN][WAIT] Timed out after " + Duration.between(startedAt, Instant.now()).toSeconds()
                            + "s while waiting for a post-login state. Current visible state: " + describeCurrentLoginSurface()
            );
            if (isCredentialsFormVisible()) {
                throw new RuntimeException(
                        "Login did not progress beyond the credentials screen after tapping Login. Check the username/password or any new login validation state.",
                        exception
                );
            }
            throw exception;
        }

        System.out.println(
                "[LOGIN][WAIT] Post-login state detected after "
                        + Duration.between(startedAt, Instant.now()).toSeconds()
                        + "s: " + describeCurrentLoginSurface()
        );

        if (isVisibleNow(invalidCredentialsMessage)) {
            throw new RuntimeException("Login failed because the app displayed an invalid credentials/error message.");
        }
    }

    private boolean isVisibleNow(By locator) {
        try {
            return driver().findElements(locator).stream().anyMatch(element -> {
                try {
                    return element.isDisplayed();
                } catch (Exception ignored) {
                    return false;
                }
            });
        } catch (Exception exception) {
            return false;
        }
    }

    private String describeCurrentLoginSurface() {
        if (isVisibleNow(homeIcon) || isVisibleNow(walletNavIcon)) {
            return "HOME";
        }
        if (isVisibleNow(pinZeroButton) || isVisibleNow(pinZeroButtonAlt)) {
            return "PIN";
        }
        if (isVisibleNow(verifySecurityTitle)) {
            return "SECURITY_QUESTION";
        }
        if (isVisibleNow(invalidCredentialsMessage)) {
            return "INVALID_CREDENTIALS_MESSAGE";
        }
        if (isVisibleNow(usernameField) || isVisibleNow(passwordField)) {
            return "LOGIN_FORM";
        }
        return "UNKNOWN";
    }

    public PostLoginState detectPostLoginState() {
        if (isHomeDisplayed()) {
            return PostLoginState.HOME;
        }
        if (isVisibleQuick(pinZeroButton, 3)) {
            return PostLoginState.PIN;
        }
        if (isVisibleQuick(verifySecurityTitle, 3)) {
            return PostLoginState.SECURITY_QUESTION;
        }
        if (isCredentialsFormVisible()) {
            return PostLoginState.LOGIN_FORM;
        }
        return PostLoginState.UNKNOWN;
    }

    public PostLoginState waitForPostPinState() {
        waitForSeconds(20).until(driver ->
                !driver.findElements(homeIcon).isEmpty()
                        || !driver.findElements(walletNavIcon).isEmpty()
                        || !driver.findElements(pinZeroButton).isEmpty()
                        || !driver.findElements(verifySecurityTitle).isEmpty()
                        || !driver.findElements(usernameField).isEmpty()
        );

        PostLoginState state = detectPostLoginState();
        System.out.println("Post-PIN state detected: " + state);
        return state;
    }

    public void waitForHomeScreen() {
        waitForSeconds(20).until(driver ->
                !driver.findElements(homeIcon).isEmpty() || !driver.findElements(walletNavIcon).isEmpty()
        );
        System.out.println("Home screen reached");
    }
    public boolean isAnyPostLoginScreenDisplayed() {
        return isAnyVisibleQuick(
                5,
                homeIcon,
                walletNavIcon
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
