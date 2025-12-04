package tests.test_scripts.api.booking.create_booking;

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
import java.util.Map;

import static common.utilities.Constants.CREATE_BOOKING_BATCH_ENDPOINT;
import static io.restassured.RestAssured.given;

public class CreateBookingBatch4PlayerTest extends TestConfig implements FlowRunnable {

    // ==== ĐƯỜNG DẪN — chỉnh cho khớp project của bạn ====
    private static final String EXCEL_FILE = System.getProperty("user.dir")
            + "/src/main/resources/input_excel_file/booking/Create_Booking_Batch.xlsx";
    private static final String SHEET_NAME = "Create_Booking_4_Player";
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
     * 1) In ra testcase được run
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

        // ===== Step 1: In ra testcase được run =====
        System.out.println("Running: " + tcId + " - " + desc);
        // ===== Step 2: Build request =====
        // Excel cột 'input_placeholders' trỏ tới file request (vd: create_booking_batch_request.json)
        String reqFileName = row.getOrDefault("input_placeholders", "create_booking_batch_request.json");
        String reqTpl = Files.readString(Paths.get(JSON_DIR + reqFileName));
        String requestBody = StringUtils.replacePlaceholdersAdvanced(reqTpl, row, ctx);
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
                .post(BASE_URL + CREATE_BOOKING_BATCH_ENDPOINT)
                .then()
                .extract().response();

        String respJson = resp.asString();

        // ===== Step 4: Gắn log request/response vào Flow =====
        String url = BASE_URL + CREATE_BOOKING_BATCH_ENDPOINT;
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
        String expectFileName = row.getOrDefault("expected_validation_data", "create_booking_batch_expect.json");
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
        JsonPath jp = resp.jsonPath();

        for (int i = 0; i < 4; i++) {
            String uid            = jp.getString("[" + i + "].uid");
            String bill_code           = jp.getString("[" + i + "].bill_code");
            String guestStyle     = jp.getString("[" + i + "].guest_style");
            String guestStyleName = jp.getString("[" + i + "].guest_style_name");
            String round_id = jp.getString("[" + i + "].round_id");

            String greenFee       = jp.getString("[" + i + "].list_golf_fee[0].green_fee");
            String caddieFee      = jp.getString("[" + i + "].list_golf_fee[0].caddie_fee");
            String totalGolfFee   = jp.getString("[" + i + "].mush_pay_info.total_golf_fee");

            String courseType   = jp.getString("[" + i + "].course_type");
            String teeType   = jp.getString("[" + i + "].tee_type");
            String teeTime   = jp.getString("[" + i + "].tee_time");
            String turnTime   = jp.getString("[" + i + "].turn_time");
            String tee_time_after   = jp.getString("[" + i + "].tee_time_after");
            String teePath   = jp.getString("[" + i + "].tee_path");
            String teeOffTime   = jp.getString("[" + i + "].tee_off_time");
            String rowIndex   = jp.getString("[" + i + "].row_index");
            String booking_date   = jp.getString("[" + i + "].booking_date");

            String hole   = jp.getString("[" + i + "].hole");
            String holeBooking   = jp.getString("[" + i + "].hole_booking");
            String caddie_booking   = jp.getString("[" + i + "].caddie_booking");

            String customer_name   = jp.getString("[" + i + "].customer_name");
            String customer_booking_name   = jp.getString("[" + i + "].customer_booking_name");
            String customer_booking_phone   = jp.getString("[" + i + "].customer_booking_phone");
            String customer_booking_email   = jp.getString("[" + i + "].customer_booking_email");
            String member_name_of_guest   = jp.getString("[" + i + "].member_name_of_guest");
            String member_uid_of_guest   = jp.getString("[" + i + "].member_uid_of_guest");
            String member_card_uid   = jp.getString("[" + i + "].member_card_uid");
            String agency_id   = jp.getString("[" + i + "].agency_id");

            String bagStatus   = jp.getString("[" + i + "].bag_status");


            if (uid != null)            ctx.setAttribute("BOOKING_UID_" + i, uid);
            if (bill_code != null)            ctx.setAttribute("BILL_CODE_" + i, bill_code);
            if (guestStyle != null)     ctx.setAttribute("GUEST_STYLE_" + i, guestStyle);
            if (guestStyleName != null) ctx.setAttribute("GUEST_STYLE_NAME_" + i, guestStyleName);
            if (round_id != null) ctx.setAttribute("ROUND_ID_" + i, round_id);

            if (greenFee != null)       ctx.setAttribute("GREEN_FEE_" + i, greenFee);
            if (caddieFee != null)      ctx.setAttribute("CADDIE_FEE_" + i, caddieFee);
            if (totalGolfFee != null)   ctx.setAttribute("TOTAL_GOLF_FEE_" + i, totalGolfFee);

            if (courseType != null)   ctx.setAttribute("COURSE_TYPE_" + i, courseType);
            if (teeType != null)   ctx.setAttribute("TEE_TYPE_" + i, teeType);
            if (teeTime != null)   ctx.setAttribute("TEE_TIME_" + i, teeTime);
            if (turnTime != null)   ctx.setAttribute("TURN_TIME_" + i, turnTime);
            if (tee_time_after != null)   ctx.setAttribute("TEE_TIME_AFTER_" + i, tee_time_after);
            if (teePath != null)   ctx.setAttribute("TEE_PATH_" + i, teePath);
            if (teeOffTime != null)   ctx.setAttribute("TEE_OFF_TIME_" + i, teeOffTime);
            if (rowIndex != null)   ctx.setAttribute("ROW_INDEX_" + i, rowIndex);
            if (booking_date != null)   ctx.setAttribute("BOOKING_DATE_" + i, booking_date);

            if (hole != null)   ctx.setAttribute("HOLE_" + i, hole);
            if (holeBooking != null)   ctx.setAttribute("HOLE_BOOKING_" + i, holeBooking);
            if (caddie_booking != null)   ctx.setAttribute("CADDIE_BOOKING_" + i, caddie_booking);

            if (customer_name != null)   ctx.setAttribute("CUSTOMER_NAME_" + i, customer_name);
            if (customer_booking_name != null)   ctx.setAttribute("CUSTOMER_BOOKING_NAME_" + i, customer_booking_name);
            if (customer_booking_phone != null)   ctx.setAttribute("CUSTOMER_BOOKING_PHONE_" + i, customer_booking_phone);
            if (customer_booking_email != null)   ctx.setAttribute("CUSTOMER_BOOKING_EMAIL_" + i, customer_booking_email);
            if (member_name_of_guest != null)   ctx.setAttribute("MEMBER_NAME_OF_GUEST_" + i, member_name_of_guest);
            if (member_uid_of_guest != null)   ctx.setAttribute("MEMBER_UID_OF_GUEST_" + i, member_uid_of_guest);
            if (member_card_uid != null)   ctx.setAttribute("MEMBER_CARD_UID_" + i, member_card_uid);
            if (agency_id != null)   ctx.setAttribute("AGENCY_ID_" + i, agency_id);

            if (bagStatus != null)   ctx.setAttribute("BAG_STATUS_" + i, bagStatus);
        }

    }

    //    Flow chạy tích hợp
    @Override
    public void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception {
        Map<String, String> row = findRowByCaseId(EXCEL_FILE, SHEET_NAME, caseId);
        logger.info("▶️ Running case: " + caseId);
        testCreateBookingBatch(row, ctx);   // chỉ gọi lại hàm test cũ
    }

    @AfterMethod(alwaysRun = true)
    public void dumpCtxToReport(ITestContext ctx) {
        ReportHelper.logAllContext(ctx);
    }

}
