package TestObject;

import PageObject.P02_LoginPage;
import api.client.OrderJournalingApi;
import api.models.ApiEnvelope;
import api.models.OrderJournalingRequestPayload;
import api.models.OrderJournalingSubmission;
import api.utils.ApiConfig;
import api.utils.AuthTokenProvider;
import api.utils.RetryUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.AllureUtils;
import utils.AssertUtils;
import utils.ScreenshotUtils;

import java.util.List;

public class T21_E2E_OrderJournalingValidation extends BaseTest {
    private static final String DEFAULT_NOTE = "Note I expect 1 Million Profit";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void userCanSubmitOrderJournalingAndValidateItViaApi() {
        FlowLogger.step("JOURNALING_TEST", "Starting trade journaling E2E validation");
        FlowLogger.step("JOURNALING_TEST", "Environment=" + ApiConfig.getEnvironment());

        String token = AuthTokenProvider.getAuthToken();
        FlowLogger.step("JOURNALING_TEST", "API authentication completed");

        new StartupFlow().completeStartupToLogin();
        new LoginFlow().loginFromCurrentScreen(
                System.getProperty("login.username", ApiConfig.getUsername()),
                System.getProperty("login.password", ApiConfig.getPassword())
        );
        Assert.assertTrue(new P02_LoginPage().isHomeDisplayed(), "Expected user to be on the home screen after login");

        OrderJournalingFlow journalingFlow = new OrderJournalingFlow();
        String requestedNote = System.getProperty("journaling.note", DEFAULT_NOTE);
        JournalingSubmission uiSubmission = captureUiSubmission(journalingFlow, requestedNote);

        validateUiSubmission(uiSubmission);
        FlowLogger.step("JOURNALING_TEST", "UI journaling flow completed. " + uiSubmission.toDebugString());
        AllureUtils.attachText("UI journaling submission", uiSubmission.toDebugString());

        OrderJournalingApi journalingApi = new OrderJournalingApi();
        ApiEnvelope<OrderJournalingSubmission> envelope = RetryUtils.retry(
                () -> journalingApi.getByOrderId(token, uiSubmission.getOrderId()),
                3,
                2000
        );

        FlowLogger.step("JOURNALING_TEST", "Received API response for orderId=" + uiSubmission.getOrderId());
        AllureUtils.attachJson("Order journaling API response", serialize(envelope));
        validateJournalingResponse(uiSubmission, envelope);
        journalingFlow.validateNoJournalingEntryPointForPendingOrder(uiSubmission.getOrderId());
    }

    private JournalingSubmission captureUiSubmission(OrderJournalingFlow journalingFlow, String note) {
        try {
            FlowLogger.step("JOURNALING_TEST", "Attempting UI journaling flow. note=" + note);
            JournalingSubmission submission = RetryUtils.retry(
                    () -> journalingFlow.submitForFirstOrderWithoutJournaling(note),
                    2,
                    1500
            );
            FlowLogger.step("JOURNALING_TEST", "UI journaling flow finished. existingJournaling=" + submission.isExistingJournaling());
            return submission;
        } catch (RuntimeException | AssertionError exception) {
            attachDiagnostics("ui-journaling-failure", exception);
            throw exception;
        }
    }

    @Step("Validate journaling response")
    private void validateJournalingResponse(JournalingSubmission uiSubmission, ApiEnvelope<OrderJournalingSubmission> envelope) {
        Assert.assertNotNull(uiSubmission, "UI journaling submission must not be null before validating API response");
        Assert.assertNotNull(uiSubmission.getOrderId(), "UI journaling orderId must not be null before validating API response");
        Assert.assertFalse(uiSubmission.getOrderId().isBlank(), "UI journaling orderId must not be blank before validating API response");
        Assert.assertNotNull(envelope, "Journaling API envelope must not be null");
        Assert.assertNotNull(envelope.getData(), "Journaling API data must not be null");

        OrderJournalingSubmission submission = envelope.getData();
        submission.hydrateRequestIfMissing();
        Assert.assertNotNull(submission.getRequest(), "Journaling submission.request must not be null");
        FlowLogger.step("JOURNALING_TEST", "Validating API payload against UI data. ui=" + uiSubmission.toDebugString()
                + ", api=" + serialize(submission));

        SoftAssert softAssert = new SoftAssert();
        OrderJournalingRequestPayload request = submission.getRequest();

        try {
            softAssert.assertEquals(String.valueOf(request.getOrderId()), uiSubmission.getOrderId(),
                    "API request orderId mismatch. expected=" + uiSubmission.getOrderId()
                            + ", actual=" + request.getOrderId());

            if (uiSubmission.getNote() != null && !uiSubmission.getNote().isBlank()) {
                softAssert.assertEquals(request.getNote(), uiSubmission.getNote(),
                        "API note mismatch. expected='" + uiSubmission.getNote() + "', actual='" + request.getNote() + "'");
            }

            if ("BUY".equalsIgnoreCase(uiSubmission.getOrderType()) && uiSubmission.getConfidence() != null) {
                softAssert.assertEquals(request.getConfidence(), toApiConfidence(uiSubmission.getConfidence()),
                        "API confidence mismatch. expected=" + toApiConfidence(uiSubmission.getConfidence())
                                + ", actual=" + request.getConfidence());
                softAssert.assertEquals(AssertUtils.normalize(resolveDecisionFactors(request)),
                        AssertUtils.normalize(uiSubmission.getDecisionFactors()),
                        "API decision factors mismatch. expected=" + uiSubmission.getDecisionFactors()
                                + ", actual=" + resolveDecisionFactors(request));
            }

            if (submission.getOrderId() != null) {
                softAssert.assertEquals(String.valueOf(submission.getOrderId()), uiSubmission.getOrderId(),
                        "Top-level API orderId mismatch. expected=" + uiSubmission.getOrderId()
                                + ", actual=" + submission.getOrderId());
            }

            if (uiSubmission.getNote() != null && !uiSubmission.getNote().isBlank() && submission.getNote() != null) {
                softAssert.assertEquals(submission.getNote(), uiSubmission.getNote(),
                        "Top-level API note mismatch. expected='" + uiSubmission.getNote()
                                + "', actual='" + submission.getNote() + "'");
            }

            FlowLogger.step("JOURNALING_TEST", "Validated journaling API response for orderId=" + uiSubmission.getOrderId());
        } finally {
            softAssert.assertAll();
        }
    }

    private void validateUiSubmission(JournalingSubmission uiSubmission) {
        Assert.assertNotNull(uiSubmission, "UI journaling submission should not be null");
        Assert.assertNotNull(uiSubmission.getOrderId(), "UI journaling orderId should not be null");
        Assert.assertFalse(uiSubmission.getOrderId().isBlank(), "UI journaling orderId should not be blank");
        Assert.assertNotNull(uiSubmission.getOrderType(), "UI journaling orderType should not be null");
        FlowLogger.step("JOURNALING_TEST", "Validated UI submission preconditions. " + uiSubmission.toDebugString());
    }

    private List<String> resolveDecisionFactors(OrderJournalingRequestPayload request) {
        if (request.getDecisionFactors() != null && !request.getDecisionFactors().isEmpty()) {
            return request.getDecisionFactors();
        }
        if (request.getInfluence() != null && !request.getInfluence().isBlank()) {
            return List.of(request.getInfluence());
        }
        return List.of();
    }

    private Integer toApiConfidence(String confidence) {
        if (confidence == null || confidence.isBlank()) {
            return null;
        }
        return "CONFIDENT".equalsIgnoreCase(confidence) ? 1 : 0;
    }

    private String serialize(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }

    private void attachDiagnostics(String label, Throwable exception) {
        FlowLogger.step("JOURNALING_TEST", "Capturing diagnostics for " + label + ": " + exception.getMessage());
        AllureUtils.attachText(label + " exception", exception.toString());
        var screenshot = ScreenshotUtils.capture(label);
        AllureUtils.attachFile(label + " screenshot", screenshot, "image/png");
    }
}
