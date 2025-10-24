package tests.test_scripts.api.integration;

import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import framework.core.*;
import helpers.ReportHelper;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentTest;
import java.util.List;
import java.util.Map;

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
    public void runIntegrationFlow(Map<String, String> flow, ITestContext ctx) throws Exception {

        String flowId   = flow.get("flow_id");
        String flowDesc = flow.get("flow_description");

        ExtentTest flowLogger = ReportHelper.startFlow(flowId, flowDesc);

        if (flowLogger != null)
            flowLogger.info("🚀 Start Flow: " + flowId + " - " + flowDesc);

        List<String> columns = ApiRegistry.orderedColumns();

        for (String col : columns) {
            String caseId = flow.get(col);
            if (caseId == null || caseId.isEmpty()) {
                if (flowLogger != null) flowLogger.info("⏭ Skip step (no case id): " + col);
                continue;
            }

            String className = ApiRegistry.get(col);
            if (className == null) {
                if (flowLogger != null) flowLogger.warning("⚠ No mapping class for column: " + col);
                continue;
            }

            if (flowLogger != null)
                flowLogger.info("▶️ Step: " + col + " → " + caseId + " → " + className);

            try {
                Class<?> clazz = Class.forName(className);
                FlowRunnable apiTest = (FlowRunnable) clazz.getDeclaredConstructor().newInstance();

                ExtentTest stepLogger = (flowLogger != null)
                        ? flowLogger.createNode(col + " - " + caseId)
                        : null;

                // Chạy API
                apiTest.runCase(caseId, ctx, stepLogger);
                // === Ghi log request / response ===
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

                // === Ghi log request / response nếu có ===
//                Object req = ctx.getAttribute("LAST_REQUEST_LOG");
//                Object resp = ctx.getAttribute("LAST_RESPONSE_LOG");
//                if (stepLogger != null) {
//                    if (req != null) stepLogger.info("📤 REQUEST:\n" + req);
//                    if (resp != null) stepLogger.info("📥 RESPONSE:\n" + resp);
//                }
                if (stepLogger != null) {
                    if (req != null)
                        stepLogger.info(MarkupHelper.createCodeBlock(req.toString(), CodeLanguage.JSON));
                    if (resp != null)
                        stepLogger.info(MarkupHelper.createCodeBlock(resp.toString(), CodeLanguage.JSON));
                }

                if (stepLogger != null)
                    stepLogger.pass("✅ Passed: " + col + " (" + caseId + ")");
            }

            catch (AssertionError ae) {
                if (flowLogger != null)
                    flowLogger.fail("❌ Assertion failed at step: " + col + " → " + ae.getMessage());
                throw ae; // dừng flow
            }
            catch (Exception ex) {
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
