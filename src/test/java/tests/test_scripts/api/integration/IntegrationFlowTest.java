package tests.test_scripts.api.integration;

import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import framework.core.*;
import helpers.ReportHelper;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentTest;

import java.util.*;
import java.util.stream.Collectors;

public class IntegrationFlowTest {

    // ==========================================================
    //  ✅ GỘP DATAPROVIDER NGAY TRONG CLASS
    // ==========================================================
    @DataProvider(name = "flowData")
    public Object[][] flowData() throws Exception {
        String excelPath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/flow_definition.xlsx";
        String sheetName = "Test"; // tạm thời fix cứng

        List<Map<String, String>> list = FlowDataLoader.readFlows(excelPath, sheetName);

        Object[][] data = new Object[list.size()][1];
        for (int i = 0; i < list.size(); i++) {
            data[i][0] = list.get(i);
        }
        return data;
    }

    // ==========================================================
    //  ✅ TEST CHÍNH CHẠY FLOW
    // ==========================================================
    @Test(dataProvider = "flowData", description = "Flow Integration – Booking API chain")
//    public void runIntegrationFlow(Map<String, String> flow, ITestContext ctx) throws Exception {
//
//        String flowId   = flow.get("flow_id");
//        String flowDesc = flow.get("flow_description");
//
//        ExtentTest flowLogger = ReportHelper.startFlow(flowId, flowDesc);
//
//        if (flowLogger != null)
//            flowLogger.info("🚀 Start Flow: " + flowId + " - " + flowDesc);
//
//        List<String> columns = ApiRegistry.orderedColumns();
//
//        for (String col : columns) {
//            String caseId = flow.get(col);
//            if (caseId == null || caseId.isEmpty()) {
//                if (flowLogger != null) flowLogger.info("⏭ Skip step (no case id): " + col);
//                continue;
//            }
//
//            String className = ApiRegistry.get(col);
//            if (className == null) {
//                if (flowLogger != null) flowLogger.warning("⚠ No mapping class for column: " + col);
//                continue;
//            }
//
//            if (flowLogger != null)
//                flowLogger.info("▶️ Step: " + col + " → " + caseId + " → " + className);
//
//            try {
//                Class<?> clazz = Class.forName(className);
//                FlowRunnable apiTest = (FlowRunnable) clazz.getDeclaredConstructor().newInstance();
//
//                ExtentTest stepLogger = (flowLogger != null)
//                        ? flowLogger.createNode(col + " - " + caseId)
//                        : null;
//
//                // Chạy API
//                apiTest.runCase(caseId, ctx, stepLogger);
//                // === Ghi log request / response ===
//                Object req = ctx.getAttribute("LAST_REQUEST_LOG");
//                Object resp = ctx.getAttribute("LAST_RESPONSE_LOG");
//
//                if (stepLogger != null) {
//                    if (req != null) {
//                        stepLogger.info("📤 **REQUEST:**");
//                        stepLogger.info(MarkupHelper.createCodeBlock(req.toString(), CodeLanguage.JSON));
//                    }
//                    if (resp != null) {
//                        stepLogger.info("📥 **RESPONSE:**");
//                        stepLogger.info(MarkupHelper.createCodeBlock(resp.toString(), CodeLanguage.JSON));
//                    }
//                }
//
//                if (stepLogger != null) {
//                    if (req != null)
//                        stepLogger.info(MarkupHelper.createCodeBlock(req.toString(), CodeLanguage.JSON));
//                    if (resp != null)
//                        stepLogger.info(MarkupHelper.createCodeBlock(resp.toString(), CodeLanguage.JSON));
//                }
//
//                if (stepLogger != null)
//                    stepLogger.pass("✅ Passed: " + col + " (" + caseId + ")");
//            }
//
//            catch (AssertionError ae) {
//                if (flowLogger != null)
//                    flowLogger.fail("❌ Assertion failed at step: " + col + " → " + ae.getMessage());
//                throw ae; // dừng flow
//            }
//            catch (Exception ex) {
//                if (flowLogger != null)
//                    flowLogger.fail("💥 Exception at step: " + col + " → " + ex.getMessage());
//                throw ex; // dừng flow
//            }
//        }
//
//        if (flowLogger != null)
//            flowLogger.pass("🎯 Flow " + flowId + " completed successfully!");
//
//        // Sau khi chạy hết các API trong flow
//        ReportHelper.logContext(flowLogger, ctx);
//
//    }
    public void runIntegrationFlow(Map<String, String> flow, ITestContext ctx) throws Exception {

        String flowId   = flow.get("flow_id");
        String flowDesc = flow.get("flow_description");

        ExtentTest flowLogger = ReportHelper.startFlow(flowId, flowDesc);

        if (flowLogger != null)
            flowLogger.info("🚀 Start Flow: " + flowId + " - " + flowDesc);

        // 1) Thứ tự chạy chuẩn theo registry (business order)
        List<String> columns = ApiRegistry.orderedColumns();

        // 2) Gom các key theo "base name" (bỏ hậu tố _1, _2...), giữ NGUYÊN thứ tự cột trong Excel
        //    Ví dụ: check_in_bag_player1_id, check_in_bag_player1_id_2  -> cùng bucket "check_in_bag_player1_id"
        Map<String, List<String>> buckets = new LinkedHashMap<>();
        for (String key : flow.keySet()) {
            if (key == null || key.isEmpty()) continue;
            String base = key.replaceFirst("(_\\d+)$", ""); // bỏ hậu tố _1, _2...
            buckets.computeIfAbsent(base, k -> new ArrayList<>()).add(key);
        }

        // 3) Con trỏ cho từng base column: đã "lấy" tới phần tử thứ mấy trong bucket
        Map<String, Integer> ptr = new HashMap<>();

        // 4) Chạy theo orderedColumns(); mỗi lần gặp 1 base column -> chỉ lấy 1 key trong bucket tương ứng
        for (String col : columns) {
            List<String> list = buckets.get(col);
            int i = ptr.getOrDefault(col, 0);

            if (list == null || i >= list.size()) {
                if (flowLogger != null) flowLogger.info("⏭ Skip step (no case id): " + col);
                continue;
            }

            // Lấy đúng key theo THỨ TỰ CỘT TRONG EXCEL cho lần xuất hiện này của base column
            String key = list.get(i);
            ptr.put(col, i + 1); // advance pointer cho lần gặp tiếp theo

            String caseId = flow.get(key);
            if (caseId == null || caseId.isEmpty()) {
                if (flowLogger != null) flowLogger.info("⏭ Skip step (empty case id): " + key);
                continue;
            }

            String className = ApiRegistry.get(col);
            if (className == null) {
                if (flowLogger != null) flowLogger.warning("⚠ No mapping class for column: " + col);
                continue;
            }

            if (flowLogger != null)
                flowLogger.info("▶️ Step: " + key + " → " + caseId + " → " + className);

            try {
                Class<?> clazz = Class.forName(className);
                FlowRunnable apiTest = (FlowRunnable) clazz.getDeclaredConstructor().newInstance();

                ExtentTest stepLogger = (flowLogger != null)
                        ? flowLogger.createNode(col + " - " + caseId)
                        : null;

                // Chạy API
                apiTest.runCase(caseId, ctx, stepLogger);

                // === Ghi log request / response (giữ nguyên logic của bạn) ===
                Object req = ctx.getAttribute("LAST_REQUEST_LOG");
                Object resp = ctx.getAttribute("LAST_RESPONSE_LOG");

                if (stepLogger != null) {
                    if (req != null) {
                        stepLogger.info("📤 **REQUEST:**");
                        stepLogger.info(MarkupHelper.createCodeBlock(req.toString(), CodeLanguage.JSON));
                    }
                    if (resp != null) {
                        stepLogger.info("📥 **RESPONSE:**");
                        stepLogger.info(MarkupHelper.createCodeBlock(resp.toString(), CodeLanguage.JSON));
                    }
                }

                if (stepLogger != null) {
                    if (req != null)
                        stepLogger.info(MarkupHelper.createCodeBlock(req.toString(), CodeLanguage.JSON));
                    if (resp != null)
                        stepLogger.info(MarkupHelper.createCodeBlock(resp.toString(), CodeLanguage.JSON));
                }

                if (stepLogger != null)
                    stepLogger.pass("✅ Passed: " + col + " (" + caseId + ")");

            } catch (AssertionError ae) {
                if (flowLogger != null)
                    flowLogger.fail("❌ Assertion failed at step: " + col + " → " + ae.getMessage());
                throw ae; // dừng flow
            } catch (Exception ex) {
                if (flowLogger != null)
                    flowLogger.fail("💥 Exception at step: " + col + " → " + ex.getMessage());
                throw ex; // dừng flow
            }
        }

        if (flowLogger != null)
            flowLogger.pass("🎯 Flow " + flowId + " completed successfully!");

        // Sau khi chạy hết các API trong flow
        ReportHelper.logContext(flowLogger, ctx);
    }
}
