package utils;

import TestObject.FlowLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class LogcatUtils {
    private static final ThreadLocal<LogcatSession> SESSION = new ThreadLocal<>();

    private LogcatUtils() {
    }

    public static void start(String testName, String udid) {
        stop();

        Path directory = Path.of("test-output", "logs");
        Path outputPath = directory.resolve("logcat_" + sanitize(testName) + ".txt");

        try {
            Files.createDirectories(directory);
            clearLogcatBuffer(udid);

            Process process = new ProcessBuilder("adb", "-s", udid, "logcat", "-v", "time")
                    .redirectErrorStream(true)
                    .redirectOutput(outputPath.toFile())
                    .start();

            SESSION.set(new LogcatSession(process, outputPath, udid));
            FlowLogger.step("LOGCAT", "Started logcat capture for " + udid + " -> " + outputPath.toAbsolutePath());
        } catch (IOException exception) {
            FlowLogger.step("LOGCAT", "Failed to start logcat capture for " + udid + ": " + exception.getMessage());
        }
    }

    public static Path stop() {
        LogcatSession session = SESSION.get();
        if (session == null) {
            return null;
        }

        try {
            session.process().destroy();
            if (!session.process().waitFor(3, TimeUnit.SECONDS)) {
                session.process().destroyForcibly();
            }
            FlowLogger.step("LOGCAT", "Stopped logcat capture for " + session.udid()
                    + " -> " + session.path().toAbsolutePath());
            return session.path();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            session.process().destroyForcibly();
            throw new IllegalStateException("Interrupted while stopping logcat capture", exception);
        } finally {
            SESSION.remove();
        }
    }

    public static Path getCurrentLogcatPath() {
        LogcatSession session = SESSION.get();
        return session == null ? null : session.path();
    }

    private static void clearLogcatBuffer(String udid) {
        try {
            new ProcessBuilder("adb", "-s", udid, "logcat", "-c").start().waitFor();
        } catch (Exception ignored) {
        }
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private record LogcatSession(Process process, Path path, String udid) {
    }
}
