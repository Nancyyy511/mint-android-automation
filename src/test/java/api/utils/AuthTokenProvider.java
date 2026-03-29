package api.utils;

import api.client.AuthApi;

public final class AuthTokenProvider {
    private AuthTokenProvider() {
    }

    public static String getAuthToken() {
        String explicitToken = System.getProperty("api.auth.token", "").trim();
        if (!explicitToken.isEmpty()) {
            return explicitToken;
        }
        return new AuthApi().authenticateAndGetToken(ApiConfig.getUsername(), ApiConfig.getPassword());
    }
}
