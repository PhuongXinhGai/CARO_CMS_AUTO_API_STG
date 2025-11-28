package generate;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstantsGenerator {

    private static final String CONSTANTS_FILE =
            System.getProperty("user.dir") + "/src/main/java/common/utilities/Constants.java";

    // public static final String QUOTE_FEE_ENDPOINT = "/golf-cms/api/booking/quote-fee";
    private static final Pattern CONST_PATTERN = Pattern.compile(
            "public\\s+static\\s+final\\s+String\\s+([A-Z0-9_]+)\\s*=\\s*\"([^\"]+)\"\\s*;");

    // ================== API chính dùng cho ScriptGenerator ==================

    /**
     * Đảm bảo endpoint có constant trong Constants.java.
     * - Nếu đã có: trả về tên constant cũ.
     * - Nếu chưa có: tạo mới, chèn vào đúng group, rồi trả về tên constant mới.
     */
    public static String getOrCreateConstant(String endpoint) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(CONSTANTS_FILE));

        // 1) Build map: endpoint value -> constant name
        Map<String, String> valueToName = new HashMap<>();
        for (String line : lines) {
            Matcher m = CONST_PATTERN.matcher(line.trim());
            if (m.find()) {
                String name  = m.group(1).trim();
                String value = m.group(2).trim();
                valueToName.put(value, name);
            }
        }

        // 2) Nếu endpoint đã được khai báo -> dùng luôn
        if (valueToName.containsKey(endpoint)) {
            return valueToName.get(endpoint);
        }

        // 3) Chưa có -> tạo mới
        String constName = buildConstantName(endpoint);
        insertConstant(lines, constName, endpoint);

        // 4) Ghi lại file
        Files.write(Paths.get(CONSTANTS_FILE), lines);

        return constName;
    }

    // ================== Helpers ==================

    /**
     * Rule: bỏ prefix /golf-xxx/api/, phần còn lại:
     *  - group = segment đầu tiên
     *  - phần sau -> SNAKE_CASE + _ENDPOINT
     *
     *  vd: /golf-cms/api/booking/list/select
     *      -> booking/list/select
     *      -> group = booking
     *      -> name  = LIST_SELECT_ENDPOINT
     */
    private static String buildConstantName(String endpoint) {
        String noPrefix = endpoint.replaceFirst("^/golf-[a-zA-Z0-9_-]+/api/", "");
        String[] parts  = noPrefix.split("/");

        if (parts.length <= 1) {
            // fallback: dùng full path
            String snake = noPrefix
                    .replaceAll("[^a-zA-Z0-9]", "_")
                    .replaceAll("_+", "_")
                    .toUpperCase();
            return snake + "_ENDPOINT";
        }

        // bỏ group ở đầu, chỉ lấy phần sau group
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) sb.append("_");
            sb.append(parts[i].toUpperCase().replace("-", "_"));
        }
        return sb + "_ENDPOINT";
    }

    /** Lấy group theo prefix sau /api/ (booking, payment, course-operating, ...) */
    private static String extractGroup(String endpoint) {
        String noPrefix = endpoint.replaceFirst("^/golf-[a-zA-Z0-9_-]+/api/", "");
        String[] parts  = noPrefix.split("/");
        return parts.length > 0 ? parts[0] : "misc";
    }

    /**
     * Chèn constant vào đúng group, sort alphabet F7, và luôn nằm TRƯỚC dấu } cuối.
     * Format group: 1 dòng trống + comment // group
     */
    private static void insertConstant(List<String> lines, String constName, String endpoint) {
        String group          = extractGroup(endpoint);      // booking, payment, ...
        String groupHeader    = "    // " + group;
        String constLine      = "    public static final String " + constName + " = \"" + endpoint + "\";";

        // 1) tìm vị trí dấu } cuối cùng (để biết giới hạn chèn)
        int closingIndex = findClosingBraceIndex(lines);
        if (closingIndex == -1) {
            throw new RuntimeException("Không tìm thấy dấu '}' kết thúc class trong Constants.java");
        }

        // 2) tìm header group nếu đã có
        int groupHeaderIndex = -1;
        for (int i = 0; i < closingIndex; i++) {
            if (lines.get(i).trim().equals(groupHeader)) {
                groupHeaderIndex = i;
                break;
            }
        }

        if (groupHeaderIndex == -1) {
            // 3) Nhóm chưa tồn tại -> tạo mới ngay TRƯỚC closingIndex
            List<String> newBlock = new ArrayList<>();
            newBlock.add("");                // dòng trống trước group
            newBlock.add(groupHeader);
            newBlock.add(constLine);

            lines.addAll(closingIndex, newBlock);
        } else {
            // 4) Nhóm đã tồn tại -> thêm vào cuối nhóm rồi sort
            int start = groupHeaderIndex + 1;
            int end   = start;

            // lướt qua các dòng constant trong group
            while (end < closingIndex && lines.get(end).trim().startsWith("public static final String")) {
                end++;
            }

            // lấy block constants trong group
            List<String> groupConsts = new ArrayList<>(lines.subList(start, end));
            groupConsts.add(constLine);

            // sort alphabet
            groupConsts.sort(Comparator.naturalOrder());

            // ghi đè lại block đã sort
            for (int i = 0; i < groupConsts.size(); i++) {
                lines.set(start + i, groupConsts.get(i));
            }
        }
    }

    /** Tìm index dấu } cuối cùng (đóng class) */
    private static int findClosingBraceIndex(List<String> lines) {
        for (int i = lines.size() - 1; i >= 0; i--) {
            String t = lines.get(i).trim();
            if ("}".equals(t)) {
                return i;
            }
        }
        return -1;
    }
}