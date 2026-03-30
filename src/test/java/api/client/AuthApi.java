package api.client;

import api.models.ApiEnvelope;
import api.models.GeoLocation;
import api.models.LoginData;
import api.models.LoginRequest;
import api.models.SecurityQuestion;
import api.models.ValidateQuestionRequest;
import api.models.VerifyPinData;
import api.models.VerifyPinRequest;
import api.utils.ApiConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;

public class AuthApi extends BaseApi {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Step("Authenticate user through login, 2FA question validation, and PIN verification")
    public String authenticateAndGetToken() {
        return authenticateAndGetToken(ApiConfig.getUsername(), ApiConfig.getPassword());
    }

    @Step("Authenticate user through login, 2FA question validation, and PIN verification")
    public String authenticateAndGetToken(String username, String password) {
        ApiEnvelope<LoginData> loginEnvelope = login(username, password);
        LoginData loginData = loginEnvelope.getData();
        Assert.assertNotNull(loginData, "Login response data should not be null");

        String guestToken = loginData.getGuestToken();
        Assert.assertNotNull(guestToken, "guestToken should be present after login");

        if (loginData.isHasSecurityQuestions()) {
            SecurityQuestion firstQuestion = getMyQuestions(guestToken).stream()
                    .filter(this::isUsableSecurityQuestion)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No usable security question returned for the authenticated user"));

            validateQuestion(guestToken, firstQuestion.getQuestionId(), ApiConfig.getOptional("api.securityAnswer", "1"));
        } else {
            System.out.println("[API][AUTH] Skipping security question validation because hasSecurityQuestions=false");
        }

        Assert.assertTrue(loginData.isPinCreated(), "PIN must be created for API authentication to continue");
        VerifyPinData verifyPinData = verifyPin(guestToken, ApiConfig.getOptional("api.pin", "0000")).getData();
        Assert.assertNotNull(verifyPinData, "Verify PIN response data should not be null");
        Assert.assertNotNull(verifyPinData.getToken(), "Final auth token should not be null after PIN verification");
        return verifyPinData.getToken();
    }

    @Step("Call login endpoint")
    public ApiEnvelope<LoginData> login() {
        return login(ApiConfig.getUsername(), ApiConfig.getPassword());
    }

    @Step("Call login endpoint")
    public ApiEnvelope<LoginData> login(String username, String password) {
        LoginRequest requestBody = new LoginRequest();
        requestBody.setGeoLocation(new GeoLocation(
                Double.parseDouble(ApiConfig.getOptional("api.geoLatitude", "10")),
                Double.parseDouble(ApiConfig.getOptional("api.geoLongitude", "15"))
        ));
        requestBody.setEmail(username);
        requestBody.setPassword(password);
        requestBody.setUseBiometric(ApiConfig.getInt("api.useBiometric", 1));

        Response response = request()
                .header("MOBILEIP", ApiConfig.getOptional("api.mobileIp", "192.168.100.46"))
                .body(requestBody)
                .when()
                .post("/api/v1/auth/user/login");

        assertStatus(response, "Login API", 200, 201);
        return read(response, new TypeReference<>() {
        });
    }

    @Step("Fetch user security questions")
    public List<SecurityQuestion> getMyQuestions(String guestToken) {
        Response response = authorizedRequest(guestToken)
                .when()
                .get("/api/v1/auth/user/2fa/my-questions");

        assertStatus(response, "2FA questions API", 200, 201);
        ApiEnvelope<List<SecurityQuestion>> envelope = read(response, new TypeReference<>() {
        });
        Assert.assertTrue(envelope.isStatus(), "2FA questions response should indicate success");
        return envelope.getData();
    }

    @Step("Validate security question")
    public ApiEnvelope<Object> validateQuestion(String guestToken, int questionId, String answer) {
        ValidateQuestionRequest requestBody = new ValidateQuestionRequest(questionId, answer);
        Response response = authorizedRequest(guestToken)
                .body(requestBody)
                .when()
                .post("/api/v1/auth/user/2fa/validate-question");

        assertStatus(response, "Validate question API", 200, 201);
        ApiEnvelope<Object> envelope = read(response, new TypeReference<>() {
        });
        Assert.assertTrue(envelope.isStatus(), "Validate question response should indicate success");
        return envelope;
    }

    @Step("Verify PIN")
    public ApiEnvelope<VerifyPinData> verifyPin(String guestToken, String pin) {
        Response response = authorizedRequest(guestToken)
                .body(new VerifyPinRequest(pin))
                .when()
                .post("/api/v1/auth/user/verify-pin");

        assertStatus(response, "Verify PIN API", 200, 201);
        ApiEnvelope<VerifyPinData> envelope = read(response, new TypeReference<>() {
        });
        Assert.assertTrue(envelope.isStatus(), "Verify PIN response should indicate success");
        return envelope;
    }

    private <T> T read(Response response, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(response.asString(), typeReference);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize API response: " + response.asString(), exception);
        }
    }

    private boolean isUsableSecurityQuestion(SecurityQuestion question) {
        if (question == null || question.getQuestionId() == null) {
            return false;
        }
        return question.getQuestionId() > 0;
    }

}
