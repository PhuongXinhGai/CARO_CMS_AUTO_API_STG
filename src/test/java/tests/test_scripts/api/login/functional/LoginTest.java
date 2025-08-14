package tests.test_scripts.api.login.functional;

// --- BƯỚC 1: THÊM CÁC IMPORT CẦN THIẾT CHO VIỆC GHI LOG VÀ BÁO CÁO ---
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.ExcelUtils; // <-- Giữ nguyên import của bạn
import static common.utilities.Constants.LOGIN_ENDPOINT; // <-- Giữ nguyên import của bạn

import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter; // <-- Thư viện để lọc và ghi lại request
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.io.output.WriterOutputStream; // <-- Thư viện hỗ trợ tạo stream để ghi log
import org.testng.ITestContext;
import org.testng.ITestResult; // <-- Đối tượng chứa kết quả test
import org.testng.Reporter; // <-- Cầu nối để đính kèm dữ liệu vào kết quả test
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

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

public class LoginTest extends TestConfig {

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/login/Login.xlsx";
        return ExcelUtils.getTestData(filePath, "testcase");
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String tc_id, String tc_description, String expected_result, String user_name, String password, String expectedValidationData, ITestContext context) throws IOException {

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/login/login_request_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String requestBody = requestBodyTemplate
                .replace("${userName}", user_name)
                .replace("${password}", password);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + LOGIN_ENDPOINT)
                .then()
//                .log().all()
                .extract().response();

        // --- Logic lưu token và các thông tin quan trọng ---
        if (expected_result.equalsIgnoreCase("success") && response.getStatusCode() == 200) {
            JsonPath responseJson = response.jsonPath();

            // 1. Lấy và lưu token (như cũ)
            String token = responseJson.getString("token");
            if (token != null && !token.isEmpty()) {
                context.setAttribute("AUTH_TOKEN", token);
                System.out.println("Đã lấy và lưu token thành công từ test case: " + tc_id);
            }

            // --- PHẦN NÂNG CẤP: LẤY VÀ LƯU UID ---
            // Giả sử response login trả về partner_uid và course_uid trong object "data"
            String partnerUid = responseJson.getString("data.partner_uid");
            String courseUid = responseJson.getString("data.course_uid");
            String userName = responseJson.getString("data.user_name");

            if (partnerUid != null && !partnerUid.isEmpty() && courseUid != null && !courseUid.isEmpty()) {
                context.setAttribute("PARTNER_UID", partnerUid);
                context.setAttribute("COURSE_UID", courseUid);
                context.setAttribute("USER_NAME", userName);
                System.out.println("Đã lấy và lưu Partner UID: " + partnerUid + " & Course UID: " + courseUid + " & User Name: " + userName + " từ test case: " + tc_id);
            }
        }

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