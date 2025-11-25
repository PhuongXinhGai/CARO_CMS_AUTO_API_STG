package tests.test_scripts.api.invoice;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.ExcelUtils;
import common.utilities.StringUtils;
import common.utilities.WaitHelper;
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
import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class EInvoiceTest extends TestConfig implements FlowRunnable {

    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/Invoice.xlsx";
    private static final String SHEET_NAME = "E_Invoice";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/invoice/e_invoice/";

    // ======================= DataProvider =======================
    @DataProvider(name = "eInvoiceData")
    public Object[][] eInvoiceData() throws IOException {
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
    @Test(dataProvider = "eInvoiceData")
    public void testEInvoice(Map<String, String> row, ITestContext ctx) throws IOException {
        final String tcId = row.getOrDefault("tc_id", "NO_ID");
        final String desc = row.getOrDefault("tc_description", "Create booking batch");

        // ===== Step 1: In ra testcase được run =====
        System.out.println("Running: " + tcId + " - " + desc);        WaitHelper.waitSeconds(5);

        // ===== Step 2: Build request =====
        String reqFileName = row.getOrDefault("input_placeholders", "");
        String reqTpl = Files.readString(Paths.get(JSON_DIR + reqFileName));
        String requestBody = StringUtils.replacePlaceholdersAdvanced(reqTpl, row, ctx); // thay tất cả ${colName}

        System.out.println("🧩 Request body sau replace:\n" + requestBody);

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
                .post(BASE_URL + "/golf-cms/api/e-invoice/payment/list")
                .then()
                .extract().response();

        String respJson = resp.asString();

        // ===== Step 4: Gắn log request/response vào Flow =====
        String url = BASE_URL + LOGIN_ENDPOINT;
        String requestLog = RequestLogHelper.buildRequestLog(
                "POST",
                url,
                null,          // POST này không có query
                requestBody    // body JSON string
        );

        ctx.setAttribute("LAST_REQUEST_LOG", requestLog);
        ctx.setAttribute("LAST_RESPONSE_LOG", respJson);

        // ===== Step 5: Load expect JSON =====
        // Excel cột 'expected_validation_data' trỏ tới file expect (vd: create_booking_batch_expect.json)
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
//        Extract paymetn_id player
        try {
            // Parse JSON response
            com.jayway.jsonpath.DocumentContext jp = com.jayway.jsonpath.JsonPath.parse(respJson);

            // Lấy toàn bộ mảng data
            List<Map<String, Object>> dataList = jp.read("$.data");

            // Lặp qua tất cả BAG_i trên context
            for (int i = 0; i < 10; i++) { // tối đa 10, tùy nhu cầu Phương chỉnh
                String bagKey = "BAG_" + i;
                Object bagVal = ctx.getAttribute(bagKey);
                if (bagVal == null) continue; // bỏ qua nếu chưa có

                String bag = bagVal.toString();
                String paymentId = null;

                // Duyệt từng item trong data để tìm bag tương ứng
                for (Map<String, Object> item : dataList) {
                    if (bag.equals(item.get("bag"))) {
                        paymentId = (String) item.get("payment_id");
                        break;
                    }
                }

                if (paymentId != null) {
                    String key = "PAYMENT_ID_BAG_" + i;
                    ctx.setAttribute(key, paymentId);
                    System.out.printf("✅ [%s] bag=%s → payment_id=%s%n", key, bag, paymentId);
                } else {
                    System.out.printf("⚠️ Bag %s (value=%s) không tìm thấy trong response%n", bagKey, bag);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Lỗi khi extract payment_id: " + e.getMessage());
            e.printStackTrace();
        }

//        Extract paymetn_id agency
        try {
            com.jayway.jsonpath.DocumentContext jp = com.jayway.jsonpath.JsonPath.parse(respJson);
            List<Map<String, Object>> dataList = jp.read("$.data");

            // 🔹 Lấy 4 bag từ context
            String bag0 = String.valueOf(ctx.getAttribute("BAG_0"));
            String bag1 = String.valueOf(ctx.getAttribute("BAG_1"));
            String bag2 = String.valueOf(ctx.getAttribute("BAG_2"));
            String bag3 = String.valueOf(ctx.getAttribute("BAG_3"));

            Set<String> targetBags = new HashSet<>(Arrays.asList(bag0, bag1, bag2, bag3));

            String paymentIdAgency = null;
            String subBagsMatched = null;

            // 🔍 Duyệt qua từng item trong data để tìm record có sub_bags chứa đủ 4 bag này
            for (Map<String, Object> item : dataList) {
                Map<String, Object> bagInfo = (Map<String, Object>) item.get("bag_info");
                if (bagInfo == null) continue;

                String subBags = String.valueOf(bagInfo.get("sub_bags"));
                if (subBags == null || subBags.trim().isEmpty()) continue;

                // Chuyển sub_bags thành set để so sánh (vd: "BAG_1,BAG_3,BAG_0,BAG_2")
                Set<String> subBagSet = Arrays.stream(subBags.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                // ✅ Nếu sub_bags chứa đủ tất cả 4 bag context
                if (subBagSet.containsAll(targetBags)) {
                    paymentIdAgency = (String) item.get("payment_id");
                    subBagsMatched = subBags;
                    break;
                }
            }

            // 🔸 Ghi kết quả lên context
            if (paymentIdAgency != null) {
                ctx.setAttribute("PAYMENT_ID_AGENCY", paymentIdAgency);
                ctx.setAttribute("SUB_BAGS_VALUE", subBagsMatched);
                System.out.printf("✅ Found PAYMENT_ID_AGENCY = %s (sub_bags matched = %s)%n",
                        paymentIdAgency, subBagsMatched);
            } else {
                System.out.printf("⚠️ Không tìm thấy record nào có sub_bags chứa đủ %s%n", targetBags);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Lỗi khi extract PAYMENT_ID_AGENCY: " + e.getMessage());
            e.printStackTrace();
        }

    }
    //    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running Login case: " + caseId);
        testEInvoice(row, ctx);   // chỉ gọi lại hàm test cũ
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }

}
