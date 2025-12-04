package tests.test_scripts.api.integration;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import framework.core.ApiRegistry;
import framework.core.FlowDataLoader;
import framework.core.FlowRunnable;
import helpers.ReportHelper;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

public class IntegrationVoucherTest {
    // ==========================================================
    //  DATA PROVIDER
    // ==========================================================
    @DataProvider(name = "flowData")
    public Object[][] flowData() throws Exception {
        String excelPath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/flow_definition.xlsx";
        String sheetName = "VC_CA_NHAN";

        List<Map<String, String>> list = FlowDataLoader.readFlows(excelPath, sheetName);

        Object[][] data = new Object[list.size()][1];
        for (int i = 0; i < list.size(); i++) {
            data[i][0] = list.get(i);
        }
        return data;
    }

    // ==========================================================
    //  MAIN FLOW TEST
    // ==========================================================
    @Test(dataProvider = "flowData", description = "Flow Integration – Booking API chain")
    public void runIntegrationFlow(Map<String, String> flow, ITestContext ctx) throws Exception {

        String excelPath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/flow_definition.xlsx";

        String flowId   = flow.get("flow_id");
        String flowDesc = flow.get("flow_description");
        String orderSheet = flow.get("flow_order_sheet"); // sheet mới

        ExtentTest flowLogger = ReportHelper.startFlow(flowId, flowDesc);

        if (flowLogger != null) {
            flowLogger.info("🚀 Start Flow: " + flowId + " - " + flowDesc);
        }

        // ==========================================================
        //  ✔ STEP 1: LẤY THỨ TỰ CHẠY TỪ SHEET ORDER
        // ==========================================================
        List<String> columns;

        if (orderSheet != null && !orderSheet.isBlank()) {
            flowLogger.info("📑 Flow dùng order sheet: **" + orderSheet + "**");
            columns = FlowDataLoader.loadOrderSheet(excelPath, orderSheet);
        } else {
            flowLogger.info("📑 Flow không có order sheet → dùng ApiRegistry.orderedColumns()");
            columns = ApiRegistry.orderedColumns();
        }

        // ==========================================================
        //  ✔ STEP 2: BUCKET LOGIC GIỮ NGUYÊN
        // ==========================================================
        Map<String, List<String>> buckets = new LinkedHashMap<>();
        for (String key : flow.keySet()) {
            if (key == null || key.isEmpty()) continue;
            String base = key.replaceFirst("(_\\d+)$", "");
            buckets.computeIfAbsent(base, k -> new ArrayList<>()).add(key);
        }

        Map<String, Integer> ptr = new HashMap<>();

        // ==========================================================
        //  ✔ STEP 3: CHẠY CÁC STEP THEO THỨ TỰ columns
        // ==========================================================
        for (String col : columns) {

            List<String> list = buckets.get(col);
            int i = ptr.getOrDefault(col, 0);

            if (list == null || i >= list.size()) {
                flowLogger.info("⏭ Skip step (no case id): " + col);
                continue;
            }

            String key = list.get(i);
            ptr.put(col, i + 1);

            String caseId = flow.get(key);
            if (caseId == null || caseId.isEmpty()) {
                flowLogger.info("⏭ Skip step (empty case id): " + key);
                continue;
            }

            String className = ApiRegistry.get(col);
            if (className == null) {
                flowLogger.warning("⚠ No mapping class for column: " + col);
                continue;
            }

            flowLogger.info("▶️ Step: " + key + " → " + caseId + " → " + className);

// ⚡ CHỈNH SỬA: Di chuyển log REQUEST/RESPONSE vào finally để đảm bảo luôn hiển thị
            ExtentTest stepLogger = flowLogger.createNode(col + " - " + caseId);

            Object req = null;   // nơi sẽ chứa request lấy từ context
            Object resp = null;  // nơi sẽ chứa response lấy từ context

            try {
                // ⚡ CHẠY API STEP
                Class<?> clazz = Class.forName(className);
                FlowRunnable apiTest = (FlowRunnable) clazz.getDeclaredConstructor().newInstance();
                apiTest.runCase(caseId, ctx, stepLogger);

                // ⚡ nếu chạy không lỗi → PASS
                if (stepLogger != null)
                    stepLogger.pass("✅ Passed: " + col + " (" + caseId + ")");

            } catch (AssertionError ae) {

                // ⚡ step FAIL
                if (stepLogger != null)
                    stepLogger.fail("❌ Assertion failed: " + col + " (" + caseId + ") " + ae.getMessage());
                    flowLogger.assignCategory("FAIL");
                throw ae;  // giữ nguyên cơ chế dừng flow

            } catch (Exception ex) {

                // ⚡ lỗi khác
                if (stepLogger != null)
                    stepLogger.fail("💥 Exception: " + col + " (" + caseId + ") " + ex.getMessage());
                    flowLogger.assignCategory("FAIL");
                throw ex;

            } finally {

                // ⚡ LUÔN luôn log REQUEST / RESPONSE dù PASS hay FAIL
                req  = ctx.getAttribute("LAST_REQUEST_LOG");
                resp = ctx.getAttribute("LAST_RESPONSE_LOG");

                if (stepLogger != null) {

                    if (req != null) {
                        stepLogger.info("📤 **REQUEST:**");
                        stepLogger.info(
                                MarkupHelper.createCodeBlock(req.toString(), CodeLanguage.JSON)
                        );
                    }

                    if (resp != null) {
                        stepLogger.info("📥 **RESPONSE:**");
                        stepLogger.info(
                                MarkupHelper.createCodeBlock(resp.toString(), CodeLanguage.JSON)
                        );
                    }
                }
            }
        }
        flowLogger.assignCategory("PASS");
        flowLogger.pass("🎯 Flow " + flowId + " completed successfully!");
        ReportHelper.logContext(flowLogger, ctx);
    }
}
