package framework.core;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class FlowDataLoader {


//    class đọc file Excel flow → list các dòng
    public static List<Map<String, String>> readFlows(String path, String sheetName) throws Exception {
        List<Map<String, String>> list = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(path);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("❌ Sheet not found: " + sheetName);

            int firstRow = sheet.getFirstRowNum();
            int lastRow  = sheet.getLastRowNum();

            // === Header row ===
            Row header = sheet.getRow(firstRow);
            if (header == null) throw new RuntimeException("❌ Header row not found in sheet: " + sheetName);

            int colCount = header.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < colCount; c++) {
                Cell cell = header.getCell(c);
                headers.add(cell != null ? cell.getStringCellValue().trim() : "");
            }

            // === Data rows ===
            for (int r = firstRow + 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> map = new LinkedHashMap<>();
                boolean emptyRow = true;

                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c);
                    String value = "";
                    if (cell != null) {
                        cell.setCellType(CellType.STRING);
                        value = cell.getStringCellValue().trim();
                        if (!value.isEmpty()) emptyRow = false;
                    }
                    String headerName = headers.get(c);
                    String key = headerName;
                    int idx = 1;
                    while (map.containsKey(key)) {
                        key = headerName + "_" + (++idx);
                    }
                    map.put(key, value);
                }

                if (emptyRow) continue;

                // ==================================================
                // 🔥 FILTER FLOW THEO CỘT is_run
                // ==================================================
                String isRun = map.get("is_run");

                if (isRun == null || isRun.isBlank()) {
                    continue; // không có is_run → skip
                }

                if (!"TRUE".equalsIgnoreCase(isRun)) {
                    continue; // FALSE → skip
                }

                list.add(map);
            }


        }
        return list;
    }
    /**
     * Loader đọc sheet order (flow_order_sheet) từ file Excel
     * Mỗi sheet phải có 1 cột: "step_order"
     */
    public static List<String> loadOrderSheet(String excelPath, String sheetName) throws Exception {
        List<String> orderList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null)
                throw new RuntimeException("❌ Không tìm thấy sheet order: " + sheetName);

            int firstRow = sheet.getFirstRowNum();
            int lastRow  = sheet.getLastRowNum();

            // Tìm vị trí cột step_order trong header
            Row header = sheet.getRow(firstRow);
            if (header == null)
                throw new RuntimeException("❌ Sheet order không có header: " + sheetName);

            int colIndex = -1;
            for (int c = 0; c < header.getLastCellNum(); c++) {
                Cell cell = header.getCell(c);
                if (cell != null && "step_order".equalsIgnoreCase(cell.getStringCellValue().trim())) {
                    colIndex = c;
                    break;
                }
            }

            if (colIndex == -1)
                throw new RuntimeException("❌ Không tìm thấy cột 'step_order' trong sheet: " + sheetName);

            // Đọc từng dòng trong cột step_order
            for (int r = firstRow + 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Cell cell = row.getCell(colIndex);
                if (cell == null) continue;

                cell.setCellType(CellType.STRING);
                String val = cell.getStringCellValue().trim();
                if (!val.isEmpty()) {
                    orderList.add(val);
                }
            }
        }

        return orderList;
    }
}


