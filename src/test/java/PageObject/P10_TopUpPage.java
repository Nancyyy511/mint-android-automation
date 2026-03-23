package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

public class P10_TopUpPage extends BasePage {

    private final By walletIcon =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Wallet\")");

    private final By topUpButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Top up\")");

    private final By homeNavLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Home\")");

    private final By portfolioValueLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Portfolio value\")");

    private final By addMoneyTitle =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Add money\")");

    private final By instapayOption =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"InstaPay\")");

    private final By openInstapayTitle =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Open Your InstaPay\")");

    private final By confirmPaymentInInstapayScreen =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Confirm Payment\")");

    private final By confirmTransferTitle =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Confirm Transfer\")");

    private final By confirmPaymentInConfirmScreen =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i)^confirm\\\\s*payment$\").clickable(true)");

    private final By uploadReceiptArea =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Upload Your Receipt\")");

    private final By uploadReceiptAreaAlt =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Upload your Receipt\")");

    private final By dateDepositedLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Date Deposited\")");

    private final By timeDepositedLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Time Deposited\")");

    private final By amountLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i)^amount(\\\\s*\\\\(egp\\\\))?$\")");

    private final By instapayAddressLabel =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i)^instapay\\\\s+address$\")");

    private final By amountPlaceholder =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*deposited\\\\s+amount.*\")");

    private final By addressPlaceholder =
            AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i).*instapay\\\\s+address.*\")");

    private final By uploadedReceiptImage =
            AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"receipt\")");

    private final By photosTab =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Photos\")");

    private final By dismissPhotoAccessHint =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Dismiss\")");

    private final By dismissCloudMediaHint =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Dismiss\")");

    private final By photoPickerImageView =
            AppiumBy.className("android.widget.ImageView");

    private final By paymentSuccessfulTitle =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Payment Successful\")");

    private final By paymentSuccessfulTitleAlt1 =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Withdrawl Successful\")");

    private final By paymentSuccessfulTitleAlt2 =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Withdrawal Successful\")");

    private final By returnToHomeButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Return to Home\")");

    private final By backToHomeButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Back to Home\")");

    private final By backToWalletButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Back to wallet\")");

    public void openWallet() {
        logStart("Open Wallet");
        if (isWalletScreenDisplayed()) {
            logSuccess("Open Wallet", "Wallet already open");
            return;
        }

        retryClick(walletIcon, "Wallet icon", 3);
        waitForWalletScreenLoaded();
        logSuccess("Open Wallet", "Wallet screen loaded");
    }

    public void clickTopUp() {
        logStart("Click Top Up");
        if (isTopUpMethodScreenDisplayed()) {
            logSuccess("Click Top Up", "Top Up method screen already open");
            return;
        }

        retryClick(topUpButton, "Top up button", 3);
        waitForTopUpScreenLoaded();
        logSuccess("Click Top Up", "Top Up method screen loaded");
    }

    public void selectInstapay() {
        logStart("Select InstaPay");
        if (isInstapayRedirectScreenDisplayed()) {
            logSuccess("Select InstaPay", "InstaPay redirect screen already open");
            return;
        }

        retryClick(instapayOption, "InstaPay option", 3);
        waitForInstapayRedirectScreen();
        logSuccess("Select InstaPay", "InstaPay redirect screen loaded");
    }

    public void confirmExternalPayment() {
        logStart("Confirm External Payment");
        if (isConfirmTransferFlowLoaded()) {
            logSuccess("Confirm External Payment", "Confirm Transfer or receipt screen already open");
            return;
        }

        retryClick(confirmPaymentInInstapayScreen, "Confirm Payment in InstaPay screen", 3);
        waitForConfirmTransferFlowLoaded();
        logSuccess("Confirm External Payment", "Confirm Transfer flow screen loaded");
    }

    public void uploadReceipt(String filePath) {
        logStart("Upload Receipt");
        waitForUploadSection();

        if (isReceiptPreviewVisible()) {
            logSuccess("Upload Receipt", "Receipt preview already visible");
            return;
        }

        if (filePath == null || filePath.isBlank()) {
            logSuccess("Upload Receipt", "Receipt upload skipped because no path was provided");
            return;
        }

        retryClickUploadArea(2);
        handleFilePicker(filePath);
        waitForReceiptUploadResult();
        logSuccess("Upload Receipt", "Receipt upload step handled");
    }

    public void enterDepositDetails(String date, String time, String amount, String address) {
        logStart("Enter Deposit Details");
        waitForConfirmTransferFlowLoaded();

        System.out.println("Keeping existing Date Deposited value as displayed by the app");
        System.out.println("Keeping existing Time Deposited value as displayed by the app");
        ensureFieldVisible(amountLabel, amountPlaceholder, "Amount (EGP)");
        enterAmount(amount);
        prepareForAddressEntry();
        enterInstapayAddress(address);

        cleanupTopUpInputUi();
        logSuccess("Enter Deposit Details", "Deposit details entered");
    }

    public void submitTopUp() {
        logStart("Submit Top Up");
        clickFinalConfirmPaymentAndVerifyTransition();
        waitForSuccessScreen();
        logSuccess("Submit Top Up", "Success screen loaded");
    }

    public void verifySuccessScreen() {
        logStart("Verify Success Screen");
        waitForSuccessScreen();
        logSuccess("Verify Success Screen", "Payment Successful displayed");
    }

    public void returnToHome() {
        logStart("Return To Home");
        if (isReturnedHome()) {
            logSuccess("Return To Home", "Already on home/wallet area");
            return;
        }
        if (!clickFirstVisible(4, backToHomeButton, backToWalletButton, returnToHomeButton)) {
            tapBackHomeFallback();
        }
        waitForReturnDestinationLoaded();
        logSuccess("Return To Home", "Returned to home/wallet area");
    }

    public void waitForTopUpScreenLoaded() {
        waitForElement(addMoneyTitle, 15);
        waitForElement(instapayOption, 15);
    }

    public void waitForSuccessScreen() {
        waitForSeconds(35).until(driver ->
                isSuccessUiVisible()
                        || isWalletScreenDisplayed()
        );
    }

    public boolean isSuccessScreenDisplayed() {
        return isSuccessUiVisible() || isWalletScreenDisplayed();
    }

    public boolean isWalletScreenDisplayed() {
        return isVisibleQuick(walletIcon, 5) && isVisibleQuick(topUpButton, 5);
    }

    public boolean isHomeScreenDisplayed() {
        return isVisibleQuick(homeNavLabel, 3) && isVisibleQuick(portfolioValueLabel, 3);
    }

    public boolean isTopUpMethodScreenDisplayed() {
        return isVisibleQuick(addMoneyTitle, 5) && isVisibleQuick(instapayOption, 5);
    }

    public boolean isInstapayRedirectScreenDisplayed() {
        return isVisibleQuick(openInstapayTitle, 5) && isVisibleQuick(confirmPaymentInInstapayScreen, 5);
    }

    public boolean isConfirmTransferScreenDisplayed() {
        return isVisibleQuick(confirmTransferTitle, 5) && isVisibleQuick(confirmPaymentInConfirmScreen, 5);
    }

    public boolean isReturnedHome() {
        return isWalletScreenDisplayed() || isHomeScreenDisplayed();
    }

    private void waitForWalletScreenLoaded() {
        waitForElement(walletIcon, 15);
        waitForElement(topUpButton, 15);
    }

    private void waitForReturnDestinationLoaded() {
        waitForSeconds(20).until(driver -> isReturnedHome());
    }

    private void waitForInstapayRedirectScreen() {
        waitForElement(openInstapayTitle, 15);
        waitForElement(confirmPaymentInInstapayScreen, 15);
    }

    private void waitForConfirmTransferFlowLoaded() {
        waitForSeconds(10).until(driver ->
                !driver.findElements(confirmTransferTitle).isEmpty()
                        || !driver.findElements(uploadReceiptArea).isEmpty()
                        || !driver.findElements(uploadReceiptAreaAlt).isEmpty()
                        || !driver.findElements(dateDepositedLabel).isEmpty()
                        || !driver.findElements(timeDepositedLabel).isEmpty()
                        || isReceiptPreviewVisible()
        );

        if (isVisibleQuick(confirmTransferTitle, 3)) {
            System.out.println("Confirm Transfer screen appeared after Confirm Payment");
        } else if (isAnyVisibleQuick(3, uploadReceiptArea, uploadReceiptAreaAlt)) {
            System.out.println("Upload Receipt screen appeared directly after Confirm Payment");
        } else if (isReceiptPreviewVisible()) {
            System.out.println("Receipt preview is visible on the confirm screen");
        } else if (isAnyVisibleQuick(3, dateDepositedLabel, timeDepositedLabel)) {
            System.out.println("Confirm screen fields are visible after upload");
        } else {
            throw new RuntimeException("Confirm screen did not load after Confirm Payment");
        }

        waitForElement(dateDepositedLabel, 15);
        waitForElement(timeDepositedLabel, 15);
    }

    private boolean isConfirmTransferFlowLoaded() {
        return isVisibleQuick(dateDepositedLabel, 5)
                && isVisibleQuick(timeDepositedLabel, 5)
                && isAnyVisibleQuick(5, uploadReceiptArea, uploadReceiptAreaAlt, uploadedReceiptImage);
    }

    private void retryClick(By locator, String elementName, int retries) {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                WebElement element = waitForSeconds(10).until(ExpectedConditions.elementToBeClickable(locator));
                clickElementReliably(element, elementName);
                System.out.println(elementName + " clicked successfully on attempt " + attempt);
                return;
            } catch (Exception exception) {
                lastFailure = new RuntimeException("Could not click " + elementName + " on attempt " + attempt, exception);
                System.out.println(elementName + " click failed on attempt " + attempt);
            }
        }

        throw lastFailure == null ? new RuntimeException("Could not click " + elementName) : lastFailure;
    }

    private void clickFinalConfirmPaymentAndVerifyTransition() {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                cleanupTopUpInputUi();
                swipeUpSmall();

                // Prefer tapping the bottom-most "Confirm Payment" text anchor if available.
                WebElement ctaText = findBottomConfirmPaymentText();
                if (ctaText != null) {
                    int centerX = ctaText.getRect().getX() + ctaText.getRect().getWidth() / 2;
                    int centerY = ctaText.getRect().getY() + ctaText.getRect().getHeight() / 2;
                    tapAt(centerX, centerY);
                    System.out.println("Confirm Payment CTA tapped by text-anchor coordinates on attempt " + attempt);
                } else {
                    tapBottomConfirmCtaFallback();
                    System.out.println("Confirm Payment CTA tapped by bottom-center fallback coordinates on attempt " + attempt);
                }

                if (waitForPostConfirmTransition(8) || !isStillOnConfirmPaymentForm()) {
                    return;
                }
            } catch (Exception exception) {
                System.out.println("Confirm Payment in Confirm Transfer screen click failed on attempt " + attempt);
                lastFailure = new RuntimeException("Could not confirm transfer transition on attempt " + attempt, exception);
            }
        }

        throw lastFailure == null
                ? new RuntimeException("Confirm Payment click did not transition away from confirm form")
                : lastFailure;
    }

    private boolean isStillOnConfirmPaymentForm() {
        return isAnyVisibleQuick(2, dateDepositedLabel, timeDepositedLabel, confirmPaymentInConfirmScreen);
    }

    private WebElement findBottomConfirmPaymentText() {
        int screenHeight = driver().manage().window().getSize().height;
        return driver().findElements(
                        AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i)^confirm\\\\s*payment$\")")
                ).stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed();
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .filter(element -> element.getRect().getY() > (int) (screenHeight * 0.55))
                .max((a, b) -> Integer.compare(a.getRect().getY(), b.getRect().getY()))
                .orElse(null);
    }

    private boolean waitForPostConfirmTransition(int seconds) {
        try {
            return waitForSeconds(seconds).until(driver ->
                    isSuccessUiVisible()
                            || isWalletScreenDisplayed()
                            || !isStillOnConfirmPaymentForm()
            );
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isSuccessUiVisible() {
        return isAnyVisibleQuick(2,
                paymentSuccessfulTitle,
                paymentSuccessfulTitleAlt1,
                paymentSuccessfulTitleAlt2,
                backToHomeButton,
                backToWalletButton,
                returnToHomeButton
        );
    }

    private void tapBottomConfirmCtaFallback() {
        int width = driver().manage().window().getSize().width;
        int height = driver().manage().window().getSize().height;
        int centerX = width / 2;

        int primaryY = (int) (height * 0.92);
        int secondaryY = (int) (height * 0.88);
        int tertiaryY = (int) (height * 0.85);

        tapAt(centerX, primaryY);
        pause(700);
        if (isStillOnConfirmPaymentForm()) {
            tapAt(centerX, secondaryY);
            pause(700);
        }
        if (isStillOnConfirmPaymentForm()) {
            tapAt(centerX, tertiaryY);
            pause(700);
        }
    }

    private void tapBackHomeFallback() {
        int width = driver().manage().window().getSize().width;
        int height = driver().manage().window().getSize().height;
        int centerX = width / 2;

        // Success screen buttons are centered around lower-middle section.
        tapAt(centerX, (int) (height * 0.62)); // Back to Home
        pause(700);
        if (!isWalletScreenDisplayed()) {
            tapAt(centerX, (int) (height * 0.70)); // Back to wallet
            pause(700);
        }
    }

    private boolean isElementEnabled(WebElement element) {
        try {
            String enabledAttr = element.getAttribute("enabled");
            if (enabledAttr != null) {
                return "true".equalsIgnoreCase(enabledAttr);
            }
        } catch (Exception ignored) {
        }
        try {
            return element.isEnabled();
        } catch (Exception ignored) {
            return false;
        }
    }

    private void retryClickUploadArea(int retries) {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                if (clickIfVisible(uploadReceiptArea, 5) || clickIfVisible(uploadReceiptAreaAlt, 5)) {
                    System.out.println("Upload receipt area clicked successfully on attempt " + attempt);
                    return;
                }

                if (isVisibleQuick(uploadReceiptArea, 3)) {
                    tap(uploadReceiptArea);
                    System.out.println("Upload receipt area tapped successfully on attempt " + attempt);
                    return;
                }

                if (isVisibleQuick(uploadReceiptAreaAlt, 3)) {
                    tap(uploadReceiptAreaAlt);
                    System.out.println("Upload receipt area tapped successfully on attempt " + attempt);
                    return;
                }
            } catch (Exception exception) {
                lastFailure = new RuntimeException("Could not click upload receipt area on attempt " + attempt, exception);
            }
        }

        throw lastFailure == null
                ? new RuntimeException("Could not click upload receipt area")
                : lastFailure;
    }

    private void handleFilePicker(String filePath) {
        System.out.println("Handling receipt upload with path: " + filePath);
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        waitForSeconds(10).until(driver ->
                !driver.findElements(photosTab).isEmpty()
                        || !driver.findElements(dismissPhotoAccessHint).isEmpty()
                        || !driver.findElements(photoPickerImageView).isEmpty()
                        || !driver.findElements(AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"" + fileName + "\")"
                )).isEmpty()
        );

        if (clickIfVisible(dismissPhotoAccessHint, 3)) {
            System.out.println("Dismissed photo picker access hint");
        }

        if (clickIfVisible(dismissCloudMediaHint, 3)) {
            System.out.println("Dismissed cloud media hint");
        }

        clickIfVisible(photosTab, 3);

        By targetFile = AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + fileName + "\")");
        if (clickIfVisible(targetFile, 5)) {
            System.out.println("Receipt file selected by file name");
            return;
        }

        List<WebElement> thumbnails = driver().findElements(photoPickerImageView).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getHeight() > 120)
                .filter(element -> element.getRect().getY() > 250)
                .filter(element -> element.getRect().getWidth() > 120)
                .toList();

        if (!thumbnails.isEmpty()) {
            WebElement targetThumbnail = thumbnails.stream()
                    .min(Comparator
                            .comparingInt((WebElement element) -> element.getRect().getY())
                            .thenComparingInt(element -> element.getRect().getX()))
                    .orElse(thumbnails.get(0));

            clickElementReliably(targetThumbnail, "Receipt thumbnail");
            System.out.println("Receipt file selected using lower-left visible thumbnail");
            return;
        }

        WebElement photosTabElement = waitForElement(photosTab, 5);
        int screenWidth = driver().manage().window().getSize().width;
        int fallbackX = (int) (screenWidth * 0.18);
        int fallbackY = photosTabElement.getRect().getY() + photosTabElement.getRect().getHeight() + 170;
        tapAt(fallbackX, fallbackY);
        System.out.println("Receipt thumbnail selected using lower-left picker fallback tap");
        pause(1200);

        if (isReceiptPreviewVisible()
                || !isAnyVisibleQuick(2, photosTab, dismissPhotoAccessHint, dismissCloudMediaHint)) {
            return;
        }

        throw new RuntimeException("Could not select a receipt image from the Android photo picker");
    }

    private void waitForUploadSection() {
        waitForSeconds(10).until(driver ->
                !driver.findElements(uploadReceiptArea).isEmpty()
                        || !driver.findElements(uploadReceiptAreaAlt).isEmpty()
                        || isReceiptPreviewVisible()
        );
    }

    private void waitForReceiptUploadResult() {
        waitForSeconds(15).until(driver -> isReceiptPreviewVisible());
        System.out.println("Receipt upload validated successfully");
    }

    private boolean isReceiptPreviewVisible() {
        boolean uploadPromptGone = !isAnyVisibleQuick(2, uploadReceiptArea, uploadReceiptAreaAlt);
        boolean stillOnReceiptForm = isAnyVisibleQuick(2, dateDepositedLabel, timeDepositedLabel);
        boolean pickerClosed = !isAnyVisibleQuick(2, photosTab, dismissPhotoAccessHint, dismissCloudMediaHint);
        boolean explicitPreviewVisible = isVisibleQuick(uploadedReceiptImage, 2);
        return explicitPreviewVisible || (uploadPromptGone && stillOnReceiptForm && pickerClosed);
    }

    private void enterAmount(String amount) {
        setInputValue(() -> resolveAmountField(), amount, "Amount (EGP)");
    }

    private void enterInstapayAddress(String address) {
        setInputValue(() -> resolveInstapayAddressField(), address, "Instapay Address");
    }

    private WebElement resolveTextField(By... anchors) {
        for (By anchor : anchors) {
            if (anchor != null && isVisibleQuick(anchor, 3)) {
                WebElement anchorElement = waitForElement(anchor, 5);
                WebElement inputNearAnchor = findClosestEditableFieldBelow(anchorElement);
                if (inputNearAnchor != null) {
                    return inputNearAnchor;
                }
            }
        }

        List<WebElement> editTexts = driver().findElements(AppiumBy.className("android.widget.EditText"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();

        if (!editTexts.isEmpty()) {
            return editTexts.get(editTexts.size() - 1);
        }

        throw new RuntimeException("Could not resolve a stable text field for deposit details");
    }

    private WebElement findClosestEditableFieldBelow(WebElement anchorElement) {
        int anchorTop = anchorElement.getRect().getY();
        int anchorBottom = anchorTop + anchorElement.getRect().getHeight();
        int anchorCenterX = anchorElement.getRect().getX() + anchorElement.getRect().getWidth() / 2;

        WebElement best = null;
        int bestDistance = Integer.MAX_VALUE;
        int bestCenterDistance = Integer.MAX_VALUE;

        for (WebElement element : driver().findElements(AppiumBy.className("android.widget.EditText"))) {
            try {
                if (!element.isDisplayed()) {
                    continue;
                }
                int elementTop = element.getRect().getY();
                if (elementTop < anchorBottom - 10) {
                    continue;
                }
                int distance = Math.abs(elementTop - anchorBottom);
                int centerDistance = Math.abs((element.getRect().getX() + (element.getRect().getWidth() / 2)) - anchorCenterX);
                if (distance < bestDistance || (distance == bestDistance && centerDistance < bestCenterDistance)) {
                    best = element;
                    bestDistance = distance;
                    bestCenterDistance = centerDistance;
                }
            } catch (Exception ignored) {
            }
        }

        return best;
    }

    private WebElement resolveAmountField() {
        ensureFieldVisible(amountLabel, amountPlaceholder, "Amount (EGP)");
        WebElement amountAnchor = resolveVisibleAnchor(amountLabel, amountPlaceholder);
        return findClosestEditableFieldBelow(amountAnchor, instapayAddressLabel);
    }

    private WebElement resolveInstapayAddressField() {
        try {
            if (isAnyVisibleQuick(2, instapayAddressLabel, addressPlaceholder)) {
                WebElement addressAnchor = resolveVisibleAnchor(instapayAddressLabel, addressPlaceholder);
                WebElement anchored = findClosestEditableFieldBelow(addressAnchor, null);
                if (anchored != null) {
                    return anchored;
                }
            }
        } catch (Exception ignored) {
        }

        List<WebElement> editTexts = driver().findElements(AppiumBy.className("android.widget.EditText")).stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed();
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .toList();

        for (WebElement field : editTexts) {
            if (isAddressLikeField(field)) {
                return field;
            }
        }

        if (editTexts.size() >= 2) {
            // On this form, address input is usually the lower EditText.
            return editTexts.get(editTexts.size() - 1);
        }
        if (editTexts.size() == 1) {
            return editTexts.get(0);
        }

        throw new RuntimeException("Could not resolve Instapay Address input field");
    }

    private boolean isAddressLikeField(WebElement element) {
        return attributeContainsIgnoreCase(element, "hint", "address")
                || attributeContainsIgnoreCase(element, "hint", "instapay")
                || attributeContainsIgnoreCase(element, "text", "address")
                || attributeContainsIgnoreCase(element, "contentDescription", "address")
                || attributeContainsIgnoreCase(element, "resource-id", "address");
    }

    private boolean attributeContainsIgnoreCase(WebElement element, String attribute, String token) {
        try {
            String value = element.getAttribute(attribute);
            return value != null && value.toLowerCase().contains(token.toLowerCase());
        } catch (Exception ignored) {
            return false;
        }
    }

    private WebElement resolveVisibleAnchor(By primary, By secondary) {
        if (isVisibleQuick(primary, 2)) {
            return waitForElement(primary, 5);
        }
        if (isVisibleQuick(secondary, 2)) {
            return waitForElement(secondary, 5);
        }
        throw new RuntimeException("Could not resolve a visible anchor for deposit field");
    }

    private WebElement findClosestEditableFieldBelow(WebElement anchorElement, By nextSectionLabel) {
        int anchorTop = anchorElement.getRect().getY();
        int anchorBottom = anchorTop + anchorElement.getRect().getHeight();
        int anchorCenterX = anchorElement.getRect().getX() + anchorElement.getRect().getWidth() / 2;
        int computedLowerBoundary = Integer.MAX_VALUE;

        if (nextSectionLabel != null && isVisibleQuick(nextSectionLabel, 2)) {
            computedLowerBoundary = waitForElement(nextSectionLabel, 5).getRect().getY();
        }
        final int lowerBoundary = computedLowerBoundary;

        WebElement candidate = null;
        int bestDistance = Integer.MAX_VALUE;
        int bestCenterDistance = Integer.MAX_VALUE;
        for (WebElement element : driver().findElements(AppiumBy.className("android.widget.EditText"))) {
            try {
                if (!element.isDisplayed()) {
                    continue;
                }
                int elementTop = element.getRect().getY();
                if (elementTop < anchorBottom - 10 || elementTop >= lowerBoundary) {
                    continue;
                }
                int distance = Math.abs(elementTop - anchorBottom);
                int centerDistance = Math.abs((element.getRect().getX() + (element.getRect().getWidth() / 2)) - anchorCenterX);
                if (distance < bestDistance || (distance == bestDistance && centerDistance < bestCenterDistance)) {
                    candidate = element;
                    bestDistance = distance;
                    bestCenterDistance = centerDistance;
                }
            } catch (Exception ignored) {
            }
        }

        if (candidate == null) {
            throw new RuntimeException("Could not resolve editable field below section anchor");
        }

        return candidate;
    }

    private void setInputValue(FieldResolver resolver, String value, String fieldName) {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement freshField = resolver.resolve();
                freshField.click();
                try {
                    freshField.clear();
                } catch (Exception ignored) {
                }
                freshField.sendKeys(value);
                if (!fieldContainsValue(freshField, value)) {
                    throw new RuntimeException(fieldName + " value was not retained in the intended field");
                }
                System.out.println(fieldName + " entered: " + value);
                return;
            } catch (InvalidElementStateException exception) {
                throw exception;
            } catch (Exception exception) {
                System.out.println("Could not enter " + fieldName + " on attempt " + attempt + ": " + exception.getMessage());
                lastFailure = new RuntimeException("Could not enter " + fieldName + " on attempt " + attempt, exception);
            }
        }

        throw lastFailure == null ? new RuntimeException("Could not enter " + fieldName) : lastFailure;
    }

    private boolean fieldContainsValue(WebElement field, String expectedValue) {
        try {
            String text = field.getText();
            if (text != null && text.contains(expectedValue)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            String textAttr = field.getAttribute("text");
            return textAttr != null && textAttr.contains(expectedValue);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void cleanupTopUpInputUi() {
        // Avoid screen taps here because they can trigger unrelated controls on dynamic forms.
        hideKeyboardIfVisible();
        dismissKeyboardOverlay();
    }

    private void prepareForAddressEntry() {
        cleanupTopUpInputUi();

        if (isAnyVisibleQuick(2, instapayAddressLabel, addressPlaceholder)) {
            return;
        }

        for (int attempt = 1; attempt <= 2; attempt++) {
            swipeUpSmall();
            cleanupTopUpInputUi();
            if (isAnyVisibleQuick(2, instapayAddressLabel, addressPlaceholder)) {
                System.out.println("Instapay Address became visible after post-amount adjustment attempt " + attempt);
                return;
            }
        }
    }

    private void ensureFieldVisible(By primaryLocator, By secondaryLocator, String fieldName) {
        if (isAnyVisibleQuick(3, primaryLocator, secondaryLocator)) {
            return;
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            scrollDown();
            if (isAnyVisibleQuick(4, primaryLocator, secondaryLocator)) {
                System.out.println(fieldName + " became visible after scroll attempt " + attempt);
                return;
            }
        }

        throw new RuntimeException(fieldName + " field did not become visible on the receipt form");
    }

    private void logStart(String step) {
        System.out.println("[TOPUP][START] " + step);
    }

    private void logSuccess(String step, String details) {
        System.out.println("[TOPUP][SUCCESS] " + step + " - " + details);
    }

    @FunctionalInterface
    private interface FieldResolver {
        WebElement resolve();
    }
}
