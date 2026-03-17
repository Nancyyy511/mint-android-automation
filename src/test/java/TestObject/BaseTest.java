package TestObject;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        DriverManager.initializeDriver();
        System.out.println("App launched successfully");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
        System.out.println("App closed");
    }
}
