package TestObject;

import PageObject.P01_OnboardingPage;
import org.testng.annotations.Test;

public class T01_OnBoardingTest extends BaseTest {

   //@Test(priority = 1)
    public void userCanCompleteOnboardingAndOpenLogin() {

        P01_OnboardingPage onboarding = new P01_OnboardingPage();

        onboarding.completeOnboardingAndGoToLogin();



    }
}
