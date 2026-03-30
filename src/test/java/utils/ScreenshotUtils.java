package utils;

import TestObject.DriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScreenshotUtils {
    private ScreenshotUtils() {
    }

    public static Path capture(String testName) {
        if (DriverManager.getDriver() == null) {
            return null;
        }

        Path directory = Path.of("test-output", "screenshots");
        Path destination = directory.resolve(sanitize(testName) + ".png");
        byte[] bytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);

        try {
            Files.createDirectories(directory);
            Files.write(destination, bytes);
            return destination;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save screenshot for test: " + testName, exception);
        }
    }

    @Attachment(value = "{name}", type = "image/png")
    public static byte[] attach(String name, byte[] bytes) {
        return bytes;
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
