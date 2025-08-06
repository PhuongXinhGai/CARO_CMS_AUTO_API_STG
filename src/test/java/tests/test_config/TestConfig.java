package test.test_config;

import com.google.gson.Gson;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.IOException;

import static common.utilities.RequestHelper.getRequestNoAuth;
import static org.testng.Assert.assertNotNull;
import common.utilities.ConfigReader;
import common.utilities.FileHelper;
import common.models.login.LoginResponse;


public class TestConfig {
    protected static String BASE_URL = ConfigReader.get("base_url");

    public static String getToken() throws IOException {
        String BASE_URL = ConfigReader.get("base_url") + "/golf-cms/api/user/login";
        String requestLoginBody = FileHelper.readJsonFileAsString("src/main/resources/input_json_file/login/case1-login-success.json");

        Response response = getRequestNoAuth(BASE_URL)
                .contentType(ContentType.JSON)
                .body(requestLoginBody)
                .post(BASE_URL);

        System.out.println("👉 Response login: " + response.asString());
        Gson gson = new Gson();
        LoginResponse loginResponse = gson.fromJson(response.getBody().asString(), LoginResponse.class);

        // Gọi phương thức thông qua object loginResponse
        assertNotNull(loginResponse.getToken());

        String token = loginResponse.getToken();
        if (token == null) {
            throw new RuntimeException("❌ Không lấy được token! Response:\n" + response.asString());
        }
        return token;
    }
}
