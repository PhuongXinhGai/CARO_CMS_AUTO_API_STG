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
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private ExtentReportManager() {}

    // ========== KHỞI TẠO EXTENT REPORT =====================================
    public static synchronized ExtentReports getExtent() {

        if (extent == null) {

            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dir  = System.getProperty("user.dir") + "/reports/";
            String path = dir + "Test-Report-" + time + ".html";

            try {
                Files.createDirectories(Paths.get(dir));
            } catch (Exception ignored) {}

            ExtentSparkReporter spark = new ExtentSparkReporter(path);

            // ORDER VIEW (TEST → CATEGORY → DASHBOARD)
            spark.viewConfigurer().viewOrder()
                    .as(new ViewName[]{
                            ViewName.TEST,
                            ViewName.CATEGORY,
                            ViewName.DASHBOARD,
                            ViewName.EXCEPTION
                    })
                    .apply();

            // REPORT SETTINGS
            spark.config().setDocumentTitle("Automation Test Report");
            spark.config().setReportName("API Test Report - CARO CMS");
            spark.config().setTheme(Theme.DARK);
            spark.config().setEncoding("utf-8");

            // =============================================================
            extent = new ExtentReports();
            extent.attachReporter(spark);

            extent.setSystemInfo("Project", "CARO CMS");
            extent.setSystemInfo("Module", "Booking Batch");
            extent.setSystemInfo("Env", "STG");
        }

        return extent;
    }

    // ========== GÁN TEST HIỆN TẠI =====================================
    public static void setTest(ExtentTest test) {
        CURRENT_TEST.set(test);
    }

    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    // ========== FLUSH REPORT ==========================================
    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
