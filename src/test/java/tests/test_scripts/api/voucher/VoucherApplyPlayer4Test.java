package tests.test_scripts.api.voucher;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.*;
import framework.core.FlowRunnable;
import helpers.ReportHelper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static common.utilities.Constants.*;
import static io.restassured.RestAssured.given;

import tests.test_config.TestConfig;

public class  VoucherApplyPlayer4Test extends TestConfig implements FlowRunnable {
    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/voucher.xlsx";
    private static final String SHEET_NAME = "Voucher_Apply_Player4";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/voucher/vouchers_golf_fee_apply/";

    // ======================= DataProvider =======================
    @DataProvider(name = "voucherApplyData")
    public Object[][] voucherApplyData() throws IOException {
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
    @Test(dataProvider = "voucherApplyData")
    public void testVoucherApplyPlayer4(Map<String, String> row, ITestContext ctx) throws IOException {
        final String tcId = row.getOrDefault("tc_id", "NO_ID");
        final String desc = row.getOrDefault("tc_description", "");

        // ===== Step 1: In ra testcase được run =====
        System.out.println("Running: " + tcId + " - " + desc);

        // ===== Step 2: Build request (query) =====
		String reqFileName = row.getOrDefault("input_placeholders", "");
        String reqTpl = Files.readString(Paths.get(JSON_DIR + reqFileName));
        String requestBody = StringUtils.replacePlaceholdersAdvanced(reqTpl, row, ctx);
        System.out.println("🧩 Request JSON sau replace:\n" + requestBody);

		// ===== Step 3: Call API =====
        String tokenFromCtx = (String) ctx.getAttribute("AUTH_TOKEN");
        String tokenFromExcel = row.get("auth_token"); // optional in Excel
        String bearer = tokenFromCtx != null ? tokenFromCtx : tokenFromExcel;

        Response resp = given()
                        .contentType(ContentType.JSON)
                        .header("Accept", "application/json")
                        .header("Authorization", bearer != null ? bearer : "")
                        .body(requestBody)
                        .when()
                        .post(BASE_URL + GOLF_FEE_APPLY_ENDPOINT)
                        .then()
                        .extract().response();

        String respJson = resp.asString();

        // ===== Step 4: Gắn log request/response vào report =====
        String url = BASE_URL + GOLF_FEE_APPLY_ENDPOINT;
        String requestLog = RequestLogHelper.buildRequestLog(
        "POST",
        url,
        null,
        requestBody
);

        ctx.setAttribute("LAST_REQUEST_LOG", requestLog);
        ctx.setAttribute("LAST_RESPONSE_LOG", respJson);

        // ===== Step 5: Load expect JSON =====
        String expectFileName = row.getOrDefault("expected_validation_data", "");
        String expectRaw = Files.readString(Paths.get(JSON_DIR + expectFileName));

        // ===== Step 6: Replace placeholder trong expect =====
        String expectResolved = StringUtils.replacePlaceholdersInString(expectRaw, row);
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> expectJson = gson.fromJson(expectResolved, mapType);

        // ===== Step 7: So sánh actual vs expect =====
        AssertionHelper.verifyStatusCode(resp, expectJson);
        AssertionHelper.assertFromJson(respJson, expectJson);

        // ===== Step 8: Extract lưu biến cho bước sau (nếu cần) =====
        // ===== Step 8.1: Extract & Save voucher codes and IDs to ctx =====
        try {
            Gson gson2 = new Gson();
            Type reqType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> reqMap = gson2.fromJson(requestBody, reqType);

            Object voucherApplyObj = reqMap.get("voucher_apply");
            if (voucherApplyObj instanceof Iterable<?>) {
                Iterable<?> list = (Iterable<?>) voucherApplyObj;
                for (Object item : list) {
                    if (item instanceof Map<?, ?>) {
                        Map<String, Object> m = (Map<String, Object>) item;

                        // === voucher_code ===
                        String voucherCode = String.valueOf(m.get("voucher_code"));

                        // === id: xử lý bỏ .0 ===
                        Object idObj = m.get("id");
                        String id;

                        if (idObj instanceof Number) {
                            // Gson parse số thành Double -> convert về long để bỏ .0
                            long idLong = ((Number) idObj).longValue();
                            id = String.valueOf(idLong);
                        } else {
                            id = String.valueOf(idObj);
                        }

                        // === Key lưu lên ctx ===
                        String codeKey = "VOUCHER_CODE_" + voucherCode;
                        String idKey   = "VOUCHER_ID_" + voucherCode;

                        ctx.setAttribute(codeKey, voucherCode);
                        ctx.setAttribute(idKey, id);

                        System.out.println("🔖 Saved to ctx: " + codeKey + "=" + voucherCode);
                        System.out.println("🔖 Saved to ctx: " + idKey + "=" + id);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Cannot extract voucher_apply fields: " + e.getMessage());
        }

    }
    //    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running case: " + caseId);
        testVoucherApplyPlayer4(row, ctx);   // chỉ gọi lại hàm test cũ
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }
}