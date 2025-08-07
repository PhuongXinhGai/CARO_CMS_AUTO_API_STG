package common.utilities;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

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
}