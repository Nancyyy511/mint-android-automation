package TestObject;

import io.qameta.allure.Allure;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class TestListener implements ITestListener, IConfigurationListener {

    private static final Logger LOGGER = Logger.getLogger(TestListener.class.getName());

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info("Starting suite: " + context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info("Starting test: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info("Passed test: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.severe("Failed test: " + result.getMethod().getMethodName());

        Path screenshotPath = ScreenshotUtils.capture(result.getMethod().getMethodName());
        if (screenshotPath != null) {
            try (InputStream inputStream = Files.newInputStream(screenshotPath)) {
                Allure.addAttachment("Failure Screenshot - " + result.getMethod().getMethodName(), "image/png", inputStream, ".png");
            } catch (IOException exception) {
                LOGGER.severe("Unable to attach screenshot to Allure report: " + exception.getMessage());
            }
        }

        if (result.getThrowable() != null) {
            LOGGER.severe(result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warning("Skipped test: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            LOGGER.warning("Skip reason: " + summarizeThrowable(result.getThrowable()));
        } else {
            LOGGER.warning("Skip reason: this test was most likely skipped because a configuration method failed earlier.");
        }
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        String methodName = result.getMethod() == null ? "<unknown>" : result.getMethod().getMethodName();
        LOGGER.severe("Configuration failure in " + methodName);
        if (result.getThrowable() != null) {
            LOGGER.severe(summarizeThrowable(result.getThrowable()));
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info("Finished suite: " + context.getName());
    }

    private String summarizeThrowable(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            return root.getClass().getSimpleName();
        }
        return message;
    }
}
