package TestObject;

public class TradeTestData {
    private final String ticker;
    private final String quantity;
    private final String price;
    private final String settlement;

    public TradeTestData(String ticker, String quantity, String price) {
        this(ticker, quantity, price, null);
    }

    public TradeTestData(String ticker, String quantity, String price, String settlement) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.settlement = settlement;
    }

    public String getTicker() {
        return ticker;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getSettlement() {
        return settlement;
    }
}
