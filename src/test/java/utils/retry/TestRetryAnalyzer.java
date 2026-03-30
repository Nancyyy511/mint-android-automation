package utils.retry;

import TestObject.FlowLogger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class TestRetryAnalyzer implements IRetryAnalyzer {
    private static final int MAX_RETRIES = 2;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount >= MAX_RETRIES) {
            return false;
        }

        retryCount++;
        FlowLogger.step("RETRY", "Retrying test " + result.getMethod().getMethodName()
                + " attempt " + retryCount + "/" + MAX_RETRIES);
        return true;
    }
}
