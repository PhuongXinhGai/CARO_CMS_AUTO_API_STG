package common.utilities;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.io.PrintStream;
import java.util.Map;

public class RequestLogger implements Filter {

    private final PrintStream stream;

    public RequestLogger(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public Response filter(FilterableRequestSpecification reqSpec,
                           FilterableResponseSpecification resSpec,
                           FilterContext ctx) {

        try {
            stream.println("===== REQUEST START =====");

            // Method + URL (có path param)
            String url = reqSpec.getURI();

            // Nếu có query param → append vào cuối URL
            if (reqSpec.getQueryParams() != null && !reqSpec.getQueryParams().isEmpty()) {
                StringBuilder queryStr = new StringBuilder("?");
                for (Map.Entry<String, String> entry : reqSpec.getQueryParams().entrySet()) {
                    queryStr.append(entry.getKey()).append("=")
                            .append(entry.getValue()).append("&");
                }
                url = url + queryStr.substring(0, queryStr.length() - 1);
            }

            stream.println(reqSpec.getMethod() + " " + url);

            // Header
            stream.println("Headers:");
            reqSpec.getHeaders().forEach(h ->
                    stream.println("  " + h.getName() + ": " + h.getValue()));

            // Path params (nếu có)
            if (reqSpec.getPathParams() != null && !reqSpec.getPathParams().isEmpty()) {
                stream.println("Path Params: " + reqSpec.getPathParams());
            }

            // Query params (in rõ ràng hơn)
            if (reqSpec.getQueryParams() != null && !reqSpec.getQueryParams().isEmpty()) {
                stream.println("Query Params: " + reqSpec.getQueryParams());
            }

            // Body (nếu có)
            Object body = reqSpec.getBody();
            if (body != null) {
                stream.println("Body:");
                stream.println(body.toString());
            }

            stream.println("===== REQUEST END =====");
            stream.flush();

        } catch (Exception e) {
            stream.println("⚠️ Error logging request: " + e.getMessage());
        }

        // Tiếp tục chain call tới API
        return ctx.next(reqSpec, resSpec);
    }
}
