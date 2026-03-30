package api.utils;

import TestObject.FlowLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class RetryUtils {
    private RetryUtils() {
    }

    public static void waitForCondition(BooleanSupplier condition, Duration timeout, Duration pollInterval) {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (condition.getAsBoolean()) {
                return;
            }
            sleep(pollInterval);
        }
        throw new AssertionError("Condition was not satisfied within " + timeout.toSeconds() + " seconds");
    }

    public static <T> T retry(Supplier<T> action, int maxAttempts, int delayMs) {
        RuntimeException lastException = null;
        AssertionError lastAssertion = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = action.get();
                FlowLogger.step("RETRY", "Attempt " + attempt + "/" + maxAttempts + " succeeded");
                return result;
            } catch (AssertionError assertionError) {
                lastAssertion = assertionError;
                FlowLogger.step("RETRY", "Attempt " + attempt + "/" + maxAttempts
                        + " assertion failed: " + assertionError.getMessage());
            } catch (RuntimeException exception) {
                lastException = exception;
                FlowLogger.step("RETRY", "Attempt " + attempt + "/" + maxAttempts
                        + " failed: " + exception.getMessage());
            }

            if (attempt < maxAttempts) {
                sleep(Duration.ofMillis(delayMs));
            }
        }

        if (lastAssertion != null) {
            throw lastAssertion;
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new IllegalStateException("Retry exhausted without producing a result");
    }

    public static <T> T until(String description,
                              Supplier<T> action,
                              Predicate<T> successCondition,
                              Duration timeout,
                              Duration pollInterval) {
        Instant deadline = Instant.now().plus(timeout);
        RuntimeException lastFailure = null;

        while (Instant.now().isBefore(deadline)) {
            try {
                T result = action.get();
                if (successCondition.test(result)) {
                    return result;
                }
            } catch (RuntimeException exception) {
                lastFailure = exception;
            }

            sleep(pollInterval);
        }

        if (lastFailure != null) {
            throw new AssertionError("Timed out while waiting for " + description, lastFailure);
        }
        throw new AssertionError("Timed out while waiting for " + description);
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry wait interrupted", exception);
        }
    }
}
