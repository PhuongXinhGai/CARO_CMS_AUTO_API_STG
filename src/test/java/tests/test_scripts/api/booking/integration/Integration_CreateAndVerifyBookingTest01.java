package tests.test_scripts.api.booking.integration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.utilities.DynamicDataHelper;
import common.utilities.ExcelUtils;
import helpers.ReportHelper;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.actions.BookingActions;
import tests.actions.LoginActions;
import tests.models.ActionResult;
import tests.test_config.TestConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class Integration_CreateAndVerifyBookingTest01 extends TestConfig {

    // Khai báo các "người thực thi hành động" và các công cụ
    private LoginActions loginActions;
    private BookingActions bookingActions;
    private Gson gson;

    // Khai báo đường dẫn đến các file Excel để dễ quản lý
    private String loginDataFile;
    private String createBookingDataFile;
    private String getListDataFile;

    // Annotation @BeforeClass sẽ chạy một lần duy nhất trước khi tất cả các test trong class này bắt đầu
    @BeforeClass
    public void setup() {
        // Khởi tạo các đối tượng cần thiết
        loginActions = new LoginActions();
        bookingActions = new BookingActions();
        gson = new Gson();

        // Định nghĩa đường dẫn file tập trung ở một nơi
        String baseFilePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/";
        loginDataFile = baseFilePath + "user/Login.xlsx";
        createBookingDataFile = baseFilePath + "booking/Create_Booking_Batch.xlsx";
        getListDataFile = baseFilePath + "booking/Get_Booking_List_Select.xlsx";
    }

    @DataProvider(name = "integrationFlows")
    public Object[][] getFlows() {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/booking/IntegrationFlows.xlsx";
        return ExcelUtils.getTestData(filePath, "flows");
    }

    @Test(dataProvider = "integrationFlows")
    public void testCreateAndVerifyBookingFlow(String flow_id, String flow_description, String login_case_id, String create_booking_case_id, String get_list_case_id, String final_validation_data) throws IOException {

        System.out.println("====== BẮT ĐẦU LUỒNG TEST: " + flow_id + " - " + flow_description + " ======");

        // --- LẤY DỮ LIỆU TĨNH TỪ CÁC FILE EXCEL ---
        Map<String, String> loginData = ExcelUtils.getTestCaseData(loginDataFile, "testcase", login_case_id);
        Map<String, String> createBookingData = ExcelUtils.getTestCaseData(createBookingDataFile, "testcase", create_booking_case_id);
        Map<String, String> getListData = ExcelUtils.getTestCaseData(getListDataFile, "testcase", get_list_case_id);

        /// === BƯỚC 1: LOGIN ===
        System.out.println("--- Bước 1: Đang thực hiện Login...");
        ActionResult loginResult = loginActions.loginAndGetResponse(loginData.get("user_name"), loginData.get("password"));
        Response loginResponse = loginResult.getResponse();
        ReportHelper.logApiActionStep("Bước 1: Login to get token", loginResult);

        assertEquals(loginResponse.getStatusCode(), 200, "Bước 1: Login thất bại!");
        String authToken = loginResponse.jsonPath().getString("token");

        // === BƯỚC 2: TẠO BOOKING ===
        System.out.println("--- Bước 2: Đang tạo Booking mới...");
        String rawBookingListJson = createBookingData.get("booking_list");
        assertNotNull(rawBookingListJson, "Không tìm thấy cột 'booking_list' trong file Create_Booking_Batch.xlsx cho test case ID: " + create_booking_case_id);
        String resolvedBookingJson = DynamicDataHelper.resolveDynamicValue(rawBookingListJson);
        ActionResult createBookingResult = bookingActions.createBookingBatch(authToken, resolvedBookingJson);
        Response createBookingResponse = createBookingResult.getResponse();

        ReportHelper.logApiActionStep("Bước 2: Create new bookings", createBookingResult);
        assertEquals(createBookingResponse.getStatusCode(), 200, "Bước 2: Tạo booking thất bại!");

        List<String> createdBookingUids = createBookingResponse.jsonPath().getList("uid");
        assertNotNull(createdBookingUids, "UID của booking vừa tạo không được null.");
        assertFalse(createdBookingUids.isEmpty(), "Danh sách UID của booking vừa tạo không được rỗng.");
        System.out.println("    -> Đã tạo thành công " + createdBookingUids.size() + " booking với UIDs: " + createdBookingUids);

        // === BƯỚC 3: LẤY DANH SÁCH VÀ NGHIỆM THU ---
        System.out.println("--- Bước 3: Đang lấy danh sách Booking để nghiệm thu...");
        Map<String, String> getListParams = new HashMap<>();
        getListParams.put("partner_uid", getListData.get("partner_uid"));
        getListParams.put("course_uid", getListData.get("course_uid"));
        getListParams.put("sort_by", getListData.get("sort_by"));
        getListParams.put("sort_dir", getListData.get("sort_dir"));
        getListParams.put("is_single_book", getListData.get("is_single_book"));
        getListParams.put("is_ignore_tournament_booking", getListData.get("is_ignore_tournament_booking"));
        String resolvedBookingDate = DynamicDataHelper.resolveDynamicValue(getListData.get("booking_date"));
        getListParams.put("booking_date", resolvedBookingDate);

        ActionResult getListResult = bookingActions.getBookingList(authToken, getListParams);
        Response getListResponse = getListResult.getResponse();

        ReportHelper.logApiActionStep("Bước 3: Get booking list to verify", getListResult);
        assertEquals(getListResponse.getStatusCode(), 200, "Bước 3: Lấy danh sách booking thất bại!");

        List<String> actualBookingUidsInList = getListResponse.jsonPath().getList("data.uid");
        System.out.println("    -> Tìm thấy " + actualBookingUidsInList.size() + " booking trong danh sách.");

        // === NGHIỆM THU CUỐI CÙNG ===
        System.out.println("--- Bước 4: Đang thực hiện nghiệm thu cuối cùng...");
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> validationRules = gson.fromJson(final_validation_data, type);

        for (Map.Entry<String, Object> rule : validationRules.entrySet()) {
            if ("created_bookings_in_list".equalsIgnoreCase(rule.getKey())) {
                boolean expected = (Boolean) rule.getValue();
                boolean actual = actualBookingUidsInList.containsAll(createdBookingUids);
                assertEquals(actual, expected,"Nghiệm thu cuối cùng thất bại! Kỳ vọng các booking vừa tạo có trong danh sách là: " + expected + ", nhưng thực tế là: " + actual);
                System.out.println("    -> Nghiệm thu 'created_bookings_in_list' thành công!");
            }
        }
        System.out.println("====== KẾT THÚC LUỒNG TEST: " + flow_id + " - THÀNH CÔNG ======");
    }
}