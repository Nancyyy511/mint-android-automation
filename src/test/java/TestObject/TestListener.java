package TestObject;

import api.utils.ApiConfig;
import api.utils.ApiLogContext;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.AllureUtils;
import utils.LogcatUtils;
import utils.ScreenshotUtils;

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
        ApiLogContext.clear();

        DriverManager.SessionConfig sessionConfig = DriverManager.getSessionConfig();
        if (sessionConfig != null) {
            FlowLogger.step("TEST", "Environment=" + ApiConfig.getEnvironment()
                    + ", device=" + sessionConfig.deviceMode()
                    + ", udid=" + sessionConfig.udid());
            LogcatUtils.start(result.getMethod().getMethodName(), sessionConfig.udid());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info("Passed test: " + result.getMethod().getMethodName());
        LogcatUtils.stop();
        ApiLogContext.clear();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.severe("Failed test: " + result.getMethod().getMethodName());

        Path screenshotPath = ScreenshotUtils.capture(result.getMethod().getMethodName());
        if (screenshotPath != null) {
            AllureUtils.attachFile("Failure Screenshot - " + result.getMethod().getMethodName(), screenshotPath, "image/png");
        }

        Path logcatPath = LogcatUtils.stop();
        if (logcatPath != null) {
            FlowLogger.step("LOGCAT", "Failure logcat saved to " + logcatPath.toAbsolutePath());
            AllureUtils.attachFile("Logcat - " + result.getMethod().getMethodName(), logcatPath, "text/plain");
        }

        ApiLogContext.attachAll();
        ApiLogContext.clear();

        if (result.getThrowable() != null) {
            LOGGER.severe(result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warning("Skipped test: " + result.getMethod().getMethodName());
        LogcatUtils.stop();
        ApiLogContext.clear();
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
        LogcatUtils.stop();
        ApiLogContext.clear();
        if (result.getThrowable() != null) {
            LOGGER.severe(summarizeThrowable(result.getThrowable()));
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        LogcatUtils.stop();
        ApiLogContext.clear();
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
