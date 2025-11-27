package generate;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;

public class ScriptGenerator {

    private static final String API_MASTER_FILE =
            System.getProperty("user.dir") + "/src/main/resources/api_master/ApiMaster.xlsx";

    private static final String TEMPLATE_FILE =
            System.getProperty("user.dir") + "/src/main/resources/templates/SkeletalTestScriptTemplate.txt";

    private static final String OUTPUT_SRC =
            System.getProperty("user.dir") + "/src/test/java/tests/test_scripts/api/";

    public static void main(String[] args) throws Exception {
        new ScriptGenerator().run();
    }

    public void run() throws Exception {
        System.out.println("=== Script Generator Started ===");

        // Load template
        String template = Files.readString(Paths.get(TEMPLATE_FILE));

        // Load Excel
        Workbook wb = new XSSFWorkbook(new FileInputStream(API_MASTER_FILE));
        Sheet sheet = wb.getSheet("api_config");
        if (sheet == null) throw new RuntimeException("❌ Sheet 'api_config' không tồn tại!");

        // Read each row
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i);
            if (r == null) continue;

            String module         = getCell(r, 0);
            String className      = getCell(r, 1);
            String dataProvider   = getCell(r, 2);
            String baseUrlKey     = getCell(r, 3);
            String endpoint       = getCell(r, 4);
            String httpMethod     = getCell(r, 5);
            String requestType    = getCell(r, 6);
            String excelFile      = getCell(r, 7);
            String sheetName      = getCell(r, 8);
            String jsonTemplate   = getCell(r, 9);
            String testMethod     = getCell(r, 10);
            String registryKey    = getCell(r, 11); // không dùng trong generator

            if (module == null || className == null) continue;

            generateOne(template, module, className, dataProvider,
                    baseUrlKey, endpoint, httpMethod, requestType,
                    excelFile, sheetName, jsonTemplate, testMethod);
        }

        System.out.println("=== Script Generator Done! ===");
    }

    private void generateOne(
            String tpl,
            String module,
            String className,
            String dataProvider,
            String baseUrlKey,
            String endpoint,
            String httpMethod,
            String requestType,
            String excelFile,
            String sheetName,
            String jsonTemplate,
            String testMethod
    ) throws Exception {

        // Validate module folder exists
        String modulePath = OUTPUT_SRC + module + "/";
        if (!Files.exists(Paths.get(modulePath))) {
            throw new RuntimeException("❌ Folder module không tồn tại: " + modulePath);
        }

        // Build blocks
        String requestBuildBlock = buildRequestBuildBlock(requestType);
        String requestCallBlock = buildRequestCallBlock(requestType);
        String requestLogBlock = buildRequestLogBlock(httpMethod, requestType);

        // Convert module path ("booking/create_booking") thành package ("booking.create_booking")
        String moduleForPackage = module.replace("/", ".");

        // Auto tạo constant cho endpoint
        String constantName = ConstantsGenerator.getOrCreateConstant(endpoint);

        // Replace template
        String content = tpl
                .replace("@@module@@", moduleForPackage)
                .replace("@@ClassName@@", className)
                .replace("@@DataProviderName@@", dataProvider)
                .replace("@@TestMethodName@@", testMethod)
                .replace("@@ExcelFile@@", excelFile)
                .replace("@@SheetName@@", sheetName)
                .replace("@@JsonTemplate@@", jsonTemplate)
                .replace("@@BaseUrl@@", baseUrlKey)
                .replace("@@EndPoint@@", constantName)
                .replace("@@HttpMethod@@", httpMethod.toLowerCase())
                .replace("@@RequestBuildBlock@@", requestBuildBlock)
                .replace("@@Request@@", requestCallBlock)
                .replace("@@RequestLogBlock@@", requestLogBlock);

        // Write file
        String filePath = modulePath + className + ".java";
        Files.writeString(Paths.get(filePath), content);

        System.out.println("✔ Generated: " + filePath);
    }

    private String buildRequestBuildBlock(String requestType) {
        if ("JSON".equalsIgnoreCase(requestType)) {
            return
                    "String reqFileName = row.getOrDefault(\"input_placeholders\", \"\");\n" +
                    "        " + "String reqTpl = Files.readString(Paths.get(JSON_DIR + reqFileName));\n" +
                    "        " + "String requestBody = StringUtils.replacePlaceholdersAdvanced(reqTpl, row, ctx);\n" +
                    "        " + "System.out.println(\"🧩 Request JSON sau replace:\\n\" + requestBody);";
        }
        if ("QUERY_PARAM".equalsIgnoreCase(requestType)) {
            return
                    "Map<String, Object> q = QueryParamHelper.build(row, ctx);\n" +
                    "        " + "System.out.println(\"🧩 Query Params:\\n\" + q);";
        }
        return "// (không có request build)";
    }

    private String buildRequestCallBlock(String requestType) {
        if ("JSON".equalsIgnoreCase(requestType)) {
            return "body(requestBody)";
        }
        if ("QUERY_PARAM".equalsIgnoreCase(requestType)) {
            return "queryParams(q)";
        }
        return "";
    }

    private String buildRequestLogBlock(String httpMethod, String requestType) {

        if ("GET".equalsIgnoreCase(httpMethod)) {
            return
                    "String requestLog = RequestLogHelper.buildRequestLog(\n" +
                            "        \"GET\",\n" +
                            "        url,\n" +
                            "        q,\n" +
                            "        null\n" +
                            ");";
        }

        // POST / PUT / DELETE
        return
                "String requestLog = RequestLogHelper.buildRequestLog(\n" +
                        "        \"" + httpMethod + "\",\n" +
                        "        url,\n" +
                        "        null,\n" +
                        "        requestBody\n" +
                        ");";
    }

    private String getCell(Row r, int index) {
        Cell c = r.getCell(index);
        return c == null ? null : c.toString().trim();
    }
}
