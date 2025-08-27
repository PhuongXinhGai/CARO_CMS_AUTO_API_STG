package tests.test_config;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.google.gson.Gson;
import common.utilities.ConfigReader;
import common.utilities.DynamicDataHelper;
import common.utilities.ExcelUtils;
import io.restassured.response.Response;
import listeners.IntegrationListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import tests.actions.BookingActions;
import tests.actions.LoginActions;
import tests.annotations.IntegrationFlow;
import tests.models.ActionResult;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class TestConfig {

    // BASE_URL và Gson được khởi tạo một lần
    protected static String BASE_URL = ConfigReader.getProperty("base_url");
    protected Gson gson;

    // --- KHAI BÁO CÁC BIẾN DÙNG CHUNG CHO LUỒNG TÍCH HỢP ---
    // 'protected' để các class con (như IntegrationTest) có thể truy cập
    protected String authToken;
    protected String partnerUid;
    protected String courseUid;
    protected List<String> createdBookingUids;
    protected List<String> actualBookingUidsInList;

    // Khai báo các đối tượng Actions
    protected LoginActions loginActions;
    protected BookingActions bookingActions;

    // Khai báo đường dẫn đến các file Excel
    protected String loginDataFile;
    protected String createBookingDataFile;
    protected String getListDataFile;

    // Sử dụng @BeforeClass để khởi tạo các đối tượng dùng chung, tránh lặp lại
    @BeforeClass
    public void initialize() {
        loginActions = new LoginActions();
        bookingActions = new BookingActions();
        gson = new Gson();

        // Định nghĩa đường dẫn file tập trung ở một nơi
        String baseFilePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/";
        loginDataFile = baseFilePath + "user/Login.xlsx";
        createBookingDataFile = baseFilePath + "booking/Create_Booking_Batch.xlsx";
        getListDataFile = baseFilePath + "booking/Get_Booking_List_Select.xlsx";
    }

    private void logApiStepInReport(String stepName, ActionResult actionResult, boolean isSuccess) {
        ExtentTest parentTest = IntegrationListener.getExtentTest();
        if (parentTest != null) {
            ExtentTest stepNode = parentTest.createNode(stepName);
            if (isSuccess) {
                stepNode.pass("Step executed successfully.");
            } else {
                stepNode.fail("Step failed. Status code: " + actionResult.getResponse().getStatusCode());
            }
            stepNode.info(MarkupHelper.createCodeBlock(actionResult.getRequestLog()));
            stepNode.info(MarkupHelper.createCodeBlock(actionResult.getResponse().getBody().prettyPrint(), CodeLanguage.JSON));
        }
    }

    /**
     * Phương thức này sẽ tự động chạy TRƯỚC MỖI method @Test được cung cấp dữ liệu bởi DataProvider.
     * Nó sẽ kiểm tra xem class test có được "dán nhãn" @IntegrationFlow hay không.
     * Nếu có, nó sẽ thực hiện toàn bộ luồng API để chuẩn bị dữ liệu.
     * @param method Đối tượng chứa thông tin về method @Test sắp được chạy.
     * @param testData Mảng Object chứa dữ liệu của 1 dòng từ DataProvider.
     */
    @BeforeMethod
    public void setupIntegrationFlow(Method method, Object[] testData) throws IOException {
        Class<?> testClass = method.getDeclaringClass();

        // Chỉ thực thi logic này nếu class được dán nhãn @IntegrationFlow
        if (testClass.isAnnotationPresent(IntegrationFlow.class)) {

            // --- LẤY DỮ LIỆU ĐIỀU PHỐI TỪ DATAPROVIDER ---
            if (testData.length == 0) return; // Bỏ qua nếu không có dữ liệu
            String flow_id = (String) testData[0];
            String loginCaseId = (String) testData[2];
            String createCaseId = (String) testData[3];
            String getListCaseId = (String) testData[4];

            System.out.println("\n====== BẮT ĐẦU SETUP CHO LUỒNG: " + flow_id + " ======");

            // --- LẤY DỮ LIỆU CHI TIẾT CHO TỪNG BƯỚC ---
            Map<String, String> loginData = ExcelUtils.getTestCaseData(loginDataFile, "testcase", loginCaseId);
            Map<String, String> createData = ExcelUtils.getTestCaseData(createBookingDataFile, "testcase", createCaseId);
            Map<String, String> getListData = ExcelUtils.getTestCaseData(getListDataFile, "testcase", getListCaseId);

            // === BƯỚC A: LOGIN ===
            System.out.println("--- Setup Step 1: Login...");
            ActionResult loginResult = loginActions.loginAndGetResponse(loginData.get("user_name"), loginData.get("password"));
            logApiStepInReport("Step 1: Login", loginResult, loginResult.getResponse().getStatusCode() == 200);
            assertEquals(loginResult.getResponse().getStatusCode(), 200, "Setup thất bại tại Bước 1: Login");
            Response loginResponse = loginResult.getResponse();
            this.authToken = loginResponse.jsonPath().getString("token");
            this.partnerUid = loginResponse.jsonPath().getString("data.partner_uid"); // Sửa lại đường dẫn nếu cần
            this.courseUid = loginResponse.jsonPath().getString("data.course_uid"); // Sửa lại đường dẫn nếu cần

            // === BƯỚC B: CREATE BOOKING ===
            System.out.println("--- Setup Step 2: Create Booking...");
            String rawBookingJson = createData.get("booking_list");
            String resolvedBookingJson = DynamicDataHelper.resolveDynamicValue(rawBookingJson);
            ActionResult createResult = bookingActions.createBookingBatch(this.authToken, resolvedBookingJson);
            logApiStepInReport("Step 2: Create Booking", createResult, createResult.getResponse().getStatusCode() == 200);
            assertEquals(createResult.getResponse().getStatusCode(), 200, "Setup thất bại tại Bước 2: Create Booking");
            this.createdBookingUids = createResult.getResponse().jsonPath().getList("uid");

            // === BƯỚC C: GET LIST ===
            System.out.println("--- Setup Step 3: Get Booking List...");
            Map<String, String> getListParams = new HashMap<>();
            getListParams.put("partner_uid", this.partnerUid); // Dùng UID từ bước Login
            getListParams.put("course_uid", this.courseUid);   // Dùng UID từ bước Login
            getListParams.put("sort_by", getListData.get("sort_by"));
            getListParams.put("sort_dir", getListData.get("sort_dir"));
            getListParams.put("is_single_book", getListData.get("is_single_book"));
            String resolvedBookingDate = DynamicDataHelper.resolveDynamicValue(getListData.get("booking_date"));
            getListParams.put("booking_date", resolvedBookingDate);

            ActionResult getListResult = bookingActions.getBookingList(this.authToken, getListParams);
            logApiStepInReport("Step 3: Get Booking List", getListResult, getListResult.getResponse().getStatusCode() == 200);
            assertEquals(getListResult.getResponse().getStatusCode(), 200, "Setup thất bại tại Bước 3: Get List");
            this.actualBookingUidsInList = getListResult.getResponse().jsonPath().getList("data.uid");

            System.out.println("====== SETUP LUỒNG HOÀN TẤT ======");
        }
    }
}