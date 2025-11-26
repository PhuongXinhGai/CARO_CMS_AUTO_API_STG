package common.utilities;

import org.testng.ITestContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class QueryParamHelper {

    /**
     * Build query params tự động:
     * - Cột Excel nào có prefix "q_" sẽ được map thành query param
     * - Tự remove prefix "q_"
     * - Tự resolve dynamic value (TODAY, {{CTX:...}}, v.v.)
     * - Cho phép dùng cả ctx & excel
     */
    public static Map<String, Object> build(Map<String, String> row, ITestContext ctx) {
        Map<String, Object> q = new LinkedHashMap<>();

        for (Map.Entry<String, String> e : row.entrySet()) {
            String col = e.getKey();

            // Chỉ xử lý cột có prefix q_
            if (col.startsWith("q_")) {
                String paramName = col.substring(2);   // bỏ "q_"
                String rawVal = e.getValue();

                if (rawVal == null || rawVal.isEmpty()) {
                    continue; // skip param rỗng
                }

                // Resolve dynamic value (TODAY, TODAY+1, {{CTX:...}}, ...)
                String resolved = DynamicDataHelper.resolveDynamicValue(rawVal);

                // Nếu value vẫn chứa {{CTX:KEY}} thì lấy từ context
                resolved = resolveContextIfNeeded(resolved, ctx);

                q.put(paramName, resolved);
            }
        }

        return q;
    }

    /**
     * Nếu value dạng {{CTX:KEY}} → tự động lấy ctx.getAttribute(KEY)
     */
    private static String resolveContextIfNeeded(String val, ITestContext ctx) {
        if (val != null && val.startsWith("{{CTX:") && val.endsWith("}}")) {
            String key = val.substring(6, val.length() - 2); // cắt {{CTX: ... }}
            Object ctxVal = ctx.getAttribute(key);
            return ctxVal != null ? ctxVal.toString() : "";
        }
        return val;
    }
}
