package PageObject;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class P00_LocationPermissionPage extends BasePage {
    private final By enableLocationCtaButton =
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").text(\"Enable Location\")");

    private final By enableLocationExactText =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Enable Location\")");

    private final By enableLocationButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Enable Location\")");

    private final By closeLocationButton =
            AppiumBy.xpath("//android.widget.TextView[@text='×' or @text='X' or @text='x']");

    private final By whileUsingTheAppButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"While using the app\")");

    private final By allowOnlyWhileUsingTheAppButton =
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Allow only while using the app\")");

    private final By allowTextButton =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Allow\")");

    private final By allowForegroundOnlyButton =
            AppiumBy.id("com.android.permissioncontroller:id/permission_allow_foreground_only_button");

    private final By allowButton =
            AppiumBy.id("com.android.permissioncontroller:id/permission_allow_button");

    public boolean isEnableLocationVisible() {
        return isAnyVisibleQuick(5, enableLocationCtaButton, enableLocationExactText, enableLocationButton);
    }

    public boolean isPermissionPopupVisible() {
        return isAnyVisibleQuick(
                5,
                whileUsingTheAppButton,
                allowOnlyWhileUsingTheAppButton,
                allowTextButton,
                allowForegroundOnlyButton,
                allowButton
        );
    }

    public void clickEnableLocationIfVisible() {
        if (!isEnableLocationVisible()) {
            System.out.println("Enable Location did not appear");
            return;
        }

        int attempts = 3;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            tapEnableLocationButton(attempt);

            if (!isEnableLocationVisible()) {
                System.out.println("Enable Location page dismissed");
                return;
            }

            System.out.println("Enable Location still visible after attempt " + attempt);
        }

        System.out.println("Enable Location click was attempted but the page is still visible");
    }

    private void tapEnableLocationButton(int attempt) {
        try {
            if (isVisibleQuick(enableLocationCtaButton, 1)) {
                WebElement button = waitForElement(enableLocationCtaButton, 2);
                int centerX = button.getRect().getX() + button.getRect().getWidth() / 2;
                int centerY = button.getRect().getY() + button.getRect().getHeight() / 2;

                tapAt(centerX, centerY);
                System.out.println(
                        "Enable Location clicked using exact CTA center on attempt "
                                + attempt
                                + " at x=" + centerX
                                + ", y=" + centerY
                );
                return;
            }

            if (isVisibleQuick(enableLocationExactText, 1)) {
                tapByScreenPercentage(0.5, 0.93);
                System.out.println("Enable Location clicked using bottom CTA fallback on attempt " + attempt);
                return;
            }
        } catch (Exception exception) {
            System.out.println("Element-center tap for Enable Location failed on attempt " + attempt);
        }

        try {
            tapByScreenPercentage(0.5, 0.93);
            System.out.println("Enable Location clicked using screen-percentage fallback on attempt " + attempt);
        } catch (Exception exception) {
            System.out.println("Screen-percentage fallback for Enable Location failed on attempt " + attempt);
            throw exception;
        }
    }

    public void allowLocationPermission() {
        if (!isPermissionPopupVisible()) {
            System.out.println("No location permission popup displayed. Continuing startup flow.");
            return;
        }

        int attempts = 2;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            if (clickFirstVisible(
                    2,
                    whileUsingTheAppButton,
                    allowOnlyWhileUsingTheAppButton,
                    allowTextButton,
                    allowForegroundOnlyButton,
                    allowButton
            )) {
                System.out.println("Location permission accepted on attempt " + attempt);
                return;
            }

            System.out.println("Location permission popup visible but not accepted yet. Retry " + attempt);
        }

        System.out.println("Location permission popup was not accepted immediately. Continuing startup flow.");
    }

    public void skipLocationIfStillVisible() {
        if (!isEnableLocationVisible()) {
            return;
        }

        if (clickIfVisible(closeLocationButton, 2)) {
            System.out.println("Location screen closed using X button");
            return;
        }

        tapByScreenPercentage(0.06, 0.06);
        System.out.println("Location screen close fallback tapped at top-left");
    }

    public void allowLocationWhileUsingApp() {
        allowLocationPermission();
    }
}
