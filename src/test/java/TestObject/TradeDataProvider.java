package TestObject;

import org.testng.annotations.DataProvider;

public class TradeDataProvider {

    @DataProvider(name = "buyLimitData", parallel = true)
    public static Object[][] buyLimitData() {
        return new Object[][]{
                {new TradeTestData("OFH", "1", "0.590")}
        };
    }

    @DataProvider(name = "buyMarketData", parallel = true)
    public static Object[][] buyMarketData() {
        return new Object[][]{
                {new TradeTestData("OFH", "5", null)}
        };
    }

    @DataProvider(name = "sellLimitData", parallel = true)
    public static Object[][] sellLimitData() {
        return new Object[][]{
                {new TradeTestData("NHPS", "1", "94.00", "T+2")}
        };
    }

    @DataProvider(name = "sellMarketData", parallel = true)
    public static Object[][] sellMarketData() {
        return new Object[][]{
                {new TradeTestData("NHPS", "1", null, "T+2")}
        };
    }
}
