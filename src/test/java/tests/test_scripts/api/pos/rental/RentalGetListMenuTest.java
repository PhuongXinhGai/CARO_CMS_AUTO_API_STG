package tests.test_scripts.api.pos.rental;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.ExcelUtils;
import common.utilities.RequestLogHelper;
import common.utilities.StringUtils;
import framework.core.FlowRunnable;
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RentalGetListMenuTest extends TestConfig implements FlowRunnable {
    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/POS.xlsx";
    private static final String SHEET_NAME = "Rental_List_Menu";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/POS/rental/list_menu/";

    // ======================= DataProvider =======================
    @DataProvider(name = "getListMenuData")
    public Object[][] getListMenuData() throws IOException {
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
    @Test(dataProvider = "getListMenuData")
    public void testGetListMenu(Map<String, String> row, ITestContext ctx) throws IOException {
        final String tcId = row.getOrDefault("tc_id", "NO_ID");
        final String desc = row.getOrDefault("tc_description", "Create booking batch");

        // ===== Step 1: In ra testcase được run =====
        System.out.println("Running: " + tcId + " - " + desc);
        // ===== Step 2: Build request (query) =====
// Lấy từ context
        String tokenFromCtx = (String) ctx.getAttribute("AUTH_TOKEN");
        String tokenFromExcel = row.get("auth_token"); // optional in Excel
        String bearer = tokenFromCtx != null ? tokenFromCtx : tokenFromExcel;

        String partnerCtx = (String) ctx.getAttribute("PARTNER_UID");
        String courseCtx  = (String) ctx.getAttribute("COURSE_UID");
        String guestStyleCtx  = (String) ctx.getAttribute("GUEST_STYLE_0");


// Query params: context + excel
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("course_uid",  courseCtx);
        q.put("partner_uid", partnerCtx);
        q.put("service_id", row.get("service_id"));
        q.put("status", row.get("status"));
        q.put("is_driving", row.get("is_driving"));
        q.put("guestStyleCtx", guestStyleCtx);

        System.out.println("🧩 Request body sau replace:\n" + q);

// ===== Step 3: Call API =====
        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer)
                .queryParams(q)
                .when()
                .get(BASE_URL + "/golf-cms/api/rental/golf-club")
                .then()
                .extract()
                .response();

        String respJson = resp.asString();


        // ===== Step 4: Gắn log request/response vào report =====
        String url = BASE_URL + "/golf-cms/api/rental/golf-club";

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
        JsonPath jp = resp.jsonPath();
//Lưu đặt caddie
        String booking_caddie_name            = jp.getString("booking_caddie.name");
        String booking_caddie_fee             = jp.getString("booking_caddie.fee");
        if (booking_caddie_name != null)            ctx.setAttribute("BOOKING_CADDIE_NAME", booking_caddie_name);
        if (booking_caddie_fee != null)             ctx.setAttribute("BOOKING_CADDIE_FEE", booking_caddie_fee);

// Lưu lẻ xe
        for (int i = 0; i < 5; i++) {
            String odd_car_hole            = jp.getString("booking_buggy.odd_car_fee[" + i + "].hole");
            String odd_car_fee         = jp.getString("booking_buggy.odd_car_fee[" + i + "].fee");
            if (odd_car_hole != null)            ctx.setAttribute("ODD_CAR_HOLE_" + odd_car_hole, odd_car_hole);
            if (odd_car_fee != null)     ctx.setAttribute("ODD_CAR_FEE_" + odd_car_hole, odd_car_fee);
        }

//  Lưu riêng xe
        for (int i = 0; i < 5; i++) {
            String private_car_hole            = jp.getString("booking_buggy.private_car_fee[" + i + "].hole");
            String private_car_fee            = jp.getString("booking_buggy.private_car_fee[" + i + "].fee");

            if (private_car_hole != null)            ctx.setAttribute("PRIVATE_CAR_HOLE_" + private_car_hole, private_car_hole);
            if (private_car_fee != null)     ctx.setAttribute("PRIVATE_CAR_FEE_" + private_car_hole, private_car_fee);
        }
//   Lưu thuê 1/2 xe
        for (int i = 0; i < 5; i++) {
            String rental_car_hole            = jp.getString("booking_buggy.rental_fee[" + i + "].hole");
            String rental_car_fee            = jp.getString("booking_buggy.rental_fee[" + i + "].fee");

            if (rental_car_hole != null)            ctx.setAttribute("RENTAL_CAR_HOLE_" + rental_car_hole, rental_car_hole);
            if (rental_car_fee != null)     ctx.setAttribute("RENTAL_CAR_FEE_" + rental_car_hole, rental_car_fee);
        }

//  Lưu item rental
        for (int i = 0; i < 4; i++) {
            String rental_id            = jp.getString("rentals[" + i + "].rental_id");
            String price           = jp.getString("rentals[" + i + "].price");

            if (rental_id != null)            ctx.setAttribute("ITEM_CODE_" + i, rental_id);
            if (price != null)     ctx.setAttribute("UNIT_PRICE_" + i, price);
        }

    }
    //    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running Login case: " + caseId);
        testGetListMenu(row, ctx);   // chỉ gọi lại hàm test cũ
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }
}
