package framework.core;

import common.utilities.ExcelUtils;
import org.testng.ITestContext;
import com.aventstack.extentreports.ExtentTest;


import java.util.Map;

public interface FlowRunnable {

    /**
     * Chạy 1 test case cụ thể theo caseId
     * @param caseId  mã test case trong Excel sheet của API đó
     * @param ctx     context chung (dùng để chia sẻ token, uid,...)
     * @param logger  ExtentTest log của step hiện tại
     */
    void runCase(String caseId, ITestContext ctx, ExtentTest logger) throws Exception;
    /**
     * Hàm tiện ích đọc 1 dòng từ Excel theo caseId (dùng lại ở mọi test)
     */
    default Map<String, String> findRowByCaseId(String excelPath, String sheetName, String caseId) throws Exception {
        Object[][] all = ExcelUtils.readSheetAsMaps(excelPath, sheetName);
        for (Object[] obj : all) {
            Map<String, String> row = (Map<String, String>) obj[0];
            if (row.get("tc_id").equalsIgnoreCase(caseId)) return row;
        }
        throw new IllegalArgumentException("❌ Không tìm thấy caseId: " + caseId);
    }
}
