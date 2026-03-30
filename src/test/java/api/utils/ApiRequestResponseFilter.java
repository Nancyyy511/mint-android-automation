package api.utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class ApiRequestResponseFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        String request = requestSpec.getMethod() + " " + requestSpec.getURI()
                + System.lineSeparator() + "Headers: " + requestSpec.getHeaders()
                + System.lineSeparator() + "Body: " + valueOrNone(requestSpec.getBody());

        Response response = context.next(requestSpec, responseSpec);
        String responsePayload = "Status: " + response.statusCode()
                + System.lineSeparator() + "Body: " + response.asString();

        ApiLogContext.add(request, responsePayload);
        return response;
    }

    private String valueOrNone(Object value) {
        return value == null ? "<none>" : value.toString();
    }
}
