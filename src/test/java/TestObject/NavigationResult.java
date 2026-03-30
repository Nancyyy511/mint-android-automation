package TestObject;

public record NavigationResult(Status status, String strategy, String details) {
    public boolean succeeded() {
        return status == Status.SUCCESS || status == Status.RECOVERED;
    }

    public enum Status {
        SUCCESS,
        RECOVERED,
        FAILED
    }
}
