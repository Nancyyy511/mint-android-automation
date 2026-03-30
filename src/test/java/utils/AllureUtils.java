package utils;

import io.qameta.allure.Allure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AllureUtils {
    private AllureUtils() {
    }

    public static void attachText(String name, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        Allure.addAttachment(name, "text/plain", content, ".txt");
    }

    public static void attachJson(String name, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        Allure.addAttachment(name, "application/json", content, ".json");
    }

    public static void attachFile(String name, Path path, String mimeType) {
        if (path == null || !Files.exists(path)) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            String extension = resolveExtension(path);
            Allure.addAttachment(name, mimeType, inputStream, extension);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to attach file to Allure: " + path, exception);
        }
    }

    private static String resolveExtension(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index) : ".txt";
    }
}
