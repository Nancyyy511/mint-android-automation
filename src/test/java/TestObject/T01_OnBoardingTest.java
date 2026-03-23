package TestObject;
import org.testng.annotations.Test;

public class T01_OnBoardingTest extends BaseTest {

    //@Test(priority = 1)
    public void userCanCompleteOnboardingAndOpenLogin() {
        new StartupFlow().completeStartupToLogin();
    }
}
