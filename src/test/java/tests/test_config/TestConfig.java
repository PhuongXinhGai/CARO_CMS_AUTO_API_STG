package tests.test_config;

import com.google.gson.Gson;
import common.utilities.ConfigReader;
import org.testng.annotations.BeforeClass;
import tests.actions.BookingActions;
import tests.actions.LoginActions;

// Đây là lớp cấu hình cơ sở mà TẤT CẢ các class test sẽ kế thừa
public class TestConfig {

    // BASE_URL được đọc một lần và dùng chung
    protected static String BASE_URL = ConfigReader.getProperty("base_url");
    protected static String API_KEY = ConfigReader.getProperty("api_Key");


    // Khai báo các đối tượng dùng chung với 'protected'
    // để các class con (như IntegrationTest, LoginTest...) có thể kế thừa và sử dụng
    protected Gson gson;
    protected LoginActions loginActions;
    protected BookingActions bookingActions;

    // Khai báo các biến lưu đường dẫn file để dễ quản lý
    protected String loginDataFile;
    protected String createBookingDataFile;
    protected String getListDataFile;
    protected String getBookingPriceFile;

    protected String integrationFlowsFile;
    protected String integrationPriceCheckFlowsFile;


    /**
     * Phương thức này sẽ được TestNG tự động gọi một lần duy nhất
     * TRƯỚC KHI bất kỳ method @Test nào trong một class test được chạy.
     * Nhiệm vụ của nó là khởi tạo các đối tượng và thiết lập các cấu hình cần thiết.
     */
    @BeforeClass
    public void initialize() {
        System.out.println("====== Khởi tạo các đối tượng dùng chung (@BeforeClass) ======");
        // Khởi tạo các đối tượng "công cụ"
        gson = new Gson();

        // Khởi tạo các "người thực thi hành động"
        loginActions = new LoginActions();
        bookingActions = new BookingActions();

        // Định nghĩa đường dẫn đến các file dữ liệu.
        // Việc tập trung ở đây giúp dễ dàng thay đổi nếu cấu trúc thư mục thay đổi.
        String baseFilePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/";
        loginDataFile = baseFilePath + "user/Login.xlsx";
        createBookingDataFile = baseFilePath + "booking/Create_Booking_Batch.xlsx";
        getListDataFile = baseFilePath + "booking/Get_Booking_List_Select.xlsx";
        getBookingPriceFile = baseFilePath + "booking/Get_Booking_Price.xlsx"; // <-- THÊM MỚI

        // --- Các file dữ liệu "Điều phối" Integration ---
        integrationFlowsFile = baseFilePath + "booking/IntegrationFlows.xlsx";
        integrationPriceCheckFlowsFile = baseFilePath + "booking/IntegrationFlows_WithPriceCheck.xlsx";
    }
}