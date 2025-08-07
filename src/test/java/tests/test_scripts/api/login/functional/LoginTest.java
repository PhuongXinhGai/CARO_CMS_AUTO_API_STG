package tests.test_scripts.api.login.functional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.ExcelUtils;
import static common.utilities.Constants.LOGIN_ENDPOINT;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.test_config.TestConfig;

import java.io.IOException;
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

    // Thêm tham số "expectedValidationData" vào cuối
    @Test(dataProvider = "loginData")
    public void testLogin(String tc_id, String tc_description, String expected_result, String user_name, String password, String actual_result, String log_result, String expectedValidationData) throws IOException {

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/login/login_request_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String requestBody = requestBodyTemplate
                .replace("${userName}", user_name)
                .replace("${password}", password);

        // --- GỬI REQUEST VÀ LẤY RESPONSE ---
        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_URL + LOGIN_ENDPOINT)
                .then()
                .log().all()
                .extract().response();

        // --- KIỂM TRA STATUS CODE ---
        if (expected_result.equalsIgnoreCase("success")) {
            assertEquals(response.getStatusCode(), 200, "TC_ID: " + tc_id + " - Expected status code 200 for successful login");
        } else if (expected_result.equalsIgnoreCase("failed")) {
            assertTrue(response.getStatusCode() == 400 || response.getStatusCode() == 500,
                    "TC_ID: " + tc_id + " - Expected status code 400 or 500 for failed login");
        }

        // --- LOGIC MỚI: KIỂM TRA DỮ LIỆU BÊN TRONG RESPONSE ---
        // Chỉ thực hiện khi cột expectedValidationData có dữ liệu
        if (expectedValidationData != null && !expectedValidationData.isEmpty()) {
            JsonPath actualResponseJson = response.jsonPath();

            // Dùng Gson để parse chuỗi JSON trong Excel thành một Map
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> expectedDataMap = gson.fromJson(expectedValidationData, type);

            // Vòng lặp qua từng cặp key-value muốn kiểm tra
            for (Map.Entry<String, Object> entry : expectedDataMap.entrySet()) {
                String keyPath = entry.getKey();
                Object expectedValue = entry.getValue();

                if (keyPath.equalsIgnoreCase("status_code")) {
                    // Ép kiểu giá trị mong đợi (Double) về số nguyên (Integer) để so sánh
                    int expectedStatusCode = ((Double) expectedValue).intValue();
                    assertEquals(response.getStatusCode(), expectedStatusCode,
                            "TC_ID: " + tc_id + " - Status code mismatch.");

                    // Dùng 'continue' để bỏ qua các bước so sánh chuỗi bên dưới và đi đến key tiếp theo
                    continue;
                }

                Object actualValue = actualResponseJson.get(keyPath);

                System.out.println("Validating: Key='" + keyPath + "', Expected='" + expectedValue + "', Actual='" + actualValue + "'");

                // Xử lý các trường hợp đặc biệt
                if (expectedValue.toString().equalsIgnoreCase("NOT_NULL")) {
                    assertNotNull(actualValue, "TC_ID: " + tc_id + " - Key '" + keyPath + "' should not be null.");
                } else {
                    // So sánh giá trị thông thường
                    // Chuyển tất cả về String để so sánh cho an toàn (tránh lỗi do kiểu dữ liệu: 1.0 vs 1)
                    assertEquals(String.valueOf(actualValue), String.valueOf(expectedValue),
                            "TC_ID: " + tc_id + " - Mismatch for key '" + keyPath + "'");
                }
            }
        }
    }
}