package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import helpers.ExtentReportManager;
import org.testng.*;

import org.testng.ITestResult;

import java.util.Map;


public class TestListener implements ITestListener, ISuiteListener {

    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // ========================================================
//// 🔹 Map lưu node cha của từng flow để tránh tạo duplicate
//// ========================================================
//    private static final Map<String, ExtentTest> flowTestMap = new java.util.concurrent.ConcurrentHashMap<>();
//
//    public static Map<String, ExtentTest> getExtentTestMap() {
//        return flowTestMap;
//    }

    public static ExtentTest getExtentTest() { return extentTest.get(); }

    // Init Extent 1 lần khi suite bắt đầu
    @Override public void onStart(ISuite suite) {
        ExtentReportManager.getExtent();
    }

    // Flush report khi suite kết thúc
    @Override public void onFinish(ISuite suite) {
        ExtentReportManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = buildName(result);
//        ExtentTest test = ExtentReportManager.getExtent().createTest(name);
        ExtentTest test = ExtentReportManager.getExtent().createTest(
                name,
                result.getMethod().getDescription() != null ? result.getMethod().getDescription() : ""
        );

        extentTest.set(test);
    }


    private String buildName(ITestResult r){
        // 1) Nếu class có implements ITest (nếu còn class nào), vẫn ưu tiên
        if (r.getTestName()!=null && !r.getTestName().trim().isEmpty()) return r.getTestName();

        // 2) DataProvider = Map (API booking…)
        Object[] p = r.getParameters();
        if (p != null && p.length > 0 && p[0] instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String,String> m = (java.util.Map<String,String>) p[0];
            String id  = m.getOrDefault("tc_id", "");
            String des = m.getOrDefault("tc_description", "");
            return (id + " - " + des).trim();
        }

        // 3) DataProvider = String... (login…)
        if (p != null && p.length > 1 && p[0] instanceof String && p[1] instanceof String) {
            return ((String)p[0]) + " - " + ((String)p[1]);
        }

        // 4) Fallback
        return r.getMethod().getMethodName();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().pass("Test Passed");
        extentTest.get().assignCategory("PASS");  // <-- Filter theo PASS

        String requestLog = (String) result.getAttribute("requestLog");
        if (requestLog != null && !requestLog.isEmpty()) {
            extentTest.get().info("REQUEST DETAILS:");
            extentTest.get().info(MarkupHelper.createCodeBlock(requestLog));
        }

        String responseLog = (String) result.getAttribute("responseLog");
        if (responseLog != null && !responseLog.isEmpty()) {
            extentTest.get().info("RESPONSE BODY:");
            extentTest.get().info(MarkupHelper.createCodeBlock(responseLog, CodeLanguage.JSON));
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        extentTest.get().fail(result.getThrowable());
        extentTest.get().assignCategory("FAIL");  // <-- Filter theo FAIL

        String requestLog = (String) result.getAttribute("requestLog");
        if (requestLog != null && !requestLog.isEmpty()) {
            extentTest.get().fail("REQUEST DETAILS:");
            extentTest.get().fail(MarkupHelper.createCodeBlock(requestLog));
        }

        String responseLog = (String) result.getAttribute("responseLog");
        if (responseLog != null && !responseLog.isEmpty()) {
            extentTest.get().info("RESPONSE BODY:");
            extentTest.get().info(MarkupHelper.createCodeBlock(responseLog, CodeLanguage.JSON));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().skip("Test Skipped");
        extentTest.get().assignCategory("SKIPPED"); // optional, để lọc skipped
    }
    // Getter cho ExtentReports – dùng cho ReportHelper.startFlow()
    public static ExtentReports getExtentReports() {
        return ExtentReportManager.getExtent();
    }



//    @Override
//    public void onFinish(ITestContext context) {
//        if (extentTest != null) {
//            extentTest.flush();
//        }
//    }
}
