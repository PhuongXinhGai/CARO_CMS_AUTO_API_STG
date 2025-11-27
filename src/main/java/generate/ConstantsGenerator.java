package generate;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ConstantsGenerator {

    private static final String CONSTANTS_FILE =
            System.getProperty("user.dir") + "/src/main/java/common/utilities/Constants.java";

    // Regex lấy dòng kiểu: public static final String ABC_ENDPOINT = "/golf-cms/api/...";
    private static final Pattern CONST_PATTERN = Pattern.compile(
            "public static final String ([A-Z0-9_]+)\\s*=\\s*\"([^\"]+)\"");

    // ============================================================
    // == 1. TẢI TOÀN BỘ CONSTANT HIỆN CÓ LÊN MAP
    // ============================================================
    public static Map<String, String> loadExistingConstants() throws Exception {
        Map<String, String> valueToName = new HashMap<>();

        List<String> lines = Files.readAllLines(Paths.get(CONSTANTS_FILE));

        for (String line : lines) {
            Matcher m = CONST_PATTERN.matcher(line.trim());
            if (m.find()) {
                String name = m.group(1).trim();
                String value = m.group(2).trim();
                valueToName.put(value, name);
            }
        }

        return valueToName;
    }

    // ============================================================
    // == 2. TẠO HOẶC LẤY CONSTANT NAME CHO ENDPOINT
    // ============================================================
    public static String getOrCreateConstant(String endpoint) throws Exception {

        Map<String, String> existing = loadExistingConstants();

        // ---- A. Nếu endpoint đã tồn tại → dùng lại
        if (existing.containsKey(endpoint)) {
            return existing.get(endpoint);
        }

        // ---- B. Sinh constant mới
        String constantName = buildConstantName(endpoint);

        // ---- C. Append vào file Constants.java
        appendConstantToFile(constantName, endpoint);

        return constantName;
    }

    // ============================================================
    // == 3. RULE TẠO TÊN CONSTANT
    // ============================================================
    private static String buildConstantName(String endpoint) {

        String cleaned = endpoint;

        // Bỏ prefix cms & maintenance
        if (cleaned.startsWith("/golf-cms/api/")) {
            cleaned = cleaned.replace("/golf-cms/api/", "");
        } else if (cleaned.startsWith("/golf-maintenance/api/")) {
            cleaned = cleaned.replace("/golf-maintenance/api/", "");
        }

        // Xác định group (chức năng)
        String[] parts = cleaned.split("/");
        String group = parts[0]; // booking, payment, course-operating...

        // Phần còn lại để tạo constant name
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i].toUpperCase().replace("-", "_"));
            if (i < parts.length - 1) sb.append("_");
        }

        return sb.toString() + "_ENDPOINT";
    }

    // ============================================================
    // == 4. APPEND CONSTANT THEO NHÓM + SORT ALPHABET
    // ============================================================
    private static void appendConstantToFile(String constName, String endpoint) throws Exception {

        List<String> lines = Files.readAllLines(Paths.get(CONSTANTS_FILE));

        // Xác định group từ endpoint
        String group = extractGroup(endpoint);

        // Tìm vị trí nhóm trong file
        int insertPos = findGroupInsertPosition(lines, group);

        // Nếu nhóm chưa có → chèn header nhóm
        if (insertPos == -1) {
            lines.add("");
            lines.add("    // " + group);
            insertPos = lines.size();
        }

        // Thêm constant mới
        lines.add(insertPos, "    public static final String " + constName + " = \"" + endpoint + "\";");

        // Sort trong nhóm
        sortGroup(lines, group);

        // Ghi lại file
        Files.write(Paths.get(CONSTANTS_FILE), lines);
    }

    // ============================================================
    // == 5. PHÂN NHÓM THEO PREFIX SAU /api/
    // ============================================================
    private static String extractGroup(String endpoint) {
        String cleaned = endpoint
                .replace("/golf-cms/api/", "")
                .replace("/golf-maintenance/api/", "");

        return cleaned.split("/")[0]; // booking/payment/report...
    }

    // ============================================================
    // == 6. TÌM VỊ TRÍ CHÈN VÀO NHÓM
    // ============================================================
    private static int findGroupInsertPosition(List<String> lines, String group) {
        String marker = "// " + group;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(marker)) {
                return i + 1;
            }
        }

        return -1;
    }

    // ============================================================
    // == 7. SORT CONSTANT TRONG NHÓM
    // ============================================================
    private static void sortGroup(List<String> lines, String group) {

        String marker = "// " + group;
        int start = -1;

        // Tìm nhóm
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(marker)) {
                start = i + 1;
                break;
            }
        }
        if (start == -1) return;

        // Tìm block nhóm
        int end = start;
        while (end < lines.size() && lines.get(end).trim().startsWith("public static final")) {
            end++;
        }

        // Sort
        List<String> block = new ArrayList<>(lines.subList(start, end));
        block.sort(Comparator.naturalOrder());

        // Replace
        for (int i = 0; i < block.size(); i++) {
            lines.set(start + i, block.get(i));
        }
    }
}
