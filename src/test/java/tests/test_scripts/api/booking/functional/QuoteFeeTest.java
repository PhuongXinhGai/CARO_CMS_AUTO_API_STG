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
import org.testng.ITestContext;
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



    @DataProvider(name = "quoteFeeData")
    public Object[][] getQuoteFeeData() {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/booking/Booking_Quote_Fee.xlsx";
        return ExcelUtils.getTestData(filePath, "testcase");
    }

    @Test(dataProvider = "quoteFeeData")
    public void testQuoteFee(String tc_id, String tc_description, String expected_result, String partner_uid, String course_uid, String booking_date, String agency_id, String list_player_json, String expectedValidationData, ITestContext context) throws IOException {
        // --- PHẦN NÂNG CẤP: LẤY TOKEN ĐỘNG TỪ CONTEXT ---
        String authToken = (String) context.getAttribute("AUTH_TOKEN");
        assertNotNull(authToken, "Token không được null. Hãy chắc chắn rằng LoginTest đã chạy thành công trước.");

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);
        // Tạo một StringWriter để hoạt động như một bộ đệm, lưu lại log dưới dạng chuỗi.
        StringWriter requestWriter = new StringWriter();
        // Tạo một PrintStream để RestAssured có thể ghi log vào đó.
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- Xử lý dữ liệu động (ví dụ: {{TODAY}}) ---
        String resolvedBookingDate = DynamicDataHelper.resolveDynamicValue(booking_date);
        String resolvedListPlayerJson = DynamicDataHelper.resolveDynamicValue(list_player_json);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/booking/quote_fee_request_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String requestBody = requestBodyTemplate
                .replace("${partnerUid}", partner_uid)
                .replace("${courseUid}", course_uid)
                .replace("${bookingDate}", resolvedBookingDate)
                .replace("${agencyId}", agency_id)
                .replace("${listPlayerJson}",resolvedListPlayerJson);

        Response response = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + QUOTE_FEE_ENDPOINT)
                .then()
//                .log().all()
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

                if (expectedValue instanceof Number && actualValue instanceof Number) {
                    java.math.BigDecimal ev = new java.math.BigDecimal(expectedValue.toString());
                    java.math.BigDecimal av = new java.math.BigDecimal(actualValue.toString());
                    org.testng.Assert.assertEquals(av.compareTo(ev), 0,
                            "TC_ID: " + tc_id + " - Mismatch for numeric key '" + keyPath + "'");
                } else if ("NOT_NULL".equalsIgnoreCase(String.valueOf(expectedValue))) {
                    assertNotNull(actualValue, "TC_ID: " + tc_id + " - Key '" + keyPath + "' should not be null.");
                } else {
                    assertEquals(String.valueOf(actualValue), String.valueOf(expectedValue),
                            "TC_ID: " + tc_id + " - Mismatch for key '" + keyPath + "'");
                }
            }
        }
    }
}