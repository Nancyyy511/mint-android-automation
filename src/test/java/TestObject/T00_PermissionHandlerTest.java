package TestObject;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class T00_PermissionHandlerTest {

    public static void handleNotificationPopup(AndroidDriver driver) {

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable(
                    AppiumBy.id(
                            "com.android.permissioncontroller:id/permission_allow_button"
                    )
            )).click();
            System.out.println("Notification permission allowed");
        } catch (Exception e) {
            System.out.println("No notification permission popup");
        }
    }



}
