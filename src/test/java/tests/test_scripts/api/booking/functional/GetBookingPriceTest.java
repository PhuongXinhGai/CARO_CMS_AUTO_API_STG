package tests.test_scripts.api.booking.functional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.DynamicDataHelper;
import common.utilities.ExcelUtils;
import common.utilities.StringUtils;
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static common.utilities.Constants.BOOKING_PRICE_ENDPOINT;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class GetBookingPriceTest extends TestConfig {
    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/Create_Booking_Batch.xlsx";
    private static final String SHEET_NAME = "Get_Booking_Price";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/booking/get_booking_price/";

    // ======================= DataProvider =======================
    @DataProvider(name = "getBookingPriceData")
    public Object[][] getBookingPriceData() throws IOException {
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
    @Test(dataProvider = "getBookingPriceData")
    public void testGetBookingList(Map<String, String> row, ITestContext ctx) throws IOException {
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
        String bookingDateRaw = row.getOrDefault("booking_date", "");
        String resolvedBookingDate = DynamicDataHelper.resolveDynamicValue(bookingDateRaw);

// Query params: context + excel
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("partner_uid", partnerCtx);
        q.put("course_uid",  courseCtx);
//        q.put("sort_by",     row.get("sort_by"));
//        q.put("sort_dir",    row.get("sort_dir"));
        q.put("booking_date", resolvedBookingDate);
//        q.put("is_single_book", row.get("is_single_book"));
//        q.put("is_ignore_tournament_booking", row.get("is_ignore_tournament_booking"));

// ===== Step 3: Call API =====
        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer)
                .queryParams(q)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCapture))
                .when()
                .get(BASE_URL + "/golf-cms/api/booking/booking-price")
                .then()
                .extract()
                .response();

        String respJson = resp.asString();


        // ===== Step 4: Gắn log request/response vào report =====
        ITestResult tr = Reporter.getCurrentTestResult();
        tr.setAttribute("requestLog", reqWriter.toString());
        tr.setAttribute("responseLog", resp.getBody().prettyPrint());

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
        // tuỳ nhu cầu: VD lưu booking_code_0, booking_uid_0 (đã định nghĩa trong "extract" của expect)
        // nếu bạn muốn parse nhanh ở đây, có thể dùng JsonPath đọc lại:
//         JsonPath jp = new JsonPath(respJson);
        // ctx.setAttribute("BOOKING_CODE_0", jp.getString("[0].booking_code"));
//        JsonPath jp = resp.jsonPath();
//        String uid      = jp.getString("[0].uid");
//        String guestStyle = jp.getString("[0].guest_style");
//        String guestStyleName  = jp.getString("[0].guest_style_name");
//        String greenFee = jp.getString("[0].list_golf_fee[0].green_fee");
//        String caddieFee = jp.getString("[0].list_golf_fee[0].caddie_fee");
//        String totalGolfFee = jp.getString("[0].mush_pay_info.total_golf_fee");
//        if (uid != null)      ctx.setAttribute("BOOKING_UID", uid);
//        if (guestStyle != null) ctx.setAttribute("GUEST_STYLE", guestStyle);
//        if (guestStyleName != null)  ctx.setAttribute("GUEST_TYPE_NAME", guestStyleName);
//        if (greenFee != null)  ctx.setAttribute("GREEN_FEE", greenFee);
//        if (caddieFee != null) ctx.setAttribute("CADDIE_FEE",  caddieFee);
//        if (totalGolfFee != null) ctx.setAttribute("TOTAL_GOLF_FEE",  totalGolfFee);
    }
}