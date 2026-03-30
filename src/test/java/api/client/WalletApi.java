package api.client;

import api.models.ApiEnvelope;
import api.models.CashFlowData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.math.BigDecimal;

public class WalletApi extends BaseApi {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Step("Get wallet balance from cash transactions endpoint")
    public BigDecimal getBalance(String token) {
        String accountId = api.utils.ApiConfig.getRequired("api.accountId");
        String path = "/api/v1/transactions/cash?ac=" + accountId;
        logRequest("GET", path);
        Response response = authorizedRequest(token)
                .queryParam("ac", accountId)
                .when()
                .get("/api/v1/transactions/cash");

        logResponse(path, response.asString());
        assertStatus(response, "Wallet balance API", 200, 201);
        ApiEnvelope<CashFlowData> envelope = read(response, new TypeReference<>() {
        });
        org.testng.Assert.assertTrue(envelope.isStatus(), "Wallet balance response should indicate success");
        org.testng.Assert.assertNotNull(envelope.getData(), "Wallet balance response data should not be null");
        org.testng.Assert.assertNotNull(envelope.getData().getCloseBalance(), "closeBalance should not be null");
        BigDecimal balance = envelope.getData().getCloseBalance();
        logParsed("balance=" + balance.toPlainString());
        return balance;
    }

    private <T> T read(Response response, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(response.asString(), typeReference);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize wallet response: " + response.asString(), exception);
        }
    }
}
