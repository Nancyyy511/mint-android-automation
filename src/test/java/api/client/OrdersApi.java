package api.client;

import api.models.ApiEnvelope;
import api.models.OrderItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.restassured.response.Response;
public class OrdersApi extends BaseApi {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Step("Get order status from order details endpoint")
    public String getOrderStatus(String token, String orderId) {
        String path = "/api/v1/orders/" + orderId + "/info";
        logRequest("GET", path);
        Response response = authorizedRequest(token)
                .when()
                .get("/api/v1/orders/{orderId}/info", orderId);

        logResponse(path, response.asString());
        assertStatus(response, "Order details API", 200, 201);
        ApiEnvelope<OrderItem> envelope = read(response, new TypeReference<>() {
        });
        org.testng.Assert.assertTrue(envelope.isStatus(), "Order details response should indicate success");
        org.testng.Assert.assertNotNull(envelope.getData(), "Order details data should not be null");

        String status = valueOrEmpty(envelope.getData().getStatus());
        org.testng.Assert.assertFalse(status.isBlank(), "Order status should not be blank");
        logParsed("orderId=" + orderId + ", status=" + status);
        return status;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private <T> T read(Response response, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(response.asString(), typeReference);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize orders response: " + response.asString(), exception);
        }
    }
}
