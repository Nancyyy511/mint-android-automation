package api.utils;

import utils.AllureUtils;

import java.util.ArrayList;
import java.util.List;

public final class ApiLogContext {
    private static final ThreadLocal<List<ApiExchange>> EXCHANGES =
            ThreadLocal.withInitial(ArrayList::new);

    private ApiLogContext() {
    }

    public static void clear() {
        EXCHANGES.get().clear();
    }

    public static void add(String request, String response) {
        EXCHANGES.get().add(new ApiExchange(request, response));
    }

    public static void attachAll() {
        List<ApiExchange> exchanges = EXCHANGES.get();
        for (int index = 0; index < exchanges.size(); index++) {
            ApiExchange exchange = exchanges.get(index);
            int number = index + 1;
            AllureUtils.attachText("API Request " + number, exchange.request());
            AllureUtils.attachText("API Response " + number, exchange.response());
        }
    }

    private record ApiExchange(String request, String response) {
    }
}
