package tests.test_scripts.api.cico;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.ExcelUtils;
import common.utilities.StringUtils;
import framework.core.FlowRunnable;
import helpers.ReportHelper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.io.output.WriterOutputStream;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
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

public class UpdateAgencyPayPlayer4Test extends TestConfig implements FlowRunnable {
    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/CICO.xlsx";
    private static final String SHEET_NAME = "Update_Agency_Pay_Player4";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/cico/update_agency_pay/";

    // ======================= DataProvider =======================
    @DataProvider(name = "updateAgencyPayData")
    public Object[][] updateAgencyPayData() throws IOException {
        return ExcelUtils.readSheetAsMaps(EXCEL_FILE, SHEET_NAME);
    }

    /**
     * 8 STEP:
     * 1) Chuẩn bị log
     * 2) Build request (đọc template + replace placeholder)
     * 3) Call API
     * 4) Gắn log request/response vào report
     * 5) Load expect JSON (raw string)
     * 6) Replace placeholder trong expect
     * 7) So sánh actual vs expect (AssertionHelper)
     * 8) Extract và lưu biến cho step sau (nếu cần)
     */
    @Test(dataProvider = "updateAgencyPayData")
    public void testUpdateAgencyPay(Map<String, String> row, ITestContext ctx) throws IOException {
        final String tcId = row.getOrDefault("tc_id", "NO_ID");
        final String desc = row.getOrDefault("tc_description", "Create booking batch");

        System.out.println("Running: " + tcId + " - " + desc);

        // ===== Step 1: Chuẩn bị log =====
        StringWriter reqWriter = new StringWriter();
        PrintStream reqCapture = new PrintStream(new WriterOutputStream(reqWriter), true);

        // ===== Step 2: Build request =====
        String reqFileName = row.getOrDefault("input_placeholders", "");
        String reqTpl = Files.readString(Paths.get(JSON_DIR + reqFileName));

        String requestBody = StringUtils.replacePlaceholdersAdvanced(reqTpl, row, ctx);
        System.out.println("🧩 Request body sau replace:\n" + requestBody);

        // ===== Step 3: Call API =====
        String tokenFromCtx = (String) ctx.getAttribute("AUTH_TOKEN");
        String tokenFromExcel = row.get("auth_token"); // optional in Excel
        String bearer = tokenFromCtx != null ? tokenFromCtx : tokenFromExcel;

        String booking_uid = (String) ctx.getAttribute("BOOKING_UID_3");


        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .header("Authorization", bearer != null ? bearer : "")
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCapture))
                .when()
                .put(BASE_URL + "/golf-cms/api/booking/" + booking_uid)
                .then()
                .extract().response();

        String respJson = resp.asString();

        // ===== Step 4: Gắn log request/response vào report =====
        reqCapture.flush();
        ITestResult tr = Reporter.getCurrentTestResult();
        tr.setAttribute("requestLog", reqWriter.toString());
        tr.setAttribute("responseLog", resp.getBody().prettyPrint());
        ctx.setAttribute("LAST_REQUEST_LOG", requestBody);
        ctx.setAttribute("LAST_RESPONSE_LOG", resp.asString());

        // ===== Step 5: Load expect JSON =====
        // Excel cột 'expected_validation_data' trỏ tới file expect (vd: create_booking_batch_expect.json)
        String expectFileName = row.getOrDefault("expected_validation_data", "");
        String expectRaw = Files.readString(Paths.get(JSON_DIR + expectFileName));

        // ===== Step 6: Replace placeholder trong expect =====
        // Lưu ý: với boolean (true/false) hãy KHÔNG đặt dấu nháy quanh placeholder trong file expect.
        String expectResolved = StringUtils.replacePlaceholdersInString(expectRaw, row);
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> expectJson = gson.fromJson(expectResolved, mapType);

        // ===== Step 7: So sánh actual vs expect =====
        AssertionHelper.verifyStatusCode(resp, expectJson);
        AssertionHelper.assertFromJson(respJson, expectJson);

        // ===== Step 8: Extract lưu biến cho bước sau (nếu cần) =====
//        // tuỳ nhu cầu: VD lưu booking_code_0, booking_uid_0 (đã định nghĩa trong "extract" của expect)
//        // nếu bạn muốn parse nhanh ở đây, có thể dùng JsonPath đọc lại:
////         JsonPath jp = new JsonPath(respJson);
//        // ctx.setAttribute("BOOKING_CODE_0", jp.getString("[0].booking_code"));
//        JsonPath jp = resp.jsonPath();
//
//        for (int i = 0; i < 4; i++) {
//            String uid            = jp.getString("[" + i + "].uid");
//            String guestStyle     = jp.getString("[" + i + "].guest_style");
//            String guestStyleName = jp.getString("[" + i + "].guest_style_name");
//            String greenFee       = jp.getString("[" + i + "].list_golf_fee[0].green_fee");
//            String caddieFee      = jp.getString("[" + i + "].list_golf_fee[0].caddie_fee");
//            String totalGolfFee   = jp.getString("[" + i + "].mush_pay_info.total_golf_fee");
//
//            if (uid != null)            ctx.setAttribute("BOOKING_UID_" + i, uid);
//            if (guestStyle != null)     ctx.setAttribute("GUEST_STYLE_" + i, guestStyle);
//            if (guestStyleName != null) ctx.setAttribute("GUEST_STYLE_NAME_" + i, guestStyleName);
//            if (greenFee != null)       ctx.setAttribute("GREEN_FEE_" + i, greenFee);
//            if (caddieFee != null)      ctx.setAttribute("CADDIE_FEE_" + i, caddieFee);
//            if (totalGolfFee != null)   ctx.setAttribute("TOTAL_GOLF_FEE_" + i, totalGolfFee);
//        }
    }
    //    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running Login case: " + caseId);
        testUpdateAgencyPay(row, ctx);   // chỉ gọi lại hàm test cũ
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }
}
