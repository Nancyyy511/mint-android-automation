package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderJournalingRequestPayload {
    private Long orderId;
    private Integer confidence;
    private String influence;
    private String note;
    private List<String> decisionFactors;

    public OrderJournalingRequestPayload() {
    }

    public OrderJournalingRequestPayload(Long orderId, Integer confidence, String influence, String note) {
        this.orderId = orderId;
        this.confidence = confidence;
        this.influence = influence;
        this.note = note;
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
}
