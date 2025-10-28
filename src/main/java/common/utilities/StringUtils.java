package common.utilities;

import org.testng.ITestContext;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // 1️⃣ Replace placeholder từ Excel map như bình thường
        for (Map.Entry<String, String> e : ctx.entrySet()) {
            String key = "${" + e.getKey() + "}";
            String val = e.getValue() == null ? "" : e.getValue();

            // xử lý dynamic {{TODAY}}, {{TODAY+N}}, ...
            val = DynamicDataHelper.resolveDynamicValue(val);

            out = out.replace(key, val);
        }
        return out;
    }

    /**
     * 🔹 Hàm nâng cao: Replace placeholder hỗn hợp (Excel + Context + ENV + Dynamic)
     *
     *  - ${key}             → lấy từ Excel map (row)
     *  - {{CTX:key}}        → lấy từ ITestContext
     *  - {{ENV:key}}        → lấy từ config.properties
     *  - {{TODAY}}, {{TODAY+N}} → dynamic date/time
     *  - {{RAND:xxx}}       → random data (VD RAND:PHONE)
     */
    public static String replacePlaceholdersAdvanced(String raw, Map<String, String> excelMap, ITestContext testCtx) {
        if (raw == null) return "";

        String result = raw;

        // ✅ Replace ${key} từ Excel map
        for (Map.Entry<String, String> e : excelMap.entrySet()) {
            String key = "${" + e.getKey() + "}";
            String val = e.getValue() == null ? "" : e.getValue();

            // Xử lý dynamic {{TODAY}}, {{TODAY+N}}, ...
//            val = DynamicDataHelper.resolveDynamicValue(val);

            // Nếu giá trị trong Excel là {{CTX:...}} → lấy từ context
            if (val.matches("\\{\\{CTX:[^}]+}}")) {
                String ctxKey = val.substring(6, val.length() - 2); // cắt "{{CTX:" và "}}"
                Object ctxVal = testCtx != null ? testCtx.getAttribute(ctxKey) : null;
                val = ctxVal != null ? ctxVal.toString() : "";
            }

            // ✅ Gọi bản dynamic mới: xử lý {{TODAY}}, {{TODAY±N}}, {{CHECKSUM}}, ...
            val = DynamicDataHelper.resolveDynamicValue(val, testCtx);

            // Nếu giá trị là {{ENV:...}} → lấy từ config.properties
//            else if (val.matches("\\{\\{ENV:[^}]+}}")) {
//                String envKey = val.substring(6, val.length() - 2);
//                val = ConfigHelper.get(envKey);
//            }

            // Replace trong chuỗi
            result = result.replace(key, val);
        }

        return result;
    }



}
