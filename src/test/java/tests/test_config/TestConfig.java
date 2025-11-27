package tests.test_config;

import com.google.gson.Gson;
import common.utilities.ConfigReader;
import org.testng.annotations.BeforeClass;

// Đây là lớp cấu hình cơ sở mà TẤT CẢ các class test sẽ kế thừa
public class TestConfig {

    // BASE_URL được đọc một lần và dùng chung
    protected static String BASE_URL = ConfigReader.getProperty("base_url");
    protected static String BASE_URL_MAINTENANCE = ConfigReader.getProperty("base_url_maintenance");

    protected static String API_KEY = ConfigReader.getProperty("api_key");

}