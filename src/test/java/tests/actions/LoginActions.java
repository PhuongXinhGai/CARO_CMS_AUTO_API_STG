package tests.actions;

import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.io.output.WriterOutputStream;
import tests.test_config.TestConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static common.utilities.Constants.LOGIN_ENDPOINT;
import static io.restassured.RestAssured.given;

public class LoginActions extends TestConfig {

    /**
     * Thực hiện hành động gọi API Login và trả về response thô.
     * @param user_name Tên đăng nhập
     * @param password Mật khẩu
     * @return Đối tượng Response từ RestAssured
     */
    public Response loginAndGetResponse(String user_name, String password) throws IOException {
        // --- Logic được "copy" từ LoginTest.java ---

        // Chuẩn bị để ghi log (chỉ cần cho request)
        StringWriter requestWriter = new StringWriter();
        PrintStream requestCapture = new PrintStream(new WriterOutputStream(requestWriter), true);

        // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/user/login_request_template.json";
        String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String requestBody = requestBodyTemplate
                .replace("${userName}", user_name)
                .replace("${password}", password);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .filter(new RequestLoggingFilter(LogDetail.ALL, true, requestCapture))
                .when()
                .post(BASE_URL + LOGIN_ENDPOINT)
                .then()
//                .log().all()
                .extract().response();

        // Gắn request log vào response để các bước sau có thể lấy ra
        // Đây là một "mẹo" nhỏ để truyền log đi
        response.then().header("X-Request-Log", requestWriter.toString());

        return response;
    }
}