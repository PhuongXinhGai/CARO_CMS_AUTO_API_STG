package helpers;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import listeners.TestListener; // Cần import TestListener
import tests.models.ActionResult;

public class ReportHelper {

    /**
     * Ghi lại chi tiết một bước thực thi API vào báo cáo Extent Reports.
     * @param stepName Tên của bước (ví dụ: "Bước 1: Login").
     * @param actionResult Kết quả trả về từ một phương thức Action.
     */
    public static void logApiActionStep(String stepName, ActionResult actionResult) {
        // Lấy đối tượng ExtentTest hiện tại từ TestListener
        ExtentTest extentTest = TestListener.getExtentTest();

        if (extentTest != null && actionResult != null) {
            // Tạo một node con trong báo cáo cho bước này
            ExtentTest stepNode = extentTest.createNode(stepName);

            // Ghi log request
            String requestLog = actionResult.getRequestLog();
            if (requestLog != null && !requestLog.isEmpty()) {
                stepNode.info(MarkupHelper.createCodeBlock(requestLog));
            }

            // Ghi log response
            String responseLog = actionResult.getResponse().getBody().prettyPrint();
            if (responseLog != null && !responseLog.isEmpty()) {
                stepNode.info(MarkupHelper.createCodeBlock(responseLog, CodeLanguage.JSON));
            }
        }
    }
}