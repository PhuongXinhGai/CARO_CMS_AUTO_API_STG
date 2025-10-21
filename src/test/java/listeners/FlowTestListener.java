package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import helpers.ExtentReportManager;
import org.testng.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlowTestListener implements ITestListener, ISuiteListener {

    // ✅ Map lưu node cha cho từng flow để tránh duplicate
    private static final Map<String, ExtentTest> flowTestMap = new ConcurrentHashMap<>();

    public static Map<String, ExtentTest> getExtentTestMap() {
        return flowTestMap;
    }

    // ✅ ExtentTest hiện tại cho thread (API step trong flow)
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    public static ExtentTest getExtentTest() {
        return extentTest.get();
    }

    // ✅ Trả về ExtentReports global
    public static ExtentReports getExtentReports() {
        return ExtentReportManager.getExtent();
    }

    // === Suite lifecycle ===
    @Override
    public void onStart(ISuite suite) {
        ExtentReportManager.getExtent();
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentReportManager.flush();
    }

    // === Không tạo node test ở đây ===
    // Flow test sẽ tự tạo node qua ReportHelper.startFlow()
    @Override
    public void onTestStart(ITestResult result) {
        extentTest.remove(); // Không tạo gì thêm
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = extentTest.get();
        if (test != null)
            test.pass("✅ Flow test passed.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        if (test != null)
            test.fail(result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = extentTest.get();
        if (test != null)
            test.skip("⏭ Flow test skipped.");
    }
}
