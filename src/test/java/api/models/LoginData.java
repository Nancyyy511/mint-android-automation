package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginData {
    private Integer userId;

    @JsonProperty("guestToken")
    private String guestToken;

    @JsonProperty("hasSecurityQuestions")
    private boolean hasSecurityQuestions;

    @JsonProperty("isPinCreated")
    private boolean pinCreated;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
    }

    public boolean isHasSecurityQuestions() {
        return hasSecurityQuestions;
    }

    public void setHasSecurityQuestions(boolean hasSecurityQuestions) {
        this.hasSecurityQuestions = hasSecurityQuestions;
    }

    public boolean isPinCreated() {
        return pinCreated;
    }

    public void setPinCreated(boolean pinCreated) {
        this.pinCreated = pinCreated;
    }
}
