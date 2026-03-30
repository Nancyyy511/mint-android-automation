package TestObject;

import java.util.ArrayList;
import java.util.List;

public class JournalingSubmission {
    private String orderId;
    private String orderType;
    private String confidence;
    private List<String> decisionFactors = new ArrayList<>();
    private String expectationOutcome;
    private String note;
    private boolean existingJournaling;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public List<String> getDecisionFactors() {
        return decisionFactors;
    }

    public void setDecisionFactors(List<String> decisionFactors) {
        this.decisionFactors = decisionFactors == null ? new ArrayList<>() : new ArrayList<>(decisionFactors);
    }

    public String getExpectationOutcome() {
        return expectationOutcome;
    }

    public void setExpectationOutcome(String expectationOutcome) {
        this.expectationOutcome = expectationOutcome;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isExistingJournaling() {
        return existingJournaling;
    }

    public void setExistingJournaling(boolean existingJournaling) {
        this.existingJournaling = existingJournaling;
    }

    public String toDebugString() {
        return "orderId=" + orderId
                + ", orderType=" + orderType
                + ", confidence=" + confidence
                + ", decisionFactors=" + decisionFactors
                + ", expectationOutcome=" + expectationOutcome
                + ", note=" + note
                + ", existingJournaling=" + existingJournaling;
    }
}
