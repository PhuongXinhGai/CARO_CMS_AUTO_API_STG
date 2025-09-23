package helpers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExtentReportManager {
    private static ExtentReports extent;
    // Giữ ExtentTest theo từng thread (mỗi test case một bản riêng)
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private ExtentReportManager() {}

    // Tạo 1 lần và tái dùng (thread-safe)
    public static synchronized ExtentReports getExtent() {
        if (extent == null) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dir  = System.getProperty("user.dir") + "/reports/";
            String path = dir + "Test-Report-" + time + ".html";
            try { Files.createDirectories(Paths.get(dir)); } catch (Exception ignored) {}

            ExtentSparkReporter spark = new ExtentSparkReporter(path);
            // Tuỳ chọn giao diện
            spark.viewConfigurer().viewOrder()
                    .as(new ViewName[]{ViewName.DASHBOARD, ViewName.TEST, ViewName.CATEGORY, ViewName.EXCEPTION})
                    .apply();
            spark.config().setDocumentTitle("Automation Test Report");
            spark.config().setReportName("API Test Report");
            spark.config().setTheme(Theme.DARK);
            spark.config().setEncoding("utf-8");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            // Thêm system info nếu muốn
            extent.setSystemInfo("Project", "CARO CMS");
            extent.setSystemInfo("Module", "Booking Batch");
            extent.setSystemInfo("Env", "STG");
        }
        return extent;
    }

    // Gọi trong Listener khi tạo test mới
    public static void setTest(ExtentTest test) {
        CURRENT_TEST.set(test);
    }

    // Lấy test hiện tại (dùng trong class test để log)
    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
