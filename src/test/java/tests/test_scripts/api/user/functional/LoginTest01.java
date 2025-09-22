package tests.test_scripts.api.user.functional;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.ContextLogger;
import common.utilities.ExcelUtils;
import helpers.ExtentReportManager;
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
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class LoginTest01 extends TestConfig {

    // ===== cấu hình đường dẫn (đổi theo project của bạn) =====
    private static final String EXCEL_FILE   = "/src/main/resources/input_excel_file/user/Login.xlsx";
    private static final String SHEET_NAME   = "testcase_v2";
    private static final String JSON_DIR     = "/src/main/resources/input_json_file/user/login/"; // chứa login_request.json, login_expect.json

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        return ExcelUtils.getTestData(System.getProperty("user.dir") + EXCEL_FILE, SHEET_NAME);
    }

    /**
     * Cột Excel (gợi ý):
     * tc_id, tc_description, expected_result, input_placeholders, user_name, password,
     * expected_validation_data, status_code, assert_token, assert_data.user_name,
     * assert_data.partner_uid, assert_data.course_uid
     */
    @Test(dataProvider = "loginData")
    public void testLogin(String tc_id,
                          String tc_description,
                          String expected_result,
                          String input_placeholders,       // login_request.json
                          String user_name,
                          String password,
                          String expected_validation_data, // login_expect.json
                          String status_code,
                          String assert_token,
                          String assert_data_user_name,
                          String assert_data_partner_uid,
                          String assert_data_course_uid,
                          ITestContext ctx) throws Exception {

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);

        // ===== Step 1: Chuẩn bị log =====
        StringWriter reqWriter = new StringWriter();
        PrintStream  reqCapture = new PrintStream(new WriterOutputStream(reqWriter), true);

        // ===== Step 2: Build request =====
        String reqTplPath = System.getProperty("user.dir") + JSON_DIR + input_placeholders;
        String reqTpl     = Files.readString(Paths.get(reqTplPath));
        // thay placeholder từ Excel vào request
        String requestBody = reqTpl
                .replace("${user_name}", user_name)
                .replace("${password}",  password);

        // ===== Step 3: Call API =====
        Response resp = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCapture))
                .when()
                .post(BASE_URL + "/golf-cms/api/user/login")
                .then()
                .extract().response();

        String respJson = resp.asString();

        // ===== Step 4: Gắn log request/response vào báo cáo =====
        ITestResult tr = Reporter.getCurrentTestResult();
        tr.setAttribute("requestLog",  reqWriter.toString());
        tr.setAttribute("responseLog", resp.getBody().prettyPrint());

        // ===== Step 5: Load expect JSON =====
        String expectPath = System.getProperty("user.dir") + JSON_DIR + expected_validation_data;
        String expectRaw  = Files.readString(Paths.get(expectPath)); // đọc chuỗi để có thể thay placeholder trước khi parse

        // ===== Step 6: Thay placeholder trong expect =====
        Map<String, Object> place = new HashMap<>();
        place.put("status_code",            safeInt(status_code, 200));
        place.put("assert_token",           assert_token);
        place.put("assert_data.user_name",  assert_data_user_name);
        place.put("assert_data.partner_uid",assert_data_partner_uid);
        place.put("assert_data.course_uid", assert_data_course_uid);
        String expectResolved = replacePlaceholdersInString(expectRaw, place);

        // Parse expect sau khi đã thay xong
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> expectJson = gson.fromJson(expectResolved, mapType);

        // ===== Step 7: So sánh actual vs expect (AssertionHelper) =====
        AssertionHelper.assertFromJson(respJson, expectJson);

        // ===== Step 8: Lưu biến dùng cho step sau (nếu cần integration) =====
        if ("success".equalsIgnoreCase(expected_result) && resp.getStatusCode() == 200) {
            JsonPath jp = resp.jsonPath();
            String token      = jp.getString("token");
            String partnerUid = jp.getString("data.partner_uid");
            String courseUid  = jp.getString("data.course_uid");
            String userNameRp = jp.getString("data.user_name");
            if (token != null)      ctx.setAttribute("AUTH_TOKEN", token);
            if (partnerUid != null) ctx.setAttribute("PARTNER_UID", partnerUid);
            if (courseUid != null)  ctx.setAttribute("COURSE_UID", courseUid);
            if (userNameRp != null) ctx.setAttribute("USER_NAME",  userNameRp);
        }

        ExtentTest ext = ExtentReportManager.getTest();
        ContextLogger.logContextAttributes(ctx, ext);

    }

    // ===== helpers =====
    private int safeInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    // thay ${var} trong chuỗi expect
    private String replacePlaceholdersInString(String raw, Map<String, Object> ctx) {
        String out = raw;
        for (Map.Entry<String, Object> e : ctx.entrySet()) {
            out = out.replace("${" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return out;
    }
}
