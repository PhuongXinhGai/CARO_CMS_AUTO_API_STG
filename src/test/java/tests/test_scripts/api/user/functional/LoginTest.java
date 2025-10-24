package tests.test_scripts.api.user.functional;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.ExcelUtils;
import common.utilities.RequestLogger;
import common.utilities.StringUtils;
import helpers.ReportHelper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.io.output.WriterOutputStream;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.test_config.TestConfig;
import framework.core.FlowRunnable;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class LoginTest extends TestConfig implements FlowRunnable {

    // ===== cấu hình đường dẫn (đổi theo project của bạn) =====
    private static final String EXCEL_FILE   = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/user/Login.xlsx";
    private static final String SHEET_NAME   = "testcase";
    private static final String JSON_DIR     = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/user/login/"; // chứa login_request.json, login_expect.json


    @DataProvider(name = "loginData")
    public Object[][] getLoginData() throws IOException {
        return ExcelUtils.readSheetAsMaps(EXCEL_FILE, SHEET_NAME);
    }

    /**
     * Cột Excel (gợi ý):
     * tc_id, tc_description, expected_result, input_placeholders, user_name, password,
     * expected_validation_data, status_code, assert_token, assert_data.user_name,
     * assert_data.partner_uid, assert_data.course_uid
     */
    @Test(dataProvider = "loginData")
    public void testLogin(Map<String, String> row, ITestContext ctx) throws Exception {
        final String tcId = row.getOrDefault("tc_id", "NO_ID");
        final String desc = row.getOrDefault("tc_description", "Create booking batch");

        System.out.println("Running: " + tcId + " - " + desc);

        // ===== Step 1: Chuẩn bị log =====
        StringWriter reqWriter = new StringWriter();
        PrintStream  reqCapture = new PrintStream(new WriterOutputStream(reqWriter), true);

        // ===== Step 2: Build request =====
        String reqFileName = row.getOrDefault("input_placeholders", "");
        String reqTpl = Files.readString(Paths.get(JSON_DIR + reqFileName));
        String requestBody = StringUtils.replacePlaceholdersInString(reqTpl, row); // thay tất cả ${colName}

        System.out.println("🧩 Request body sau replace:\n" + requestBody);

        // ===== Step 3: Call API =====
        Response resp = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCapture))
//                .filter(new RequestLogger(reqCapture))
                .when()
                .post(BASE_URL + "/golf-cms/api/user/login-plain")
                .then()
                .extract().response();

        String respJson = resp.asString();

        // ===== Step 4: Gắn log request/response vào báo cáo =====
        reqCapture.flush();
        ITestResult tr = Reporter.getCurrentTestResult();
        tr.setAttribute("requestLog",  reqWriter.toString());
        tr.setAttribute("responseLog", resp.getBody().prettyPrint());
        ctx.setAttribute("LAST_REQUEST_LOG", reqWriter.toString());
        ctx.setAttribute("LAST_RESPONSE_LOG", resp.getBody().prettyPrint());

        // ===== Step 5: Load expect JSON =====
        String expectFileName = row.getOrDefault("expected_validation_data", "");
        String expectRaw = Files.readString(Paths.get(JSON_DIR + expectFileName));

        // ===== Step 6: Thay placeholder trong expect =====
        String expectResolved = StringUtils.replacePlaceholdersInString(expectRaw, row);
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> expectJson = gson.fromJson(expectResolved, mapType);

        // ===== Step 7: So sánh actual vs expect (AssertionHelper) =====
        AssertionHelper.verifyStatusCode(resp, expectJson);
        AssertionHelper.assertFromJson(respJson, expectJson);

        // ===== Step 8: Lưu biến dùng cho step sau (nếu cần integration) =====
        // JsonPath jp = new JsonPath(respJson);
        // ctx.setAttribute("BOOKING_CODE_0", jp.getString("[0].booking_code"));

        JsonPath jp = resp.jsonPath();
        String token      = jp.getString("token");
        String partnerUid = jp.getString("data.partner_uid");
        String courseUid  = jp.getString("data.course_uid");
        String userNameRp = jp.getString("data.user_name");
        String full_name = jp.getString("data.full_name");

        if (token != null)      ctx.setAttribute("AUTH_TOKEN", token);
        if (partnerUid != null) ctx.setAttribute("PARTNER_UID", partnerUid);
        if (courseUid != null)  ctx.setAttribute("COURSE_UID", courseUid);
        if (userNameRp != null) ctx.setAttribute("USER_NAME",  userNameRp);
        if (full_name != null)  ctx.setAttribute("FULL_NAME",  full_name);
    }

//    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running Login case: " + caseId);
        testLogin(row, ctx);   // chỉ gọi lại hàm test cũ
    }

//    Hiển thị tất cả các trường lưu trong context
    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }
}
