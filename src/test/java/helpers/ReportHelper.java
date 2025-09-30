package helpers;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import listeners.TestListener; // Cần import TestListener
import org.testng.ITestContext;
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

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void logContext(ITestContext ctx, String... keys) {
        ExtentTest test = TestListener.getExtentTest();
        for (String k : keys) {
            Object v = ctx.getAttribute(k);
            if (v == null) continue;
            String s = String.valueOf(v);
            if (looksLikeJson(s)) {
                test.info(k + ":");
                test.info(MarkupHelper.createCodeBlock(prettyJson(s), CodeLanguage.JSON));
            } else {
                test.info(k + ": " + s);
            }
        }
    }

    private static boolean looksLikeJson(String s) {
        s = s.trim();
        return (s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"));
    }
    private static String prettyJson(String s) {
        try { return GSON.toJson(GSON.fromJson(s, Object.class)); }
        catch (Exception ignore) { return s; }
    }

}