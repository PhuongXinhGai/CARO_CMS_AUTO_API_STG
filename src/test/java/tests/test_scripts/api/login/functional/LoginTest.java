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
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/Login.xlsx";
        return ExcelUtils.getTestData(filePath, "testcase");
    }

    // --- BƯỚC 2: XÓA CÁC THAM SỐ KHÔNG CẦN THIẾT ---
    // Bỏ 'actual_result' và 'log_result' vì báo cáo Extent Reports sẽ thay thế chúng.
    @Test(dataProvider = "loginData")
    public void testLogin(String tc_id, String tc_description, String expected_result, String user_name, String password, String expectedValidationData) throws IOException {

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);

        // --- BƯỚC 3: CHUẨN BỊ "BẪY" ĐỂ GHI LẠI LOG REQUEST ---
        // Tạo một StringWriter để hoạt động như một bộ đệm, lưu lại log dưới dạng chuỗi.
        StringWriter requestWriter = new StringWriter();
        // Tạo một PrintStream để RestAssured có thể ghi log vào đó.
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY (Giữ nguyên logic của bạn) ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/login/login_request_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String requestBody = requestBodyTemplate
                .replace("${userName}", user_name)
                .replace("${password}", password);

        // --- BƯỚC 4: CẬP NHẬT LẠI VIỆC GỬI REQUEST ĐỂ GHI LOG ---
        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + LOGIN_ENDPOINT) // <-- Sử dụng hằng số LOGIN_ENDPOINT của bạn
                .then()
                // .log().all() // <-- Không cần log ra console nữa vì đã có báo cáo
                .extract().response();

        // --- BƯỚC 5: ĐÍNH KÈM CÁC LOG ĐÃ GHI ĐƯỢC VÀO KẾT QUẢ TEST ---
        // Lấy đối tượng kết quả test hiện tại mà TestNG đang chạy
        ITestResult currentResult = Reporter.getCurrentTestResult();
        // Đính kèm log request vào kết quả với tên là "requestLog"
        currentResult.setAttribute("requestLog", requestWriter.toString());
        // Đính kèm response body vào kết quả với tên là "responseLog"
        // Dùng prettyPrint() để format JSON cho đẹp trong báo cáo
        currentResult.setAttribute("responseLog", response.getBody().prettyPrint());

        // --- BƯỚC 6: DỌN DẸP LOGIC ASSERTION CŨ (Quan trọng) ---
        // Xóa khối if/else kiểm tra status code cũ vì logic mới trong vòng lặp đã bao hàm và mạnh mẽ hơn.
        // Điều này giúp tránh việc kiểm tra trùng lặp và làm code sạch hơn.

        // --- KIỂM TRA DỮ LIỆU BÊN TRONG RESPONSE (Giữ nguyên logic của bạn) ---
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