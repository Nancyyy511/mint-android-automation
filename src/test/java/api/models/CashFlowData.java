package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CashFlowData {
    private BigDecimal openBalance;
    private BigDecimal closeBalance;
    private List<CashTransaction> transactions;

    public BigDecimal getOpenBalance() {
        return openBalance;
    }

    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    public BigDecimal getCloseBalance() {
        return closeBalance;
    }

    public void setCloseBalance(BigDecimal closeBalance) {
        this.closeBalance = closeBalance;
    }

    public List<CashTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<CashTransaction> transactions) {
        this.transactions = transactions;
    }
}
