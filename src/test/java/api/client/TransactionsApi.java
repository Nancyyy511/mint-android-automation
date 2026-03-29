package api.client;

import api.models.ApiEnvelope;
import api.models.DepositRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.Assert;

import java.io.File;

public class TransactionsApi extends BaseApi {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Step("Submit deposit request")
    public ApiEnvelope<Object> deposit(String token, DepositRequest requestBody) {
        String path = "/api/v1/transactions/deposit";
        logRequest("POST", path);

        Response response = authorizedRequest(token)
                .multiPart("amount", requestBody.getAmount())
                .multiPart("accountId", requestBody.getAccountId())
                .multiPart("date", requestBody.getDate())
                .multiPart("transactionPic", toFile(requestBody))
                .when()
                .post(path);

        logResponse(path, response.asString());
        Assert.assertTrue(
                response.statusCode() == 200 || response.statusCode() == 201,
                "Deposit API should return 200 or 201 but returned " + response.statusCode()
        );

        ApiEnvelope<Object> envelope = read(response, new TypeReference<>() {
        });
        Assert.assertTrue(envelope.isStatus(), "Deposit response should indicate success");
        logParsed("deposit accepted for accountId=" + requestBody.getAccountId()
                + ", amount=" + requestBody.getAmount()
                + ", date=" + requestBody.getDate());
        return envelope;
    }

    private File toFile(DepositRequest requestBody) {
        File file = requestBody.getReceiptPath().toFile();
        if (!file.isFile()) {
            throw new IllegalStateException("Deposit receipt file does not exist: " + file.getAbsolutePath());
        }
        return file;
    }

    private <T> T read(Response response, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(response.asString(), typeReference);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize deposit response: " + response.asString(), exception);
        }
    }
}
