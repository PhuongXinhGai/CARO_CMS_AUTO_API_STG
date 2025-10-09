package tests.test_scripts.api.booking.functional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.ExcelUtils;
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static io.restassured.RestAssured.given;

public class CreateBookingBatch extends TestConfig {

    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/Create_Booking_Batch.xlsx";
    private static final String SHEET_NAME = "Create_Booking_1_Player";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/booking/create_booking/";

    // ======================= DataProvider =======================
    @DataProvider(name = "bookingData")
    public Object[][] bookingData() throws IOException {
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
    @Test(dataProvider = "bookingData")
    public void testCreateBookingBatch(Map<String, String> row, ITestContext ctx) throws IOException {
        final String tcId = row.getOrDefault("tc_id", "NO_ID");
        final String desc = row.getOrDefault("tc_description", "Create booking batch");

        System.out.println("Running: " + tcId + " - " + desc);

        // ===== Step 1: Chuẩn bị log =====
        StringWriter reqWriter = new StringWriter();
        PrintStream reqCapture = new PrintStream(new WriterOutputStream(reqWriter), true);

        // ===== Step 2: Build request =====
        // Excel cột 'input_placeholders' trỏ tới file request (vd: create_booking_batch_request.json)
        String reqFileName = row.getOrDefault("input_placeholders", "create_booking_batch_request.json");
        String reqTpl = Files.readString(Paths.get(JSON_DIR + reqFileName));
        String requestBody = StringUtils.replacePlaceholdersInString(reqTpl, row); // thay tất cả ${colName}

        // ===== Step 3: Call API =====
        String tokenFromCtx = (String) ctx.getAttribute("AUTH_TOKEN");
        String tokenFromExcel = row.get("auth_token"); // optional in Excel
        String bearer = tokenFromCtx != null ? tokenFromCtx : tokenFromExcel;

        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .header("Authorization", bearer != null ? bearer : "")
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCapture))
                .when()
                .post(BASE_URL + "/golf-cms/api/booking/batch")
                .then()
                .extract().response();

        String respJson = resp.asString();

        // ===== Step 4: Gắn log request/response vào report =====
        ITestResult tr = Reporter.getCurrentTestResult();
        tr.setAttribute("requestLog", reqWriter.toString());
        tr.setAttribute("responseLog", resp.getBody().prettyPrint());

        // ===== Step 5: Load expect JSON =====
        // Excel cột 'expected_validation_data' trỏ tới file expect (vd: create_booking_batch_expect.json)
        String expectFileName = row.getOrDefault("expected_validation_data", "create_booking_batch_expect.json");
        String expectRaw = Files.readString(Paths.get(JSON_DIR + expectFileName));

        // ===== Step 6: Replace placeholder trong expect =====
        // Lưu ý: với boolean (true/false) hãy KHÔNG đặt dấu nháy quanh placeholder trong file expect.
        String expectResolved = StringUtils.replacePlaceholdersInString(expectRaw, row);
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> expectJson = gson.fromJson(expectResolved, mapType);

        // ===== Step 7: So sánh actual vs expect =====
        AssertionHelper.assertFromJson(respJson, expectJson);

        // ===== Step 8: Extract lưu biến cho bước sau (nếu cần) =====
        // tuỳ nhu cầu: VD lưu booking_code_0, booking_uid_0 (đã định nghĩa trong "extract" của expect)
        // nếu bạn muốn parse nhanh ở đây, có thể dùng JsonPath đọc lại:
//         JsonPath jp = new JsonPath(respJson);
        // ctx.setAttribute("BOOKING_CODE_0", jp.getString("[0].booking_code"));
        JsonPath jp = resp.jsonPath();
        String uid      = jp.getString("[0].uid");
        String guestStyle = jp.getString("[0].guest_style");
        String guestStyleName  = jp.getString("[0].guest_style_name");
        String greenFee = jp.getString("[0].list_golf_fee[0].green_fee");
        String caddieFee = jp.getString("[0].list_golf_fee[0].caddie_fee");
        String totalGolfFee = jp.getString("[0].mush_pay_info.total_golf_fee");
        if (uid != null)      ctx.setAttribute("BOOKING_UID", uid);
        if (guestStyle != null) ctx.setAttribute("GUEST_STYLE", guestStyle);
        if (guestStyleName != null)  ctx.setAttribute("GUEST_TYPE_NAME", guestStyleName);
        if (greenFee != null)  ctx.setAttribute("GREEN_FEE", greenFee);
        if (caddieFee != null) ctx.setAttribute("CADDIE_FEE",  caddieFee);
        if (totalGolfFee != null) ctx.setAttribute("TOTAL_GOLF_FEE",  totalGolfFee);
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        // chỉ định các key chuẩn bạn muốn show
        ReportHelper.logContext(ctx, "BOOKING_UID", "GUEST_STYLE", "GREEN_FEE", "CADDIE_FEE", "TOTAL_GOLF_FEE");
    }

}
