package TestObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FlowLogger {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FlowLogger() {
    }

    public static void step(String flow, String message) {
        System.out.println("[" + TS.format(LocalDateTime.now()) + "][" + flow + "][T" + Thread.currentThread().threadId() + "] " + message);
    }
}

