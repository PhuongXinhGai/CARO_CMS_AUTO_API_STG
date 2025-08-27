package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import helpers.ExtentReportManager;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class IntegrationListener implements ITestListener {

    // Dùng chung một trình quản lý báo cáo
    private static ExtentReports extent = ExtentReportManager.createInstance();
    // Dùng ThreadLocal để đảm bảo an toàn khi chạy song song
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    public static ExtentTest getExtentTest() {
        return extentTest.get();
    }

    @Override
    public void onTestStart(ITestResult result) {
        // Lấy mô tả từ file Excel (tham số thứ 2) để làm tên test case
        String flowDescription = (String) result.getParameters()[1];
        ExtentTest test = extent.createTest(flowDescription);
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // Lấy ExtentTest đã được tạo và đánh dấu PASS
        getExtentTest().pass("Flow execution passed.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        // Lấy ExtentTest đã được tạo và ghi lại lỗi cuối cùng
        getExtentTest().fail(result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        // Kết thúc và ghi báo cáo ra file
        if (extent != null) {
            extent.flush();
        }
    }
}