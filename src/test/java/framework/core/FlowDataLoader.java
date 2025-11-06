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
//                    map.put(headers.get(c), value);
                    String headerName = headers.get(c);
                    String key = headerName;
                    int idx = 1;
                    while (map.containsKey(key)) {
                        key = headerName + "_" + (++idx);
                    }
                    map.put(key, value);
                }

                if (!emptyRow) list.add(map);
            }
        }

        return list;
    }
}
