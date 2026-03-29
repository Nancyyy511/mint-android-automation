package api.models;

import java.nio.file.Path;

public class DepositRequest {
    private final String accountId;
    private final String amount;
    private final String date;
    private final Path receiptPath;

    public DepositRequest(String accountId, String amount, String date, Path receiptPath) {
        this.accountId = accountId;
        this.amount = amount;
        this.date = date;
        this.receiptPath = receiptPath;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public Path getReceiptPath() {
        return receiptPath;
    }
}
