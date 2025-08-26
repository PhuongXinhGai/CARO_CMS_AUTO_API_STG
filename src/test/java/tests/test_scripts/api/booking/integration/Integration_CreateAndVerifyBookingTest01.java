package tests.test_scripts.api.booking.integration;

import com.google.gson.reflect.TypeToken;
import common.utilities.ExcelUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.annotations.IntegrationFlow; // <-- Import "nhãn dán"
import tests.test_config.TestConfig;

import java.lang.reflect.Type;
import java.util.Map;

import static org.testng.Assert.*;

// "Dán nhãn" @IntegrationFlow lên class và chỉ định file dữ liệu "điều phối"
@IntegrationFlow(flowFile = "/src/main/resources/input_excel_file/booking/IntegrationFlows.xlsx")
public class Integration_CreateAndVerifyBookingTest01 extends TestConfig {

    /**
     * DataProvider này có nhiệm vụ đọc file "điều phối" (IntegrationFlows.xlsx)
     * và cung cấp từng dòng (từng luồng) cho method @Test.
     * @return Một mảng 2 chiều chứa dữ liệu của các luồng test.
     */
    @DataProvider(name = "integrationFlows")
    public Object[][] getFlows() {
        // Lấy thông tin từ "nhãn dán" @IntegrationFlow của chính class này
        IntegrationFlow flowInfo = this.getClass().getAnnotation(IntegrationFlow.class);
        String flowFilePath = System.getProperty("user.dir") + flowInfo.flowFile();
        return ExcelUtils.getTestData(flowFilePath, flowInfo.sheetName());
    }

    /**
     * Method test này sẽ được gọi lặp lại cho MỖI DÒNG trong file IntegrationFlows.xlsx.
     * Trước khi method này chạy, @BeforeMethod trong TestConfig đã thực thi xong toàn bộ luồng API.
     * Nhiệm vụ duy nhất của nó là thực hiện nghiệm thu cuối cùng.
     */
    @Test(dataProvider = "integrationFlows")
    public void testBookingVerificationFlow(String flow_id, String flow_description, String login_case_id, String create_booking_case_id, String get_list_case_id, String final_validation_data) {

        System.out.println("====== BẮT ĐẦU NGHIỆM THU CHO LUỒNG: " + flow_id + " ======");

        // === NGHIỆM THU CUỐI CÙNG ===
        // Toàn bộ dữ liệu cần thiết (this.createdBookingUids, this.actualBookingUidsInList)
        // đã được chuẩn bị sẵn sàng bởi @BeforeMethod trong lớp cha TestConfig.

        // Kiểm tra để đảm bảo dữ liệu chuẩn bị không bị null
        assertNotNull(this.createdBookingUids, "Dữ liệu 'createdBookingUids' không được null sau khi chạy setup.");
        assertNotNull(this.actualBookingUidsInList, "Dữ liệu 'actualBookingUidsInList' không được null sau khi chạy setup.");

        // Parse chuỗi JSON chứa các quy tắc nghiệm thu cuối cùng
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> validationRules = gson.fromJson(final_validation_data, type);

        // Duyệt qua từng quy tắc và thực hiện nghiệm thu
        for (Map.Entry<String, Object> rule : validationRules.entrySet()) {

            if ("created_bookings_in_list".equalsIgnoreCase(rule.getKey())) {
                boolean expected = (Boolean) rule.getValue();
                boolean actual = this.actualBookingUidsInList.containsAll(this.createdBookingUids);

                assertEquals(actual, expected,"Nghiệm thu cuối cùng thất bại! Kỳ vọng các booking vừa tạo có trong danh sách là: " + expected + ", nhưng thực tế là: " + actual);
                System.out.println("    -> Nghiệm thu 'created_bookings_in_list' thành công!");
            }

            // TODO: Thêm các quy tắc nghiệm thu cuối cùng khác ở đây nếu cần
            // Ví dụ: if ("total_bookings".equalsIgnoreCase(rule.getKey())) { ... }
        }

        System.out.println("====== NGHIỆM THU LUỒNG HOÀN TẤT: " + flow_id + " ======");
    }
}