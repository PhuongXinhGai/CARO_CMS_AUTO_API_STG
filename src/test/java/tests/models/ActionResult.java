package tests.models;

import io.restassured.response.Response;

public class ActionResult {
    private Response response;
    private String requestLog;

    public ActionResult(Response response, String requestLog) {
        this.response = response;
        this.requestLog = requestLog;
    }

    public Response getResponse() {
        return response;
    }

    public String getRequestLog() {
        return requestLog;
    }
}