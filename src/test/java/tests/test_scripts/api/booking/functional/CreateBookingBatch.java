package tests.test_scripts.api.booking.functional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.AssertionHelper;
import common.utilities.DynamicDataHelper;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.test_config.TestConfig;

import java.io.FileInputStream;
import java.io.FileReader;
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
    private static final String SHEET_NAME = "testcase";
    // Thư mục chứa JSON request/expect cho API này
    private static final String JSON_DIR = System.getProperty("user.dir")
            + "/src/main/resources/input_json_file/booking/create_booking/";

    // ======================= DataProvider =======================
    @DataProvider(name = "bookingData")
    public Object[][] bookingData() throws Exception {
        return readSheetAsMaps(EXCEL_FILE, SHEET_NAME);
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
    public void testCreateBookingBatch(Map<String, String> row, ITestContext ctx) throws Exception {
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
        String requestBody = replacePlaceholdersInString(reqTpl, row); // thay tất cả ${colName}

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
        String expectResolved = replacePlaceholdersInString(expectRaw, row);
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> expectJson = gson.fromJson(expectResolved, mapType);

        // ===== Step 7: So sánh actual vs expect =====
        AssertionHelper.assertFromJson(respJson, expectJson);

        // ===== Step 8: Extract lưu biến cho bước sau (nếu cần) =====
        // tuỳ nhu cầu: VD lưu booking_code_0, booking_uid_0 (đã định nghĩa trong "extract" của expect)
        // nếu bạn muốn parse nhanh ở đây, có thể dùng JsonPath đọc lại:
        // JsonPath jp = new JsonPath(respJson);
        // ctx.setAttribute("BOOKING_CODE_0", jp.getString("[0].booking_code"));
    }

    // ======================= Helpers =======================

    /** Replace ${key} trong chuỗi bằng value từ Map (giá trị null sẽ thay bằng chuỗi rỗng). */
    private String replacePlaceholdersInString(String raw, Map<String, String> ctx) {
        String out = raw;
        for (Map.Entry<String, String> e : ctx.entrySet()) {
            String key = "${" + e.getKey() + "}";
            String val = e.getValue() == null ? "" : e.getValue();

            // xử lý dynamic {{TODAY}}, {{TODAY+N}}, ...
            val = DynamicDataHelper.resolveDynamicValue(val);

            out = out.replace(key, val);
        }
        return out;
    }


    /**
     * Đọc Excel → trả về Object[][] mỗi phần tử là 1 Map<Header, Value>.
     * Bỏ dòng trắng, trim giá trị; header lấy từ dòng đầu tiên có dữ liệu.
     */
    private Object[][] readSheetAsMaps(String path, String sheetName) throws Exception {
        try (FileInputStream fis = new FileInputStream(path);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);

            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();

            // tìm header row
            Row header = sheet.getRow(firstRow);
            while (header == null && firstRow <= lastRow) {
                firstRow++;
                header = sheet.getRow(firstRow);
            }
            if (header == null) throw new RuntimeException("Header row is null");

            int cols = header.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                Cell hc = header.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                headers.add(hc == null ? ("col" + c) : hc.toString().trim());
            }

            List<Object[]> rows = new ArrayList<>();
            for (int r = firstRow + 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                boolean allBlank = true;
                Map<String, String> map = new LinkedHashMap<>();
                for (int c = 0; c < cols; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String val = cell == null ? "" : formatCell(cell);
                    if (!val.isEmpty()) allBlank = false;
                    map.put(headers.get(c), val);
                }
                if (!allBlank) rows.add(new Object[]{map});
            }

            Object[][] data = new Object[rows.size()][];
            for (int i = 0; i < rows.size(); i++) data[i] = rows.get(i);
            return data;
        }
    }

    private String formatCell(Cell cell) {
        cell.setCellType(CellType.STRING);
        String v = cell.getStringCellValue();
        return v == null ? "" : v.trim();
    }
}
