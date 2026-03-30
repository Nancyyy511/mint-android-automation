package utils;

import api.utils.ApiLogContext;
import core.driver.UnifiedDriverFactory;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class CrossPlatformReportingListener implements ITestListener, IConfigurationListener {
    @Override
    public void onStart(ITestContext context) {
        ApiLogContext.clear();
    }

    @Override
    public void onTestStart(ITestResult result) {
        ApiLogContext.clear();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        attachApiLogs();
        ApiLogContext.clear();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        attachScreenshot(result.getMethod().getMethodName());
        attachApiLogs();
        ApiLogContext.clear();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ApiLogContext.clear();
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        attachScreenshot(result.getMethod() == null ? "configurationFailure" : result.getMethod().getMethodName());
        attachApiLogs();
        ApiLogContext.clear();
    }

    @Override
    public void onFinish(ITestContext context) {
        ApiLogContext.clear();
    }

    private void attachScreenshot(String name) {
        AppiumDriver driver = UnifiedDriverFactory.getDriver();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }

        try {
            byte[] bytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            if (bytes != null && bytes.length > 0) {
                ScreenshotUtils.attach("Failure Screenshot - " + name, bytes);
            }
        } catch (Exception ignored) {
        }
    }

    private void attachApiLogs() {
        ApiLogContext.attachAll();
    }
}
