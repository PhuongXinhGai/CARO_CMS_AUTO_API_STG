package tests.test_scripts.api.booking.create_booking;

import common.utilities.DynamicDataHelper;
import common.utilities.ExcelUtils;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static common.utilities.Constants.CREATE_BOOKING_BATCH_ENDPOINT;
import static common.utilities.Constants.DB_BOOKING_LIST_ENDPOINT;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CreateBookingBatchTest_Old01 extends TestConfig {

    @DataProvider(name = "createBookingBatchData")
    public Object[][] getCreateBookingBatchData() throws IOException {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/booking/Create_Booking_Batch.xlsx";
        return ExcelUtils.readSheetAsMaps(filePath, "testcase");
    }

    @Test(dataProvider = "createBookingBatchData")
    public void testCreateBookingBatch(Map<String, String> testData, ITestContext context) throws IOException {
        // --- PHẦN NÂNG CẤP: LẤY TOKEN ĐỘNG TỪ CONTEXT ---
        String authToken = (String) context.getAttribute("AUTH_TOKEN");
        assertNotNull(authToken, "Token không được null. Hãy chắc chắn rằng LoginTest đã chạy thành công trước.");
        // Lấy các thông tin chung từ Map
        String tc_id = testData.get("tc_id");
        String tc_description = testData.get("tc_description");
        String expectedValidationData = testData.get("expected_validation_data");

        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);

        // --- Chuẩn bị ghi log (giữ nguyên) ---
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/booking/create_booking_batch_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));

        // --- BƯỚC 4: LẮP RÁP REQUEST BODY ---
        // --- LẮP RÁP REQUEST BODY (CỰC GỌN) ---
        String processed = requestBodyTemplate;

        // Bỏ qua các cột meta không nằm trong template (nếu có)
        java.util.Set<String> metaCols = new java.util.HashSet<>();
        metaCols.add("tc_id");
        metaCols.add("tc_description");
        metaCols.add("expected_result");
        metaCols.add("expected_validation_data");

        for (Map.Entry<String, String> e : testData.entrySet()) {
            String key = e.getKey();
            if (metaCols.contains(key)) continue;
            String value = e.getValue();
            String resolved = DynamicDataHelper.resolveDynamicValue(value);
            processed = processed.replace("${" + key + "}", resolved);
        }

        String requestBody = processed;

        Response response = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + CREATE_BOOKING_BATCH_ENDPOINT)
                .then()
//                .log().all()
                .extract().response();

        ITestResult currentResult = Reporter.getCurrentTestResult();
        currentResult.setAttribute("requestLog", requestWriter.toString());
        currentResult.setAttribute("responseLog", response.getBody().prettyPrint());



        // Call api lấy dữ liệu bảng bookings
        // Các biến đã có ở bước trước:
        String partnerUid = testData.get("partner_uid");
        String courseUid  = testData.get("course_uid");
        String bookingDateResolved = DynamicDataHelper.resolveDynamicValue(testData.get("booking_date"));

// Gọi API DB: from/to, table_name=bookings, page/limit, partner_uid, course_uid
        Response dbResp = given()
                .header("Authorization", authToken)                       // curl dùng token raw, không “Bearer ”
                .header("Accept", "application/json")
                .header("Accept-Language", "en-US,en;q=0.9,vi;q=0.8")
                .queryParam("from", bookingDateResolved)
                .queryParam("to",   bookingDateResolved)
                .queryParam("table_name", "bookings")
                .queryParam("page", 1)
                .queryParam("limit", 1000)
                .queryParam("partner_uid", partnerUid)
                .queryParam("course_uid",  courseUid)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture)) // log request DB
                .when()
                .get(BASE_URL + DB_BOOKING_LIST_ENDPOINT)
                .then()
                .extract().response();

        assertEquals(dbResp.getStatusCode(), 200, "API DB trả mã khác 200");

// Đính log DB vào report để lần theo khi fail
        ITestResult rr = Reporter.getCurrentTestResult();
        rr.setAttribute("dbResponseLog", dbResp.getBody().prettyPrint());

        // ===== Chọn record DB theo uid vừa tạo =====

// 1) Lấy uid vừa tạo từ response create (nếu bạn đã lấy ở 2.2 thì bỏ 2 dòng này)
        String createdUid = response.jsonPath().getString("[0].uid");
        assertNotNull(createdUid, "Không lấy được booking uid sau khi create");
        System.out.println("createdUid = " + createdUid);

// 2) Lấy danh sách record trong ngày từ DB
        java.util.List<java.util.Map<String,Object>> records = dbResp.jsonPath().getList("data");
        assertNotNull(records, "DB không có field 'data'");
        org.testng.Assert.assertFalse(records.isEmpty(), "DB 'data' rỗng cho ngày " + bookingDateResolved);

        System.out.println("[DB] bookings count = " + records.size() + " for " + bookingDateResolved);

// 3) Tìm đúng record theo uid
        java.util.Map<String,Object> target = null;
        for (java.util.Map<String,Object> r : records) {
            Object uid = r.get("uid");
            if (createdUid.equals(String.valueOf(uid))) { target = r; break; }
        }
        assertNotNull(target, "Không tìm thấy booking có uid = " + createdUid + " trong DB");

// 4) Gắn JSON record vào report để debug khi fail
        String targetJson = new com.google.gson.Gson().toJson(target);
        org.testng.Reporter.getCurrentTestResult().setAttribute("dbTarget", targetJson);
        System.out.println("[DB] picked record = " + targetJson);

        // ===== STEP 2.5 — Resolve expected_validation_db =====
        String expectedDbRaw = testData.get("expected_validation_db");
        String expectedDbResolved = null;

        if (expectedDbRaw != null && !expectedDbRaw.trim().isEmpty()) {
            expectedDbResolved = expectedDbRaw;

            // Thay ${...} từ Excel
            for (java.util.Map.Entry<String,String> e : testData.entrySet()) {
                expectedDbResolved = expectedDbResolved.replace(
                        "${" + e.getKey() + "}",
                        e.getValue() == null ? "" : e.getValue()
                );
            }
            // Thay $created_uid từ response create
            expectedDbResolved = expectedDbResolved.replace("$created_uid", createdUid);

            // log expected (đã resolve)
            Reporter.getCurrentTestResult().setAttribute("expectedDbResolved", expectedDbResolved);
            System.out.println("[DB] expected_validation_db (resolved) = " + expectedDbResolved);
        } else {
            System.out.println("[DB] expected_validation_db trống -> sẽ bỏ qua assert chi tiết, chỉ sanity check.");
        }
        // Sanity tối thiểu (giữ nếu muốn)
        org.testng.Assert.assertEquals(String.valueOf(target.get("partner_uid")), partnerUid, "partner_uid không khớp DB");
        org.testng.Assert.assertEquals(String.valueOf(target.get("course_uid")),  courseUid,  "course_uid không khớp DB");
        org.testng.Assert.assertEquals(String.valueOf(target.get("booking_date")), bookingDateResolved, "booking_date không khớp DB");
        org.testng.Assert.assertEquals(String.valueOf(target.get("uid")), createdUid, "uid DB khác uid create");

// ===== STEP 2.6 — Parse & assert theo expected_validation_db =====
        java.util.Map<String,Object> expectedDbAsserts = null;
        if (expectedDbResolved != null && !expectedDbResolved.trim().isEmpty()) {
            java.lang.reflect.Type mapType =
                    new com.google.gson.reflect.TypeToken<java.util.Map<String,Object>>() {}.getType();
            java.util.Map<String,Object> root =
                    new com.google.gson.Gson().fromJson(expectedDbResolved, mapType);

            if (root != null && root.get("assert") instanceof java.util.Map) {
                //noinspection unchecked
                expectedDbAsserts = (java.util.Map<String,Object>) root.get("assert");
            } else {
                expectedDbAsserts = root; // cho phép viết phẳng
            }

            // “Giải nén” các field JSON-ở-trong-chuỗi để assert sâu
            java.util.Map<String,Object> normalized = normalizeJsonStrings(target);

            // log để debug khi fail
            Reporter.getCurrentTestResult().setAttribute("dbTargetNormalized",
                    new com.google.gson.Gson().toJson(normalized));
            Reporter.getCurrentTestResult().setAttribute("expectedDbAsserts",
                    new com.google.gson.Gson().toJson(expectedDbAsserts));

            // Assert theo JsonPath
            assertDbByJsonPath(normalized, expectedDbAsserts, tc_id);
        }
    }

    // Parse JSON string -> object (Map/List) nếu value là chuỗi JSON, và "null" -> null
    private static Map<String,Object> normalizeJsonStrings(Map<String,Object> in) {
        Map<String,Object> out = new java.util.LinkedHashMap<>();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        for (Map.Entry<String,Object> e : in.entrySet()) {
            Object v = e.getValue();
            if (v instanceof String) {
                String s = ((String) v).trim();
                if ("null".equalsIgnoreCase(s)) { out.put(e.getKey(), null); continue; }
                if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
                    try { out.put(e.getKey(), gson.fromJson(s, Object.class)); continue; } catch (Exception ignore) {}
                }
            }
            out.put(e.getKey(), v);
        }
        return out;
    }

    private static java.util.Map<String,Object> resolveExpectedDb(
            String expectedDbRaw,
            java.util.Map<String,String> testData,
            String createdUid
    ) {
        if (expectedDbRaw == null || expectedDbRaw.trim().isEmpty()) return null;
        String resolved = expectedDbRaw;
        for (java.util.Map.Entry<String,String> e : testData.entrySet()) {
            resolved = resolved.replace("${" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        resolved = resolved.replace("$created_uid", createdUid);

        java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<java.util.Map<String,Object>>(){}.getType();
        java.util.Map<String,Object> root = new com.google.gson.Gson().fromJson(resolved, t);
        // Cho phép viết phẳng hoặc { "assert": { ... } }
        if (root != null && root.get("assert") instanceof java.util.Map) {
            //noinspection unchecked
            return (java.util.Map<String,Object>) root.get("assert");
        }
        return root;
    }

    private static void assertDbByJsonPath(
            java.util.Map<String,Object> normalizedRecord,
            java.util.Map<String,Object> assertsMap,
            String tcId
    ) {
        if (assertsMap == null || assertsMap.isEmpty()) return;
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.jayway.jsonpath.DocumentContext dc =
                com.jayway.jsonpath.JsonPath.parse(gson.toJson(normalizedRecord));

        for (java.util.Map.Entry<String,Object> en : assertsMap.entrySet()) {
            String path = en.getKey();      // ví dụ "$.guest_style" hoặc "$.booking_restaurant.enable" hoặc "$"
            Object expected = en.getValue();

            // Tokens
            String expStr = String.valueOf(expected);
            if ("NOT_NULL".equalsIgnoreCase(expStr)) {
                Object actual = "$".equals(path) ? normalizedRecord : dc.read(path);
                org.testng.Assert.assertNotNull(actual, "[TC " + tcId + "] NOT_NULL fail at " + path);
                continue;
            }
            if (expStr.startsWith("SIZE=")) {
                int expSize = Integer.parseInt(expStr.substring(5));
                java.util.List<?> list = "$".equals(path) ? (java.util.List<?>) normalizedRecord
                        : dc.read(path);
                org.testng.Assert.assertNotNull(list, "[TC " + tcId + "] List null at " + path);
                org.testng.Assert.assertEquals(list.size(), expSize, "[TC " + tcId + "] SIZE mismatch at " + path);
                continue;
            }

            Object actual = "$".equals(path) ? normalizedRecord : dc.read(path);
            if (expected instanceof Number && actual instanceof Number) {
                java.math.BigDecimal ev = new java.math.BigDecimal(expected.toString());
                java.math.BigDecimal av = new java.math.BigDecimal(actual.toString());
                org.testng.Assert.assertEquals(av.compareTo(ev), 0, "[TC " + tcId + "] Numeric mismatch at " + path);
            } else {
                org.testng.Assert.assertEquals(String.valueOf(actual), String.valueOf(expected),
                        "[TC " + tcId + "] Mismatch at " + path);
            }
        }
    }



}