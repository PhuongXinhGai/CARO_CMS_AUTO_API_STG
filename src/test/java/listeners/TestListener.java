package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import helpers.ExtentReportManager;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final ExtentReports extent = ExtentReportManager.createInstance();
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    public static ExtentTest getExtentTest() {
        return extentTest.get();
    }

    @Override
    public void onTestStart(ITestResult result) {
        // Lấy tc_id + tc_description an toàn
        Object[] params = result.getParameters();
        String tcId = (params != null && params.length > 0 && params[0] != null) ? params[0].toString() : "";
        String tcDescription = (params != null && params.length > 1 && params[1] != null) ? params[1].toString() : result.getMethod().getMethodName();
        String fullTitle = (tcId.isEmpty() ? "" : tcId + " - ") + tcDescription;

        ExtentTest test = extent.createTest(fullTitle);
        extentTest.set(test);
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

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
