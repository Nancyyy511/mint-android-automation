package PageObject;

import TestObject.FlowLogger;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class P14_OrderJournalingPage extends BasePage {
    private final By[] addJournalingButtonLocators = new By[]{
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*what.*think.*order.*\")"),
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*add.*journaling.*\")"),
            By.xpath("//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'journaling')]")
    };

    private final By[] viewJournalingButtonLocators = new By[]{
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*view.*journaling.*\")"),
            AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Journaling\")"),
            By.xpath("//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'view journaling')]")
    };

    private final By[] journalingSheetTitleLocators = new By[]{
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*journaling.*\")"),
            By.xpath("//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'journaling')]")
    };

    private final By[] bottomSheetContainerLocators = new By[]{
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.FrameLayout\")"),
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.ViewGroup\")"),
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\")")
    };

    private final By[] noteInputLocators = new By[]{
            AppiumBy.className("android.widget.EditText"),
            By.xpath("//android.widget.EditText"),
            By.xpath("//*[contains(translate(@resource-id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'note')]")
    };

    private final By[] submitButtonLocators = new By[]{
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*submit.*\")"),
            By.xpath("//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]"),
            By.xpath("//*[contains(translate(@resource-id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]")
    };

    private final By[] closeButtonLocators = new By[]{
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*(close|x).*\")"),
            AppiumBy.androidUIAutomator("new UiSelector().descriptionMatches(\"(?i).*(close|dismiss).*\")"),
            By.xpath("//*[contains(translate(@resource-id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'close')]")
    };

    private final By textViews = AppiumBy.className("android.widget.TextView");

    public boolean hasViewJournalingButton() {
        return isAnyVisibleFromList(2, viewJournalingButtonLocators);
    }

    public boolean hasAddJournalingButton() {
        return isAnyVisibleFromList(2, addJournalingButtonLocators);
    }

    public void openComposer() {
        retryAction("open journaling composer", () -> {
            logCurrentScreen("Before opening journaling composer");
            if (!clickFirstVisible(3, addJournalingButtonLocators)) {
                scrollToText("journaling");
                if (!clickFirstVisible(3, addJournalingButtonLocators)) {
                    captureUiDiagnostics("open-composer-failure");
                    throw new RuntimeException("Add Journaling CTA was not found");
                }
            }
            waitForAnyVisible(5, journalingSheetTitleLocators);
            return true;
        }, 2, 500);
    }

    public void selectPositiveOutcome(String label) {
        clickOption(label, "outcome");
    }

    public void selectDecisionFactor(String factor) {
        clickOption(factor, "decision factor");
    }

    public String selectFirstAvailableDecisionFactor(List<String> factors) {
        for (String factor : factors) {
            try {
                if (hasOption(factor)) {
                    selectDecisionFactor(factor);
                    return factor;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public boolean hasOption(String label) {
        By[] locators = buildFlexibleLocators(label);
        if (findFirstVisibleElement(0, locators) != null) {
            return true;
        }
        return readVisibleTexts().stream()
                .map(text -> text.toUpperCase(Locale.ROOT))
                .anyMatch(text -> text.contains(label.trim().toUpperCase(Locale.ROOT)));
    }

    public void enterNote(String note) {
        if (note == null || note.isBlank()) {
            return;
        }

        WebElement input = waitForAnyVisible(5, noteInputLocators);
        input.click();
        input.clear();
        input.sendKeys(note);
        dismissKeyboardOverlay();
        dismissFloatingInputToolbar();
        FlowLogger.step("JOURNALING_PAGE", "Entered note: " + note);
    }

    public boolean isSubmitEnabled() {
        try {
            return findFirstVisibleElement(2, submitButtonLocators).isEnabled();
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isComposerOpen() {
        return !isBottomSheetClosed();
    }

    public void submit() {
        retryAction("submit journaling", () -> {
            WebElement button = findFirstVisibleElement(0, submitButtonLocators);
            FlowLogger.step("JOURNALING_PAGE", "Attempting submit click. visible=" + (button != null)
                    + ", enabled=" + (button != null && button.isEnabled()));
            logSubmitButtonGeometry(button);
            if (button != null) {
                clickElementReliably(button, "Journaling Submit");
                pause(350);
                if (isSubmittedSuccessfully()) {
                    FlowLogger.step("JOURNALING_PAGE", "Submit clicked successfully via direct element click");
                    return true;
                }
            }
            tapSubmitButton(button);
            waitForSeconds(8).until(driver -> isSubmittedSuccessfully());
            FlowLogger.step("JOURNALING_PAGE", "Submit clicked successfully and View Journaling appeared");
            return true;
        }, 2, 500);
    }

    public boolean isBottomSheetClosed() {
        return !isAnyVisibleFromList(1, journalingSheetTitleLocators);
    }

    public boolean isSubmittedSuccessfully() {
        return isBottomSheetClosed() && hasViewJournalingButton();
    }

    public void openViewJournaling() {
        retryAction("open view journaling", () -> {
            if (!clickFirstVisible(3, viewJournalingButtonLocators)) {
                scrollToText("view journaling");
                if (!clickFirstVisible(3, viewJournalingButtonLocators)) {
                    captureUiDiagnostics("open-view-journaling-failure");
                    throw new RuntimeException("View Journaling CTA was not found");
                }
            }
            waitForAnyVisible(5, journalingSheetTitleLocators);
            return true;
        }, 2, 500);
    }

    public String readNote() {
        try {
            return waitForAnyVisible(2, noteInputLocators).getText().trim();
        } catch (Exception exception) {
            return "";
        }
    }

    public boolean containsVisibleValue(String expectedText) {
        String expectedUpper = expectedText.trim().toUpperCase(Locale.ROOT);
        return readVisibleTexts().stream()
                .map(text -> text.toUpperCase(Locale.ROOT))
                .anyMatch(text -> text.contains(expectedUpper));
    }

    public String findMatchingVisibleValue(Collection<String> candidates) {
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            if (containsVisibleValue(candidate)) {
                return candidate;
            }
        }
        return "";
    }

    public List<String> findMatchingVisibleValues(Collection<String> candidates) {
        return candidates.stream()
                .filter(candidate -> candidate != null && !candidate.isBlank())
                .filter(this::containsVisibleValue)
                .toList();
    }

    public List<String> getVisibleOptions(Collection<String> candidates) {
        List<String> visibleTexts = readVisibleTexts();
        return candidates.stream()
                .filter(candidate -> candidate != null && !candidate.isBlank())
                .filter(candidate -> visibleTexts.stream()
                        .map(this::normalizeForMatch)
                        .anyMatch(text -> text.contains(normalizeForMatch(candidate))))
                .toList();
    }

    public boolean waitUntilSubmitEnabled() {
        try {
            return waitForSeconds(5).until(driver -> {
                WebElement button = findFirstVisibleElement(0, submitButtonLocators);
                return button != null && (button.isEnabled() || isComposerOpen());
            });
        } catch (Exception exception) {
            return false;
        }
    }

    public List<String> readVisibleTexts() {
        return driver().findElements(textViews).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .map(String::trim)
                .toList();
    }

    public void closeViewJournaling() {
        if (clickFirstVisible(2, closeButtonLocators)) {
            waitForViewJournalingDismissal();
            return;
        }

        sendAndroidKeyEvent(4, "BACK");
        waitForViewJournalingDismissal();
    }

    public void resetToOrderDetails() {
        if (!isComposerOpen()) {
            return;
        }
        FlowLogger.step("JOURNALING_PAGE", "Resetting journaling composer back to order details");
        if (clickFirstVisible(2, closeButtonLocators)) {
            waitForViewJournalingDismissal();
            return;
        }
        sendAndroidKeyEvent(4, "BACK");
        waitForViewJournalingDismissal();
    }

    private void clickOption(String label, String elementType) {
        retryAction("select " + elementType + " '" + label + "'", () -> {
            By[] locators = buildFlexibleLocators(label);
            WebElement element = findFirstVisibleElement(2, locators);
            if (element == null) {
                captureUiDiagnostics("missing-" + elementType.replace(' ', '-'));
                throw new RuntimeException("Could not find journaling " + elementType + ": " + label);
            }
            clickElementReliably(element, "Journaling " + elementType + " " + label);
            return true;
        }, 2, 500);
    }

    private WebElement waitForAnyVisible(long seconds, By... locators) {
        return waitForSeconds(seconds).until(driver -> findFirstVisibleElement(0, locators));
    }

    private WebElement waitForAnyClickable(long seconds, By... locators) {
        return waitForSeconds(seconds).until(driver -> {
            for (By locator : locators) {
                try {
                    WebElement element = driver.findElement(locator);
                    if (element.isDisplayed() && element.isEnabled()) {
                        return element;
                    }
                } catch (Exception ignored) {
                }
            }
            return null;
        });
    }

    private WebElement findFirstVisibleElement(long seconds, By... locators) {
        for (By locator : locators) {
            try {
                if (seconds > 0) {
                    return waitForVisible(locator, seconds);
                }
                List<WebElement> elements = driver().findElements(locator);
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return elements.get(0);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private By[] buildFlexibleLocators(String value) {
        String regex = toLooseRegex(value);
        String token = firstToken(value);
        String lowerValue = value.toLowerCase(Locale.ROOT);
        return new By[]{
                AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"" + escapeUiAutomator(regex) + "\")"),
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + escapeUiAutomator(token) + "\")"),
                By.xpath("//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + lowerValue + "')]"),
                By.xpath("//*[contains(translate(@content-desc,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + lowerValue + "')]"),
                By.xpath("//*[contains(translate(@resource-id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + token.toLowerCase(Locale.ROOT) + "')]")
        };
    }

    private boolean isAnyVisibleFromList(int seconds, By[] locators) {
        return Arrays.stream(locators).anyMatch(locator -> isVisibleQuick(locator, seconds));
    }

    private String normalizeForMatch(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private void tapSubmitButton(WebElement button) {
        if (button != null) {
            try {
                Rectangle rect = button.getRect();
                int centerX = rect.getX() + rect.getWidth() / 2;
                int centerY = rect.getY() + rect.getHeight() / 2;
                FlowLogger.step("JOURNALING_PAGE", "Primary submit clickGesture using button center x=" + centerX + ", y=" + centerY);
                clickGestureAt(centerX, centerY);
                pause(250);
                if (isSubmittedSuccessfully()) {
                    return;
                }
            } catch (Exception exception) {
                FlowLogger.step("JOURNALING_PAGE", "Primary submit button-center clickGesture skipped because geometry became stale: "
                        + exception.getClass().getSimpleName());
            }
        }

        // Use the proven viewport-relative target first so the tap scales with screen size.
        double[] preferredViewportTargets = {0.962, 0.955, 0.970};
        for (double yTarget : preferredViewportTargets) {
            FlowLogger.step("JOURNALING_PAGE", "Primary submit clickGesture using viewport percentage x=0.50, y=" + yTarget);
            tapByViewportPercentage(0.50, yTarget);
            pause(250);
            if (isSubmittedSuccessfully()) {
                return;
            }
        }

        if (button != null) {
            try {
                Rectangle rect = button.getRect();
                int centerX = rect.getX() + rect.getWidth() / 2;
                int centerY = rect.getY() + rect.getHeight() / 2;
                FlowLogger.step("JOURNALING_PAGE", "Submit fallback clickGesture using button center x=" + centerX + ", y=" + centerY);
                clickGestureAt(centerX, centerY);
                pause(250);
                if (isSubmittedSuccessfully()) {
                    return;
                }
            } catch (Exception exception) {
                FlowLogger.step("JOURNALING_PAGE", "Submit button center tap skipped because geometry became stale: "
                        + exception.getClass().getSimpleName());
            }
        }

        WebElement sheet = findBottomSheetContainer();
        if (sheet != null) {
            int sheetCenterX = sheet.getRect().getX() + sheet.getRect().getWidth() / 2;
            int sheetTapY = sheet.getRect().getY() + (int) (sheet.getRect().getHeight() * 0.89);
            FlowLogger.step("JOURNALING_PAGE", "Submit fallback clickGesture using bottom-sheet relative coordinates x="
                    + sheetCenterX + ", y=" + sheetTapY);
            clickGestureAt(sheetCenterX, sheetTapY);
            pause(250);
            if (isSubmittedSuccessfully()) {
                return;
            }
        }

        WebElement noteField = findFirstVisibleElement(0, noteInputLocators);
        if (noteField != null) {
            int noteCenterX = noteField.getRect().getX() + noteField.getRect().getWidth() / 2;
            int noteBottom = noteField.getRect().getY() + noteField.getRect().getHeight();
            int offset = Math.max(70, Math.min(130, noteField.getRect().getHeight()));
            int anchoredY = Math.min(driver().manage().window().getSize().height - 120, noteBottom + offset);
            FlowLogger.step("JOURNALING_PAGE", "Submit fallback clickGesture using note-field anchor x="
                    + noteCenterX + ", y=" + anchoredY);
            clickGestureAt(noteCenterX, anchoredY);
            pause(250);
            if (isSubmittedSuccessfully()) {
                return;
            }
        }

        FlowLogger.step("JOURNALING_PAGE", "Submit clickGesture attempts were exhausted without confirmation");
    }

    private void waitForViewJournalingDismissal() {
        waitForSeconds(10).until(driver ->
                isBottomSheetClosed()
                        || hasViewJournalingButton()
                        || hasAddJournalingButton());
    }

    private WebElement findBottomSheetContainer() {
        WebElement title = findFirstVisibleElement(0, journalingSheetTitleLocators);
        if (title == null) {
            return null;
        }

        int titleY = title.getRect().getY();
        int bestScore = Integer.MAX_VALUE;
        WebElement bestCandidate = null;

        for (By locator : bottomSheetContainerLocators) {
            List<WebElement> elements = driver().findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (!element.isDisplayed()) {
                        continue;
                    }
                    int height = element.getRect().getHeight();
                    int width = element.getRect().getWidth();
                    int top = element.getRect().getY();
                    if (height < 300 || width < 250 || top > titleY) {
                        continue;
                    }

                    int score = Math.abs(top - Math.max(0, titleY - 60));
                    if (score < bestScore) {
                        bestScore = score;
                        bestCandidate = element;
                    }
                } catch (Exception ignored) {
                }
            }
            if (bestCandidate != null) {
                return bestCandidate;
            }
        }

        return null;
    }

    private void logSubmitButtonGeometry(WebElement button) {
        if (button == null) {
            FlowLogger.step("JOURNALING_PAGE", "Submit button geometry unavailable because the element is not currently visible");
            return;
        }

        try {
            Rectangle rect = button.getRect();
            int centerX = rect.getX() + rect.getWidth() / 2;
            int centerY = rect.getY() + rect.getHeight() / 2;
            int viewportLeft = getViewportLeft();
            int viewportTop = getViewportTop();
            int viewportWidth = getViewportWidth();
            int viewportHeight = getViewportHeight();
            double viewportX = viewportWidth == 0 ? 0.0 : (double) (centerX - viewportLeft) / viewportWidth;
            double viewportY = viewportHeight == 0 ? 0.0 : (double) (centerY - viewportTop) / viewportHeight;
            FlowLogger.step("JOURNALING_PAGE", "Submit button rect: x=" + rect.getX()
                    + ", y=" + rect.getY()
                    + ", width=" + rect.getWidth()
                    + ", height=" + rect.getHeight()
                    + ", centerX=" + centerX
                    + ", centerY=" + centerY
                    + ", viewportX=" + String.format(Locale.US, "%.3f", viewportX)
                    + ", viewportY=" + String.format(Locale.US, "%.3f", viewportY));
        } catch (Exception exception) {
            FlowLogger.step("JOURNALING_PAGE", "Submit button geometry could not be read: "
                    + exception.getClass().getSimpleName());
        }
    }

    private void tapByViewportPercentage(double xPercent, double yPercent) {
        int viewportLeft = getViewportLeft();
        int viewportTop = getViewportTop();
        int viewportWidth = getViewportWidth();
        int viewportHeight = getViewportHeight();
        int targetX = viewportLeft + (int) Math.round(viewportWidth * xPercent);
        int targetY = viewportTop + (int) Math.round(viewportHeight * yPercent);

        FlowLogger.step("JOURNALING_PAGE", "Viewport-based submit clickGesture computed from viewportRect. x=" + targetX
                + ", y=" + targetY + ", xPercent=" + xPercent + ", yPercent=" + yPercent);
        clickGestureAt(targetX, targetY);
    }

    private int getViewportLeft() {
        Object value = driver().getCapabilities().getCapability("viewportRect");
        if (value instanceof java.util.Map<?, ?> map && map.get("left") instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private int getViewportTop() {
        Object value = driver().getCapabilities().getCapability("viewportRect");
        if (value instanceof java.util.Map<?, ?> map && map.get("top") instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private int getViewportWidth() {
        Object value = driver().getCapabilities().getCapability("viewportRect");
        if (value instanceof java.util.Map<?, ?> map && map.get("width") instanceof Number number) {
            return number.intValue();
        }
        Dimension size = driver().manage().window().getSize();
        return size.getWidth();
    }

    private int getViewportHeight() {
        Object value = driver().getCapabilities().getCapability("viewportRect");
        if (value instanceof java.util.Map<?, ?> map && map.get("height") instanceof Number number) {
            return number.intValue();
        }
        Dimension size = driver().manage().window().getSize();
        return size.getHeight();
    }
}
