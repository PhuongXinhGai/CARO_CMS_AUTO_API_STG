package generate;

import common.utilities.ExcelUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class GenerateTestScriptClass {

    public static void main(String[] args) throws IOException {
        String masterFilePath = System.getProperty("user.dir") + "/src/main/resources/input_excel_file/ApiMaster.xlsx";
        String templatePath = System.getProperty("user.dir") + "/src/main/resources/templates/SkeletalTestScriptTemplate.txt";

        String templateContent = new String(Files.readAllBytes(Paths.get(templatePath)));
        Object[][] apiConfigs = ExcelUtils.getTestData(masterFilePath, "api_config");

        // Tạo đối tượng Scanner để đọc input từ console
        Scanner scanner = new Scanner(System.in);

        for (Object[] apiConfig : apiConfigs) {
            // Đọc tất cả các cột từ Excel
            String module = (String) apiConfig[0];
            String className = (String) apiConfig[1];
            String dataProviderName = (String) apiConfig[2];
            String endpoint = (String) apiConfig[3];
            String httpMethod = (String) apiConfig[4];
            String requestType = (String) apiConfig[5];
            String excelFile = (String) apiConfig[6];
            String jsonTemplate = (String) apiConfig[7];
            String testMethodName = (String) apiConfig[8];

            // --- LOGIC KIỂM TRA VÀ HỎI YES/NO ---
            String outputDir = System.getProperty("user.dir") + "/src/test/java/tests/test_scripts/api/" + module + "/";
            String outputFilePath = outputDir + className + ".java";
            File outputFile = new File(outputFilePath);

            // 1. Kiểm tra xem file đã tồn tại hay chưa
            if (outputFile.exists()) {
                System.out.println("--------------------------------------------------");
                System.out.println("Cảnh báo: File '" + className + ".java' đã tồn tại.");
                System.out.print("--> Bạn có muốn ghi đè không? (gõ 'yes' or 'no'): ");

                // 2. Đọc câu trả lời của bạn từ console
                String userInput = scanner.nextLine().trim().toLowerCase();

                // 3. Nếu câu trả lời không phải là 'yes', bỏ qua file này
                if (!userInput.equals("yes")) {
                    System.out.println("    Đã bỏ qua file: " + className + ".java");
                    continue; // Chuyển sang dòng tiếp theo trong Excel
                }
            }

            // --- Phần tạo nội dung file (giữ nguyên) ---
            String requestBlockCode = "";
            if ("POST".equalsIgnoreCase(httpMethod) && "JSON".equalsIgnoreCase(requestType)) {
                requestBlockCode = buildPostJsonRequestBlock(jsonTemplate, module, endpoint);
            } else if ("GET".equalsIgnoreCase(httpMethod) && "QUERY_PARAM".equalsIgnoreCase(requestType)) {
                requestBlockCode = buildGetQueryParamRequestBlock(endpoint);
            } else {
                requestBlockCode = "// TODO: Tự định nghĩa logic gửi request cho loại API này.";
            }

            // --- Thay thế các biến trong template ---
            String finalContent = templateContent
                    .replace("@@module@@", module)
                    .replace("@@ClassName@@", className)
                    .replace("@@DataProviderName@@", dataProviderName)
                    .replace("@@ExcelFile@@", excelFile)
                    .replace("@@TestMethodName@@", testMethodName)
                    .replace("@@RequestBlock@@", requestBlockCode);

            // --- Phần ghi file (giữ nguyên) ---
            Files.createDirectories(Paths.get(outputDir));
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(finalContent);
                System.out.println("    Đã tạo/cập nhật thành công file: " + className + ".java");
            }
        }

        scanner.close();
        System.out.println("--------------------------------------------------");
        System.out.println("Quá trình tạo file test script đã hoàn tất.");
    }

    // --- Các phương thức "thợ xây" để "viết" code ---

    private static String buildPostJsonRequestBlock(String jsonTemplate, String module, String endpoint) {
        return """
                // --- ĐỌC VÀ CHUẨN BỊ REQUEST BODY ---
                String templatePath = System.getProperty("user.dir") + "/src/main/resources/input_json_file/%s/%s";
                String requestBodyTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
                
                // TODO: Thêm các lệnh .replace() cho các biến trong template
                String requestBody = requestBodyTemplate; 
                // Ví dụ: .replace("${bookingListJson}",resolvedBookingListJson);
                
                // --- Xử lý dữ liệu động (nếu cần) ---
                // String resolvedValue = DynamicDataHelper.resolveDynamicValue(someValue);
                
                // --- Gửi Request ---
                RequestSpecification requestSpec = new RequestSpecBuilder()
                        .addHeader("Authorization", authToken)
                        .setContentType(ContentType.JSON)
                        .addFilter(new RequestLoggingFilter(requestCapture))
                        .setBody(requestBody)
                        .build();

                Response response = given()
                        .spec(requestSpec)
                        .when()
                        .post("%s")
                        .then()
                        .extract().response();
                """.formatted(module, jsonTemplate, endpoint);
    }

    private static String buildGetQueryParamRequestBlock(String endpoint) {
        return """
                // --- Xử lý dữ liệu động (nếu cần) ---
                // String resolvedValue = DynamicDataHelper.resolveDynamicValue(someValue);
                
                // --- Gửi Request ---
                RequestSpecification requestSpec = new RequestSpecBuilder()
                        .addHeader("Authorization", authToken)
                        .addFilter(new RequestLoggingFilter(requestCapture))
                        .build();

                Response response = given()
                        .spec(requestSpec)
                        // TODO: Thêm các lệnh .queryParam("key", value) cần thiết ở đây
                        .when()
                        .get("%s")
                        .then()
                        .extract().response();
                """.formatted(endpoint);
    }
}