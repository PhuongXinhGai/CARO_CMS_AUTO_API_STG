package tests.test_scripts.api.booking.functional;

import com.google.gson.Gson;
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
import java.util.Set;

import static common.utilities.Constants.CREATE_BOOKING_BATCH_ENDPOINT;
import static common.utilities.Constants.DB_BOOKING_LIST_ENDPOINT;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CreateBookingBatchTest02 extends TestConfig {

    @DataProvider(name = "createBookingBatchData")
    public Object[][] getCreateBookingBatchData() {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/booking/Create_Booking_Batch.xlsx";
        return ExcelUtils.getTestDataWithMap(filePath, "testcase");
    }

    @Test(dataProvider = "createBookingBatchData")
    public void testCreateBookingBatch(Map<String, String> testData, ITestContext context) throws IOException {
        // --- LẤY TOKEN ---
        String authToken = (String) context.getAttribute("AUTH_TOKEN");
        assertNotNull(authToken, "Token không được null. Hãy chắc chắn rằng LoginTest đã chạy thành công trước.");

        // --- LẤY THÔNG TIN TEST CASE ---
        String tc_id = testData.get("tc_id");
        String tc_description = testData.get("tc_description");
        System.out.println("Đang chạy test case: " + tc_id + " - " + tc_description);

        // --- GHI LOG ---
        StringWriter reqWriterCreate = new StringWriter();
        PrintStream reqCaptureCreate = new PrintStream(new WriterOutputStream(reqWriterCreate), true);

        StringWriter reqWriterDb = new StringWriter();
        PrintStream reqCaptureDb = new PrintStream(new WriterOutputStream(reqWriterDb), true);

        // --- CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/booking/create_booking_batch_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));

        // Lắp data từ Excel
        java.util.Set<String> metaCols = Set.of("tc_id", "tc_description", "expected_result", "expected_validation_data", "expected_validation_db", "exp_status", "exp_bag_status", "exp_last_booking_status");
        String processed = requestBodyTemplate;
        for (Map.Entry<String, String> e : testData.entrySet()) {
            if (metaCols.contains(e.getKey())) continue;
            processed = processed.replace("${" + e.getKey() + "}", DynamicDataHelper.resolveDynamicValue(e.getValue()));
        }
        String requestBody = processed;

        // --- GỌI API CREATE BOOKING ---
        Response response = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCaptureCreate))
                .when()
                .post(BASE_URL + CREATE_BOOKING_BATCH_ENDPOINT)
                .then()
                .extract().response();

        ITestResult rr = Reporter.getCurrentTestResult();
        rr.setAttribute("requestLogCreate", reqWriterCreate.toString());
        rr.setAttribute("responseLogCreate", response.getBody().prettyPrint());

        // --- GỌI API DB BOOKINGS ---
        String partnerUid = testData.get("partner_uid");
        String courseUid  = testData.get("course_uid");
        String bookingDateResolved = DynamicDataHelper.resolveDynamicValue(testData.get("booking_date"));

        Response dbResp = given()
                .header("Authorization", authToken)
                .header("Accept", "application/json")
                .queryParam("from", bookingDateResolved)
                .queryParam("to", bookingDateResolved)
                .queryParam("table_name", "bookings")
                .queryParam("page", 1)
                .queryParam("limit", 1000)
                .queryParam("partner_uid", partnerUid)
                .queryParam("course_uid",  courseUid)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, reqCaptureDb))
                .when()
                .get(BASE_URL + DB_BOOKING_LIST_ENDPOINT)
                .then()
                .extract().response();

        assertEquals(dbResp.getStatusCode(), 200, "API DB trả mã khác 200");
        rr.setAttribute("requestLogDb", reqWriterDb.toString());
        rr.setAttribute("responseLogDb", dbResp.getBody().prettyPrint());

        // --- TÌM RECORD VỪA TẠO ---
        String createdUid = response.jsonPath().getString("[0].uid");
        assertNotNull(createdUid, "Không lấy được booking uid sau khi create");
        System.out.println("createdUid = " + createdUid);

        java.util.List<java.util.Map<String,Object>> records = dbResp.jsonPath().getList("data");
        assertNotNull(records, "DB không có field 'data'");
        org.testng.Assert.assertFalse(records.isEmpty(), "DB 'data' rỗng cho ngày " + bookingDateResolved);

        java.util.Map<String,Object> target = records.stream()
                .filter(r -> createdUid.equals(String.valueOf(r.get("uid"))))
                .findFirst().orElse(null);

        assertNotNull(target, "Không tìm thấy booking có uid = " + createdUid + " trong DB");

        String targetJson = new Gson().toJson(target);
        rr.setAttribute("dbTarget", targetJson);
        System.out.println("[DB] picked record = " + targetJson);

        // --- VALIDATION DB ---
        String expectedDbRaw = testData.get("expected_validation_db");
        Map<String,Object> expectedDbAsserts = resolveExpectedDb(expectedDbRaw, testData, createdUid);

        // Sanity check cơ bản
        org.testng.Assert.assertEquals(String.valueOf(target.get("partner_uid")), partnerUid, "partner_uid không khớp DB");
        org.testng.Assert.assertEquals(String.valueOf(target.get("course_uid")),  courseUid,  "course_uid không khớp DB");
        org.testng.Assert.assertEquals(String.valueOf(target.get("booking_date")), bookingDateResolved, "booking_date không khớp DB");
        org.testng.Assert.assertEquals(String.valueOf(target.get("uid")), createdUid, "uid DB khác uid create");

        // Assert chi tiết theo expected_validation_db
        if (expectedDbAsserts != null && !expectedDbAsserts.isEmpty()) {
            Map<String,Object> normalized = normalizeJsonStrings(target);
            rr.setAttribute("dbTargetNormalized", new Gson().toJson(normalized));
            rr.setAttribute("expectedDbAsserts", new Gson().toJson(expectedDbAsserts));

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
    ) throws java.io.IOException {
        if (expectedDbRaw == null || expectedDbRaw.trim().isEmpty()) return null;

        String content = expectedDbRaw.trim();

        // Nếu ô chỉ là tên file -> đọc file template
        if (!content.startsWith("{") && !content.startsWith("[")) {
            java.nio.file.Path p = java.nio.file.Paths.get(
                    System.getProperty("user.dir"),
                    "src","main","resources","input_json_file","booking","db_assert_templates",
                    content
            );
            content = new String(java.nio.file.Files.readAllBytes(p), java.nio.charset.StandardCharsets.UTF_8);
        }

        // Thay placeholder từ Excel
        for (java.util.Map.Entry<String,String> e : testData.entrySet()) {
            content = content.replace("${" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        // Thay uid & token động kiểu {{TODAY}}
        content = content.replace("$created_uid", createdUid);
        content = common.utilities.DynamicDataHelper.resolveDynamicValue(content);

        // (Optional) log để nhìn expected sau khi resolve
        org.testng.Reporter.getCurrentTestResult().setAttribute("expectedDbResolved", content);

        java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<java.util.Map<String,Object>>(){}.getType();
        java.util.Map<String,Object> root = new com.google.gson.Gson().fromJson(content, t);

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