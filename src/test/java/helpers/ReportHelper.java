package helpers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import listeners.TestListener; // Cần import TestListener
import listeners.FlowTestListener;
import org.testng.ITestContext;
import tests.models.ActionResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ReportHelper {

    /**
     * ✅ Tạo node cha cho mỗi Flow trong ExtentReports (chạy Integration)
     */
    public static ExtentTest startFlow(String flowId, String flowDesc) {
        try {
            ExtentReports extent = FlowTestListener.getExtentReports();

            // 🔹 Kiểm tra flow đã tồn tại chưa (tránh duplicate)
            ExtentTest existing = FlowTestListener.getExtentTestMap().get(flowId);
            if (existing != null) return existing;

            // 🔹 1 dòng duy nhất: title + desc
            ExtentTest flowNode = extent.createTest("🌊 " + flowId + " – " + flowDesc);
            flowNode.assignCategory(flowId);
//            flowNode.info("🚀 Bắt đầu Flow: " + flowId);

            // Lưu lại để các class khác có thể lấy lại
            FlowTestListener.getExtentTestMap().put(flowId, flowNode);

            return flowNode;

        } catch (Exception e) {
            System.out.println("💥 Lỗi khi tạo node ExtentReports: " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ Log toàn bộ context ra báo cáo (dạng JSON)
     */
    public static void logContext(ExtentTest flowNode, ITestContext ctx) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String key : ctx.getAttributeNames()) {
                Object val = ctx.getAttribute(key);
                map.put(key, val);
            }

            if (!map.isEmpty()) {
                String json = new Gson().toJson(map);
                flowNode.info("🧾 **Context summary:**");
                flowNode.info(MarkupHelper.createCodeBlock(json, CodeLanguage.JSON));
            } else {
                flowNode.info("ℹ️ No context data found.");
            }
        } catch (Exception e) {
            flowNode.warning("⚠️ Lỗi khi log context: " + e.getMessage());
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