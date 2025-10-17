package helpers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import listeners.TestListener; // Cần import TestListener
import org.testng.ITestContext;
import tests.models.ActionResult;

import java.util.Set;

public class ReportHelper {
    /**
     * Tạo node cha cho mỗi Flow trong ExtentReports (chạy Integration)
     * @param flowId   Mã flow (vd: FLOW_001)
     * @param flowDesc Mô tả flow
     * @return ExtentTest node để log step con
     */
    public static ExtentTest startFlow(String flowId, String flowDesc) {
        try {
            ExtentReports extent = TestListener.getExtentReports();

            // ✅ 1 dòng duy nhất: title + desc hiển thị cùng
            ExtentTest flowNode = extent.createTest("🌊 " + flowId + " – " + flowDesc);
            flowNode.assignCategory(flowId); // thêm category để mô tả hiển thị rõ hơn
            flowNode.info("🚀 Bắt đầu Flow: " + flowId);

            return flowNode;

        } catch (Exception e) {
            System.out.println("💥 Lỗi khi tạo node ExtentReports: " + e.getMessage());
            return null;
        }
    }

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

    // Log dữ liệu từng key context lên ExtentReports
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

    // Log tất cả dữ liệu context lên ExtentReports
    public static void logAllContext(ITestContext ctx) {
        ExtentTest testLogger = TestListener.getExtentTest();
        if (ctx == null || testLogger == null) return;

        testLogger.info("===== 🧩 DỮ LIỆU TRONG CONTEXT =====");

        Set<String> keys = ctx.getAttributeNames();
        int count = 0;

        for (String key : keys) {
            Object val = ctx.getAttribute(key);
            testLogger.info("🔹 " + key + " = " + (val != null ? val.toString() : "null"));
            count++;
        }

        if (count == 0) {
            testLogger.info("⚠️ Không có dữ liệu nào trong context.");
        } else {
            testLogger.info("===== ✅ Tổng số key trong context: " + count + " =====");
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