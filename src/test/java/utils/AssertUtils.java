package utils;

import org.testng.Assert;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class AssertUtils {
    private AssertUtils() {
    }

    public static void assertEqualsIgnoreCase(String actual, String expected, String message) {
        if (actual == null || expected == null) {
            Assert.assertEquals(actual, expected, message);
            return;
        }
        Assert.assertEquals(actual.trim().toUpperCase(Locale.ROOT), expected.trim().toUpperCase(Locale.ROOT), message);
    }

    public static void assertListEqualsIgnoreOrder(Collection<String> actual,
                                                   Collection<String> expected,
                                                   String message) {
        Assert.assertEquals(normalize(actual), normalize(expected), message);
    }

    public static Set<String> normalize(Collection<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
