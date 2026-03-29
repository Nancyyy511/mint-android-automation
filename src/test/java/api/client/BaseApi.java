package api.client;

import api.utils.ApiConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public abstract class BaseApi {
    private final RequestSpecification baseSpec = new RequestSpecBuilder()
            .setBaseUri(ApiConfig.getBaseUrl())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addHeader("lang", ApiConfig.getOptional("api.lang", "en"))
            .addHeader("Accept-Language", ApiConfig.getOptional("api.acceptLanguage", "en-US"))
            .addHeader("imei", ApiConfig.getOptional("api.imei", "emulator-5554"))
            .addHeader("min-ios-version", ApiConfig.getOptional("api.minIosVersion", "1018"))
            .addFilter(new RequestLoggingFilter())
            .addFilter(new ResponseLoggingFilter())
            .build();

    protected RequestSpecification request() {
        return io.restassured.RestAssured.given().spec(baseSpec);
    }

    protected RequestSpecification authorizedRequest(String token) {
        return request().header("Authorization", buildBearerToken(token));
    }

    protected String buildBearerToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Auth token must not be blank");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    protected void logRequest(String method, String path) {
        System.out.println("[API][REQUEST] " + method + " " + path);
    }

    protected void logResponse(String path, String responseBody) {
        System.out.println("[API][RESPONSE] " + path + " -> " + responseBody);
    }

    protected void logParsed(String message) {
        System.out.println("[API][PARSED] " + message);
    }
}
