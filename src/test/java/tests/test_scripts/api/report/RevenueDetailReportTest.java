package tests.test_scripts.api.report;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.*;
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
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RevenueDetailReportTest extends TestConfig implements FlowRunnable {
    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/Report.xlsx";
    private static final String SHEET_NAME = "Revenue_Detail_Report";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/report/";

    // ======================= DataProvider =======================
    @DataProvider(name = "revenueDetailReportData")
    public Object[][] revenueDetailReportData() throws IOException {
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
    @Test(dataProvider = "revenueDetailReportData")
    public void testRevenueDetailReport(Map<String, String> row, ITestContext ctx) throws IOException {
        final String tcId = row.getOrDefault("tc_id", "NO_ID");
        final String desc = row.getOrDefault("tc_description", "Get Booking Price");

        System.out.println("Running: " + tcId + " - " + desc);

        // ===== Step 1: Chuẩn bị log =====
        StringWriter reqWriter = new StringWriter();
        PrintStream reqCapture = new PrintStream(new WriterOutputStream(reqWriter), true);

        // ===== Step 2: Build request (query) =====
// Lấy từ context
        String tokenFromCtx = (String) ctx.getAttribute("AUTH_TOKEN");
        String tokenFromExcel = row.get("auth_token"); // optional in Excel
        String bearer = tokenFromCtx != null ? tokenFromCtx : tokenFromExcel;

        String partnerCtx = (String) ctx.getAttribute("PARTNER_UID");
        String courseCtx  = (String) ctx.getAttribute("COURSE_UID");

// Xử lý placeholder cho booking_date
        String dateFromRaw = row.getOrDefault("date_from", "");
        String resolvedDateFrom = DynamicDataHelper.resolveDynamicValue(dateFromRaw);

        String dateToRaw = row.getOrDefault("date_to", "");
        String resolvedDateTo = DynamicDataHelper.resolveDynamicValue(dateToRaw);

// Query params: context + excel
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("partner_uid", partnerCtx);
        q.put("course_uid",  courseCtx);
        q.put("date_from", resolvedDateFrom);
        q.put("date_to", resolvedDateTo);
        q.put("guest_style", row.get("guest_style"));
        q.put("bag", row.get("bag"));
        q.put("transaction_code", row.get("transaction_code"));
        q.put("revenue_type", row.get("revenue_type"));
        q.put("limit", row.get("limit"));
        q.put("page", row.get("page"));
        q.put("sort_by", row.get("sort_by"));
        q.put("sort_dir", row.get("sort_dir"));

        System.out.println("🧩 Request body sau replace:\n" + q);

// ===== Step 3: Call API =====
        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer)
                .queryParams(q)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCapture))
                .when()
                .get(BASE_URL + "/golf-cms/api/report/revenue/report-booking-detail")
                .then()
                .extract()
                .response();

        String respJson = resp.asString();


        // ===== Step 4: Gắn log request/response vào report =====
        String url = BASE_URL + "/golf-cms/api/report/revenue/report-booking-detail";

        String requestLog = RequestLogHelper.buildRequestLog(
                "GET",
                url,
                q,      // query map
                null    // GET không có body
        );

        ctx.setAttribute("LAST_REQUEST_LOG", requestLog);
        ctx.setAttribute("LAST_RESPONSE_LOG", respJson);

        // ===== Step 5: Load expect JSON =====
        // Excel cột 'expected_validation_data' trỏ tới file expect (vd: create_booking_batch_expect.json)
        String expectFileName = row.getOrDefault("expected_validation_data", "get_booking_price_expect.json");
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

    }
    //    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running Login case: " + caseId);
        testRevenueDetailReport(row, ctx);   // chỉ gọi lại hàm test cũ
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }
}