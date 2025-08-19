package common.utilities;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtils {

    /**
     * Phương thức tĩnh để đọc dữ liệu từ một sheet trong file Excel.
     * @param filePath Đường dẫn tới file Excel.
     * @param sheetName Tên của sheet cần đọc dữ liệu.
     * @return Một mảng hai chiều Object[][] chứa dữ liệu từ Excel.
     */
    public static Object[][] getTestData(String filePath, String sheetName) {
        Object[][] data = null;
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheet(sheetName);
            DataFormatter formatter = new DataFormatter(); // Dùng để format tất cả cell về dạng String

            int rowCount = sheet.getPhysicalNumberOfRows();
            int colCount = sheet.getRow(0).getLastCellNum(); // Lấy số cột dựa vào hàng tiêu đề

            // Tạo mảng data với kích thước (số hàng - 1) vì bỏ qua hàng tiêu đề
            data = new Object[rowCount - 1][colCount];

            // Bắt đầu đọc từ hàng thứ 2 (index 1) để bỏ qua hàng tiêu đề
            for (int i = 1; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    // Dùng DataFormatter để đảm bảo mọi dữ liệu đọc ra đều là String, tránh lỗi
                    data[i - 1][j] = formatter.formatCellValue(sheet.getRow(i).getCell(j));
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi khi đọc file Excel: " + e.getMessage());
        }
        return data;
    }

    /**
     * Phương thức mới để đọc một dòng dữ liệu cụ thể dựa vào Test Case ID.
     * @param filePath Đường dẫn tới file Excel.
     * @param sheetName Tên của sheet.
     * @param testCaseId ID của test case cần tìm (ví dụ: "TC_LOGIN_0001").
     * @return Một Map chứa dữ liệu của dòng đó (key=tên cột, value=giá trị ô).
     */
    public static Map<String, String> getTestCaseData(String filePath, String sheetName, String testCaseId) {
        Map<String, String> testData = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheet(sheetName);
            DataFormatter formatter = new DataFormatter();

            // Lấy hàng tiêu đề (hàng đầu tiên) để biết tên các cột
            XSSFRow headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Không tìm thấy hàng tiêu đề trong sheet: " + sheetName);
            }
            int colCount = headerRow.getLastCellNum();

            // 1. Tìm vị trí (index) của cột "tc_id"
            int tcIdColumnIndex = -1;
            for (int i = 0; i < colCount; i++) {
                if ("tc_id".equalsIgnoreCase(formatter.formatCellValue(headerRow.getCell(i)))) {
                    tcIdColumnIndex = i;
                    break;
                }
            }
            if (tcIdColumnIndex == -1) {
                throw new RuntimeException("Không tìm thấy cột 'tc_id' trong file: " + filePath);
            }

            // 2. Duyệt qua từng hàng dữ liệu để tìm đúng testCaseId
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow currentRow = sheet.getRow(i);
                if (currentRow != null && testCaseId.equalsIgnoreCase(formatter.formatCellValue(currentRow.getCell(tcIdColumnIndex)))) {
                    // 3. Khi tìm thấy hàng khớp, đọc tất cả dữ liệu của hàng đó vào Map
                    for (int j = 0; j < colCount; j++) {
                        String headerName = formatter.formatCellValue(headerRow.getCell(j));
                        String cellValue = formatter.formatCellValue(currentRow.getCell(j));
                        testData.put(headerName, cellValue);
                    }
                    return testData; // Trả về Map chứa dữ liệu và kết thúc
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file Excel: " + filePath);
            e.printStackTrace();
        }

        // Nếu duyệt hết mà không tìm thấy, sẽ trả về một Map rỗng
        System.err.println("Cảnh báo: Không tìm thấy Test Case ID '" + testCaseId + "' trong file " + filePath);
        return testData;
    }

}