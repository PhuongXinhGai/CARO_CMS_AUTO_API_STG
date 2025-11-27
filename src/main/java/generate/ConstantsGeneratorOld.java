package generate;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ConstantsGeneratorOld {

    private static final String CONSTANTS_FILE =
            System.getProperty("user.dir") + "/src/main/java/common/utilities/Constants.java";

    // Pattern đọc dòng constant: public static final String ABC = "/golf-cms/api/..."
    private static final Pattern CONSTANT_PATTERN = Pattern.compile(
            "public static final String\\s+(\\w+)\\s*=\\s*\"([^\"]+)\""
    );

    private Map<String, String> endpointToConstant = new HashMap<>();

    public ConstantsGeneratorOld() throws Exception {
        loadExistingConstants();
    }

    /** ============================
     *  1. Load Constants.java
     * ============================ */
    private void loadExistingConstants() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(CONSTANTS_FILE));

        for (String line : lines) {
            Matcher m = CONSTANT_PATTERN.matcher(line.trim());
            if (m.find()) {
                String constName = m.group(1).trim();
                String constValue = m.group(2).trim();
                endpointToConstant.put(constValue, constName);
            }
        }

        System.out.println("✔ Loaded " + endpointToConstant.size() + " constant(s) từ Constants.java");
    }

    /** =================================================================
     *  2. MAIN FUNCTION — trả về constantName cho endpoint nhập từ Excel
     * ================================================================= */
    public String resolveConstantForEndpoint(String endpointFromExcel) throws Exception {

        if (endpointFromExcel == null || endpointFromExcel.isEmpty())
            throw new RuntimeException("❌ endpoint rỗng trong Excel!");

        // Nếu giá trị này đã tồn tại → dùng luôn constant cũ
        if (endpointToConstant.containsKey(endpointFromExcel)) {
            return endpointToConstant.get(endpointFromExcel);
        }

        // Chưa tồn tại → tạo mới
        String newConstName = buildConstantName(endpointFromExcel);

        // Append vào file Constants.java
        appendConstantToFile(newConstName, endpointFromExcel);

        // Thêm vào map local
        endpointToConstant.put(endpointFromExcel, newConstName);

        return newConstName;
    }

    /** ========================================
     *  3. Rule tạo CONSTANT NAME chuẩn của Phương
     * ======================================== */
    private String buildConstantName(String endpoint) {

        // Bỏ mọi prefix dạng /golf-xxx/api/
        String trimmed = endpoint.replaceFirst("^/golf-[a-zA-Z0-9_-]+/api/", "");

        // Chuẩn hóa thành snake_case
        String snake = trimmed
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .toUpperCase();

        return snake + "_ENDPOINT";
    }


    /** ================================================
     *  4. Append constant mới vào cuối Constants.java
     * ================================================ */
    private void appendConstantToFile(String constName, String endpointValue) throws Exception {

        List<String> lines = Files.readAllLines(Paths.get(CONSTANTS_FILE));

        // chèn trước dấu } cuối file
        int insertPos = lines.size() - 1;

        String newLine =
                "    public static final String " + constName +
                        " = \"" + endpointValue + "\";";

        lines.add(insertPos, newLine);

        Files.write(Paths.get(CONSTANTS_FILE), lines);

        System.out.println("➕ Added constant: " + constName + " = " + endpointValue);
    }
}