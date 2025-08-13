package tests.test_scripts.api.booking.functional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.DynamicDataHelper;
import common.utilities.ExcelUtils;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
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
import java.util.Map;

import static common.utilities.Constants.BOOKING_LIST_SELECT_ENDPOINT;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class GetBookingListSelectTest extends TestConfig {
    @DataProvider(name = "getBookingListSelect")
    public Object[][] getQuoteFeeData() {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/booking/Get_Booking_List_Select.xlsx";
        return ExcelUtils.getTestData(filePath, "testcase");
    }

    @Test(dataProvider = "getBookingListSelect")
    public void testLogin(String tc_id, String tc_description, String expected_result, String sort_by, String sort_dir, String booking_date, String is_ignore_tournament_booking, String is_single_book, String expectedValidationData, ITestContext context) throws IOException {
        // --- PHẦN NÂNG CẤP: LẤY TOKEN ĐỘNG TỪ CONTEXT ---
        String authToken = (String) context.getAttribute("AUTH_TOKEN");
        String partnerUid = (String) context.getAttribute("PARTNER_UID");
        String courseUid = (String) context.getAttribute("COURSE_UID");
        assertNotNull(authToken, "Token không được null. Hãy chắc chắn rằng LoginTest đã chạy thành công trước.");
        assertNotNull(partnerUid, "ParterUid không được null. Hãy chắc chắn rằng LoginTest đã chạy thành công trước.");
        assertNotNull(courseUid, "CourseUid không được null. Hãy chắc chắn rằng LoginTest đã chạy thành công trước.");

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);

        // --- Chuẩn bị ghi log (giữ nguyên) ---
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- Xử lý dữ liệu động (ví dụ: {{TODAY}}) ---
        String resolvedBookingDate = DynamicDataHelper.resolveDynamicValue(booking_date);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
//        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/booking/create_booking_batch_template.json";
//        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
//        String requestBody = requestBodyTemplate
//                .replace("${bookingListJson}",resolvedBookingListJson);

        Response response = given()
                .header("Authorization", authToken)
                .queryParam("sort_by", sort_by)
                .queryParam("sort_dir", sort_dir)
                .queryParam("booking_date", resolvedBookingDate)
                .queryParam("is_ignore_tournament_booking", is_ignore_tournament_booking)
                .queryParam("is_single_book", is_single_book)
                // Các param cố định khác có thể thêm ở đây
                .queryParam("partner_uid", partnerUid)
                .queryParam("course_uid", courseUid)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .get(BASE_URL + BOOKING_LIST_SELECT_ENDPOINT)
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