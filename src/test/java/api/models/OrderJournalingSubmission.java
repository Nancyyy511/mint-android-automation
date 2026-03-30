package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderJournalingSubmission {
    private OrderJournalingRequestPayload request;
    private Long orderId;
    private Integer confidence;
    private String influence;
    private String note;
    private List<String> decisionFactors;

    public OrderJournalingRequestPayload getRequest() {
        return request;
    }

    public void setRequest(OrderJournalingRequestPayload request) {
        this.request = request;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public String getInfluence() {
        return influence;
    }

    public void setInfluence(String influence) {
        this.influence = influence;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getDecisionFactors() {
        return decisionFactors;
    }

    public void setDecisionFactors(List<String> decisionFactors) {
        this.decisionFactors = decisionFactors;
    }

    public void hydrateRequestIfMissing() {
        if (request != null) {
            return;
        }

        if (orderId == null && influence == null && note == null && confidence == null
                && (decisionFactors == null || decisionFactors.isEmpty())) {
            return;
        }

        OrderJournalingRequestPayload hydrated = new OrderJournalingRequestPayload();
        hydrated.setOrderId(orderId);
        hydrated.setConfidence(confidence);
        hydrated.setInfluence(influence);
        hydrated.setNote(note);
        hydrated.setDecisionFactors(decisionFactors);
        this.request = hydrated;
    }
}
