package tests.test_scripts.api.booking.functional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.ExcelUtils;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.io.output.WriterOutputStream;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.test_config.TestConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static common.utilities.Constants.QUOTE_FEE_ENDPOINT;
import common.utilities.DynamicDataHelper;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class QuoteFeeTest extends TestConfig {

    private static final String AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiIzNDA5ZmZkMy02MjkzLTRkNjAtOTdiMi1lMTFkYTgyMmEyZGQiLCJwYXJ0bmVyX3VpZCI6IkNISS1MSU5IIiwiY291cnNlX3VpZCI6IkNISS1MSU5ILTAxIiwidXNlcl9uYW1lIjoicGh1b25ndHQtY2hpbGluaDAxIiwic3RhdHVzIjoiRU5BQkxFIiwicHdkX2V4cGlyZWRfYXQiOjE3NjEyMTQ5MTQsInJvbGVfdWlkIjozLCJyb2xlX25hbWUiOiJDSEktTElOSCIsImV4cCI6MTc1NTQzMDU5M30.7r58oh5v6IBrQtPtXsbioR2OHYtKErqXTMF8Hh_0qv0";

    @DataProvider(name = "quoteFeeData")
    public Object[][] getQuoteFeeData() {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/Booking_Quote_Fee.xlsx";
        return ExcelUtils.getTestData(filePath, "testcase");
    }

    @Test(dataProvider = "quoteFeeData")
    public void testLogin(String tc_id, String tc_description, String expected_result, String booking_date, String agency_id, String list_player_json, String expectedValidationData) throws IOException {

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);
        // Tạo một StringWriter để hoạt động như một bộ đệm, lưu lại log dưới dạng chuỗi.
        StringWriter requestWriter = new StringWriter();
        // Tạo một PrintStream để RestAssured có thể ghi log vào đó.
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- Xử lý dữ liệu động (ví dụ: {{TODAY}}) ---
        String resolvedBookingDate = DynamicDataHelper.resolveDynamicValue(booking_date);
        // Do list_player_json là một chuỗi JSON, chúng ta cũng cần xử lý các biến bên trong nó
        String resolvedListPlayerJson = DynamicDataHelper.resolveDynamicValue(list_player_json);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/booking/quote_fee_request_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String requestBody = requestBodyTemplate
                .replace("${bookingDate}", booking_date)
                .replace("${agencyId}", agency_id)
                .replace("${listPlayerJson}",list_player_json);

        Response response = given()
                .header("Authorization", AUTH_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + QUOTE_FEE_ENDPOINT)
                .then()
                .log().all()
                .extract().response();

        ITestResult currentResult = Reporter.getCurrentTestResult();
        currentResult.setAttribute("requestLog", requestWriter.toString());
        currentResult.setAttribute("responseLog", response.getBody().prettyPrint());

        if (expectedValidationData != null && !expectedValidationData.isEmpty()) {
            JsonPath actualResponseJson = response.jsonPath();
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> expectedDataMap = gson.fromJson(expectedValidationData, type);

            for (Map.Entry<String, Object> entry : expectedDataMap.entrySet()) {
                String keyPath = entry.getKey();
                Object expectedValue = entry.getValue();

                if (keyPath.equalsIgnoreCase("status_code")) {
                    int expectedStatusCode = ((Double) expectedValue).intValue();
                    assertEquals(response.getStatusCode(), expectedStatusCode, "TC_ID: " + tc_id + " - Status code mismatch.");
                    continue;
                }

                Object actualValue = actualResponseJson.get(keyPath);
                if (expectedValue.toString().equalsIgnoreCase("NOT_NULL")) {
                    assertNotNull(actualValue, "TC_ID: " + tc_id + " - Key '" + keyPath + "' should not be null.");
                } else {
                    assertEquals(String.valueOf(actualValue), String.valueOf(expectedValue), "TC_ID: " + tc_id + " - Mismatch for key '" + keyPath + "'");
                }
            }
        }
    }
}