package TestObject;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtils() {
    }

    public static Path capture(String testName) {
        if (DriverManager.getDriver() == null) {
            return null;
        }

        File source = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.FILE);
        Path screenshotDirectory = Path.of("target", "screenshots");
        Path destination = screenshotDirectory.resolve(sanitizeFileName(testName) + "_" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + ".png");

        try {
            Files.createDirectories(screenshotDirectory);
            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (IOException exception) {
            throw new RuntimeException("Unable to save screenshot for test: " + testName, exception);
        }
    }

    private static String sanitizeFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
