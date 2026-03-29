package api.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItem {
    @JsonAlias({"orderId", "id", "orderNo"})
    private String orderId;

    @JsonAlias({"status", "orderStatus"})
    private String status;

    @JsonAlias({"ticker", "stockName", "symbol"})
    private String ticker;

    @JsonAlias({"createdAt", "createdDate", "timeStamp", "date"})
    private String createdAt;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
