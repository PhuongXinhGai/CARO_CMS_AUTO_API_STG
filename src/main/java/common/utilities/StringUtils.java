package common.utilities;

import java.util.Map;

public class StringUtils {

    private StringUtils() {
        // private constructor để tránh khởi tạo class helper
    }

    /**
     * Thay thế các placeholder dạng ${var} trong chuỗi bằng giá trị trong ctx.
     *
     * @param raw chuỗi gốc chứa placeholder
     * @param ctx Map key-value để thay thế
     * @return chuỗi sau khi đã replace
     */
    /** Replace ${key} trong chuỗi bằng value từ Map (giá trị null sẽ thay bằng chuỗi rỗng). */
    public static String replacePlaceholdersInString(String raw, Map<String, String> ctx) {
        String out = raw;
        for (Map.Entry<String, String> e : ctx.entrySet()) {
            String key = "${" + e.getKey() + "}";
            String val = e.getValue() == null ? "" : e.getValue();

            // xử lý dynamic {{TODAY}}, {{TODAY+N}}, ...
            val = DynamicDataHelper.resolveDynamicValue(val);

            out = out.replace(key, val);
        }
        return out;
    }
}
