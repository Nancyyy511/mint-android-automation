package TestObject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public class BaseTest extends Core {

    @BeforeSuite
    public void startDriver() throws Exception {
        DriverManager.initializeDriver();
        setDriver(DriverManager.getDriver(), DriverManager.getWait());
        System.out.println("App launched successfully");
    }

    @AfterSuite
    public void quitDriver() {
        DriverManager.quitDriver();
        setDriver(null, null);
        System.out.println("App closed");
    }
}
