package tests.actions;

import common.utilities.DynamicDataHelper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.output.WriterOutputStream;
import tests.test_config.TestConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static common.utilities.Constants.BOOKING_PRICE_ENDPOINT;
import static common.utilities.Constants.CREATE_BOOKING_BATCH_ENDPOINT;
import static common.utilities.Constants.BOOKING_LIST_SELECT_ENDPOINT;
import static io.restassured.RestAssured.given;

public class BookingActions extends TestConfig {
    /**
     * Thực hiện hành động tạo booking theo lô.
     * @param authToken Token xác thực
     * @param booking_list_json Chuỗi JSON chứa danh sách booking
     * @return Đối tượng Response từ RestAssured
     */
    public Response createBookingBatch(String authToken, String booking_list_json) throws IOException {
        // --- Chuẩn bị ghi log (giữ nguyên) ---
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- Xử lý dữ liệu động (ví dụ: {{TODAY}}) ---
        String resolvedBookingListJson = DynamicDataHelper.resolveDynamicValue(booking_list_json);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/booking/create_booking_batch_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        // Thay thế biến trong template bằng dữ liệu đã xử lý
        String requestBody = requestBodyTemplate
                .replace("${bookingListJson}",resolvedBookingListJson);

        Response response = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + CREATE_BOOKING_BATCH_ENDPOINT)
                .then()
                .extract().response();

        response.then().header("X-Request-Log", requestWriter.toString());
        return response;
    }

    /**
     * Thực hiện hành động lấy danh sách booking.
     * @param authToken Token xác thực
     * @param partnerUid Partner UID
     * @param courseUid Course UID
     * @param bookingDate Ngày booking
     * @return Đối tượng Response từ RestAssured
     */
    public Response getBookingList(String authToken, String partnerUid, String courseUid, String bookingDate) throws IOException {
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + authToken)
                .addFilter(new RequestLoggingFilter(requestCapture))
                .build();

        Response response = given()
                .spec(requestSpec)
                .queryParam("partner_uid", partnerUid)
                .queryParam("course_uid", courseUid)
                .queryParam("booking_date", bookingDate)
                .when()
                .get(BASE_URL + BOOKING_LIST_SELECT_ENDPOINT)
                .then()
                .extract().response();

        response.then().header("X-Request-Log", requestWriter.toString());
        return response;
    }
}