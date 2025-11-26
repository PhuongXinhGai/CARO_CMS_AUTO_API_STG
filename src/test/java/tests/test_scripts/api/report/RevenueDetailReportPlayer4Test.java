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
import java.util.List;
import java.util.Map;

import static common.utilities.Constants.REVENUE_DETAIL_REPORT_ENDPOINT;
import static io.restassured.RestAssured.given;

public class RevenueDetailReportPlayer4Test extends TestConfig implements FlowRunnable {
    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/Report.xlsx";
    private static final String SHEET_NAME = "Revenue_Detail_Report_Player4";
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
     * 1) In ra testcase được run
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

        // ===== Step 1: In ra testcase được run =====
        System.out.println("Running: " + tcId + " - " + desc);
        // ===== Step 2: Build request (query) =====
// Lấy từ context
        String tokenFromCtx = (String) ctx.getAttribute("AUTH_TOKEN");
        String tokenFromExcel = row.get("auth_token"); // optional in Excel
        String bearer = tokenFromCtx != null ? tokenFromCtx : tokenFromExcel;

        Map<String, Object> q = QueryParamHelper.build(row, ctx);
        System.out.println("🧩 Query Params:\n" + q);

        // ===== Step 3: Call API =====
        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer)
                .queryParams(q)
                .when()
                .get(BASE_URL + REVENUE_DETAIL_REPORT_ENDPOINT)
                .then()
                .extract()
                .response();

        String respJson = resp.asString();


        // ===== Step 4: Gắn log request/response vào report =====
        String url = BASE_URL + REVENUE_DETAIL_REPORT_ENDPOINT;
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

        // ===== Step 7.1: Nghiệm thu expect theo cột excel expect_* =====
        // Lấy BAG_0 từ context
        String bagCode = (String) ctx.getAttribute("BAG_3");
        if (bagCode == null) {
            throw new AssertionError("BAG_3 không tồn tại trong context");
        }

// Parse response → list
        List<Map<String, Object>> dataList = resp.jsonPath().getList("data");

// Tìm phần tử có 'bag' đúng với BAG_0
        Map<String, Object> target = dataList.stream()
                .filter(item -> bagCode.equals(item.get("bag")))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Không tìm thấy phần tử bag = " + bagCode)
                );

        System.out.println("🔍 Đã tìm thấy phần tử cần nghiệm thu theo bag: " + target);

// ===== DUYỆT TẤT CẢ CÁC cột excel có prefix "expect_" =====
        for (String colName : row.keySet()) {

            if (colName.startsWith("expect_")) {

                // field trong API = bỏ prefix (VD: expect_cash → cash)
                String jsonField = colName.replace("expect_", "");

                Object expected = row.get(colName);

                // Skip field nếu excel để trống
                if (expected == null || expected.toString().isBlank())
                    continue;

                Object actual = target.get(jsonField);

                System.out.println("🔎 Check field: " + jsonField +
                        " | expected=" + expected +
                        " | actual=" + actual);

                AssertionHelper.assertEquals("$.data[*]." + jsonField, actual, expected);
            }
        }


        // ===== Step 8: Extract lưu biến cho bước sau (nếu cần) =====

    }
    //    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running case: " + caseId);
        testRevenueDetailReport(row, ctx);   // chỉ gọi lại hàm test cũ
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }
}