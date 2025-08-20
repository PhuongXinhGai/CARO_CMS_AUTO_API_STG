package tests.actions;

import common.utilities.DynamicDataHelper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.output.WriterOutputStream;
import tests.test_config.TestConfig; // Kế thừa TestConfig để có BASE_URL

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static common.utilities.Constants.CREATE_BOOKING_BATCH_ENDPOINT;
import static common.utilities.Constants.BOOKING_LIST_SELECT_ENDPOINT;
import static io.restassured.RestAssured.given;

// Kế thừa TestConfig giúp class này có thể truy cập BASE_URL
public class BookingActions extends TestConfig {

    /**
     * Hành động: Tạo booking theo lô (batch).
     * Phương thức này chỉ nhận dữ liệu đã được xử lý và gửi đi.
     * @param authToken Token xác thực.
     * @param finalBookingListJson Chuỗi JSON của booking_list đã được xử lý hoàn chỉnh.
     * @return Đối tượng Response từ RestAssured.
     * @throws IOException Nếu không đọc được file JSON template.
     */
    public Response createBookingBatch(String authToken, String finalBookingListJson) throws IOException {
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- Đọc và chuẩn bị Request Body ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/booking/create_booking_batch_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String requestBody = requestBodyTemplate.replace("${bookingListJson}", finalBookingListJson);

        // --- Gửi Request ---
        Response response = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(requestCapture))
                .when()
                .post(BASE_URL + CREATE_BOOKING_BATCH_ENDPOINT)
                .then()
                .extract().response();

        response.then().header("X-Request-Log", requestWriter.toString());
        return response;
    }

    /**
     * Hành động: Lấy danh sách booking.
     * Bóc tách từ GetBookingListSelectTest.java.
     * @param authToken Token xác thực.
     * @param params Một Map chứa tất cả các query parameters.
     * @return Đối tượng Response chứa kết quả trả về từ API.
     */
    public Response getBookingList(String authToken, Map<String, String> params) {
        // --- Chuẩn bị ghi log ---
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- Xây dựng và gửi Request ---
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addHeader("Authorization", authToken)
                .addFilter(new RequestLoggingFilter(requestCapture))
                .addQueryParams(params) // Thêm tất cả các tham số từ Map
                .build();

        Response response = given()
                .spec(requestSpec)
                .when()
                .get(BASE_URL + BOOKING_LIST_SELECT_ENDPOINT)
                .then()
                .extract().response();

        // Đính kèm log request vào response
        response.then().header("X-Request-Log", requestWriter.toString());
        return response;
    }
}