package TestObject;

public final class TestDataReader {

    private TestDataReader() {
    }

    public static String get(String key) {
        return ConfigReader.getProperty(key);
    }
}
