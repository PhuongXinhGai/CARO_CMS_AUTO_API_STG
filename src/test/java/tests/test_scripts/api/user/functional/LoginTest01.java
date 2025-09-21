package tests.test_scripts.api.user.functional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
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

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class LoginTest01 extends TestConfig {

    // Đường dẫn gốc để bạn dễ đổi sau này
    private static final String EXCEL_PATH = "/src/main/resources/input_excel_file/user/Login.xlsx";
    private static final String JSON_BASE  = "/src/main/resources/input_json_file/user/login/"; // thư mục chứa json

    @DataProvider(name = "loginData01")
    public Object[][] getLoginData01() {
        String filePath = System.getProperty("user.dir") + EXCEL_PATH;
        // Sheet name: "testcase" theo ảnh của bạn
        return ExcelUtils.getTestData(filePath, "testcase_v2");
    }

    /**
     * Mapping cột theo Excel hiện tại của bạn:
     * tc_id, tc_description, expected_result, input_placeholders,
     * user_name, password, expected_validation_data, status_code,
     * assert_token, assert_data.user_name, assert_data.partner_uid, assert_data.course_uid
     */
    @Test(dataProvider = "loginData01")
    public void testLogin01(String tc_id,
                          String tc_description,
                          String expected_result,
                          String input_placeholders,       // ví dụ: login_request.json
                          String user_name,
                          String password,
                          String expected_validation_data, // ví dụ: login_expect.json
                          String status_code,
                          String assert_token,
                          String assert_data_user_name,
                          String assert_data_partner_uid,
                          String assert_data_course_uid,
                          ITestContext context) throws IOException {

        System.out.println("Running: " + tc_id + " - " + tc_description);

        // ====== 1) Chuẩn bị log request ======
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // ====== 2) Đọc & build request body từ template ======
        String requestTplPath = System.getProperty("user.dir") + JSON_BASE + input_placeholders;
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(requestTplPath)));
        // Placeholder trong request: ${user_name}, ${password}
        String requestBody = requestBodyTemplate
                .replace("${user_name}", user_name)
                .replace("${password}", password);

        // ====== 3) Gọi API ======
        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + "/golf-cms/api/user/login")
                .then()
                .extract().response();

        String responseJson = response.asString();

        // ====== 4) Load expect JSON và thay placeholder ======
        String expectPath = System.getProperty("user.dir") + JSON_BASE + expected_validation_data;
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> expectJson = gson.fromJson(new FileReader(expectPath), type);

        // Build context để replace placeholders trong expect
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("status_code", tryParseInt(status_code, 200));
        ctx.put("assert_token", assert_token);
        ctx.put("assert_data.user_name", assert_data_user_name);
        ctx.put("assert_data.partner_uid", assert_data_partner_uid);
        ctx.put("assert_data.course_uid", assert_data_course_uid);

        replacePlaceholders(expectJson, ctx);

        // ====== 5) Nghiệm thu bằng AssertionHelper (implicit + explicit) ======
        AssertionHelper.assertFromJson(responseJson, expectJson);

        // ====== 6) Lưu biến dùng cho step sau (nếu cần) ======
        if ("success".equalsIgnoreCase(expected_result) && response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            String token      = jp.getString("data.token");
            String partnerUid = jp.getString("data.partner_uid");
            String courseUid  = jp.getString("data.course_uid");
            String userNameRp = jp.getString("data.user_name");

            if (token != null)      context.setAttribute("AUTH_TOKEN", token);
            if (partnerUid != null) context.setAttribute("PARTNER_UID", partnerUid);
            if (courseUid != null)  context.setAttribute("COURSE_UID", courseUid);
            if (userNameRp != null) context.setAttribute("USER_NAME", userNameRp);
        }

        // ====== 7) Gắn log request/response vào report ======
        ITestResult currentResult = Reporter.getCurrentTestResult();
        currentResult.setAttribute("requestLog", requestWriter.toString());
        currentResult.setAttribute("responseLog", response.getBody().prettyPrint());
    }

    // ===== Helpers =====
    private int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    // Replace ${var} trong Map (expect JSON) bằng giá trị context
    @SuppressWarnings("unchecked")
    private void replacePlaceholders(Map<String, Object> node, Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : node.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof String) {
                String str = (String) value;
                if (str.startsWith("${") && str.endsWith("}")) {
                    String key = str.substring(2, str.length() - 1);
                    if (context.containsKey(key)) entry.setValue(context.get(key));
                }
            } else if (value instanceof Map) {
                replacePlaceholders((Map<String, Object>) value, context);
            } else if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value) {
                    if (item instanceof Map) {
                        replacePlaceholders((Map<String, Object>) item, context);
                    }
                }
            }
        }
    }
}
