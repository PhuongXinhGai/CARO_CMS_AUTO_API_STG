package tests.test_config;

import com.google.gson.Gson;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.IOException;

import static common.utilities.RequestHelper.getRequestNoAuth;
import static org.testng.Assert.assertNotNull;
import common.utilities.ConfigReader;
import common.utilities.FileHelper;
import common.models.login.LoginResponse;
import org.testng.annotations.BeforeMethod;


public class TestConfig {
    protected static String BASE_URL = ConfigReader.getProperty("base_url");

    @BeforeMethod
    public static void setupBeforMethod() throws IOException{
//        step 1
//        đọc file excel file login với case được đánh dấu inter
//        gọi api login
//        step 2
//        đọc file ... các api chuẩn bị trước


//inter
//        luu lại response vào bộ nhớ đệm
//        đến api cuối thì lọc lấy giá trị cần để gửi request
    }
//    Tao 1 class chayj trước các case phuc vu cho intergation
//    data test
//      đánh dấu trong file các happy case phục vụ inter
}
