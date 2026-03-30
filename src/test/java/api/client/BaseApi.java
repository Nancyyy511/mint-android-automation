package api.client;

import api.utils.ApiRequestResponseFilter;
import core.config.ConfigManager;
import core.device.DeviceInfo;
import core.device.DeviceManager;
import core.exceptions.ApiException;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public abstract class BaseApi {
    private final RequestSpecification baseSpec = new RequestSpecBuilder()
            .setBaseUri(ConfigManager.getBaseUrl())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addHeader("lang", ConfigManager.getOptional("api.lang", "en"))
            .addHeader("Accept-Language", ConfigManager.getOptional("api.acceptLanguage", "en-US"))
            .addHeader("imei", resolveImei())
            .addHeader("min-ios-version", ConfigManager.getOptional("api.minIosVersion", "1018"))
            .addFilter(new RequestLoggingFilter())
            .addFilter(new ResponseLoggingFilter())
            .addFilter(new ApiRequestResponseFilter())
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

    protected void assertStatus(Response response, String apiName, int... expectedStatusCodes) {
        int actualStatusCode = response.statusCode();
        for (int expectedStatusCode : expectedStatusCodes) {
            if (actualStatusCode == expectedStatusCode) {
                return;
            }
        }
        throw new ApiException(apiName + " returned unexpected status code " + actualStatusCode
                + ". Expected one of " + java.util.Arrays.toString(expectedStatusCodes)
                + ". Response body: " + response.asString());
    }

    private static String resolveImei() {
        String udidOverride = ConfigManager.getUdidOverride();
        if (!udidOverride.isBlank()) {
            return udidOverride;
        }

        String device = ConfigManager.getDevicePreference();
        if ("real".equals(device)) {
            return DeviceManager.getRealDevices().stream()
                    .findFirst()
                    .map(DeviceInfo::udid)
                    .orElse("");
        }
        if ("auto".equals(device)) {
            try {
                return DeviceManager.getAvailableDevice().udid();
            } catch (RuntimeException ignored) {
            }
        }
        return DeviceManager.getEmulators().stream()
                .findFirst()
                .map(DeviceInfo::udid)
                .orElseGet(() -> ConfigManager.getOptional("api.imei", ""));
    }
}
