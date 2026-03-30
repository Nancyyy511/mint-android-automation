package api.client;

import TestObject.FlowLogger;
import api.models.ApiEnvelope;
import api.models.OrderJournalingRequestPayload;
import api.models.OrderJournalingSubmission;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.Assert;

public class OrderJournalingApi extends BaseApi {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Step("Get order journaling by order id")
    public ApiEnvelope<OrderJournalingSubmission> getByOrderId(String token, String orderId) {
        String path = "/api/v1/order-journaling/" + orderId;
        logRequest("GET", path);

        Response response = authorizedRequest(token)
                .when()
                .get("/api/v1/order-journaling/{orderId}", orderId);

        logResponse(path, response.asString());
        assertStatus(response, "Get order journaling API", 200, 201);

        ApiEnvelope<OrderJournalingSubmission> envelope = read(response, new TypeReference<>() {
        });
        Assert.assertTrue(envelope.isStatus(), "Order journaling GET response should indicate success");
        Assert.assertNotNull(envelope.getData(), "Order journaling GET data should not be null");
        envelope.getData().hydrateRequestIfMissing();
        FlowLogger.step("JOURNALING_API", "OrderId=" + orderId + ", full response=" + response.asString());
        return envelope;
    }

    @Step("Submit order journaling through API")
    public ApiEnvelope<OrderJournalingSubmission> submit(String token, OrderJournalingRequestPayload requestPayload) {
        logRequest("POST", "/api/v1/order-journaling");

        Response response = authorizedRequest(token)
                .body(requestPayload)
                .when()
                .post("/api/v1/order-journaling");

        logResponse("/api/v1/order-journaling", response.asString());
        assertStatus(response, "Submit order journaling API", 200, 201);

        ApiEnvelope<OrderJournalingSubmission> envelope = read(response, new TypeReference<>() {
        });
        if (envelope.getData() != null) {
            envelope.getData().hydrateRequestIfMissing();
        }
        return envelope;
    }

    private <T> T read(Response response, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(response.asString(), typeReference);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize journaling response: " + response.asString(), exception);
        }
    }
}
