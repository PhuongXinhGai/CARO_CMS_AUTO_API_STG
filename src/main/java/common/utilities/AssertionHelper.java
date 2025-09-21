package common.utilities;

import com.jayway.jsonpath.JsonPath;
import org.testng.Assert;

import java.util.*;
import java.util.regex.Pattern;

public class AssertionHelper {

    // Map các toán tử explicit
    private static final Map<String, TriConsumer<String, Object, Object>> OPS = new HashMap<>();

    static {
        OPS.put("equals", (path, actual, expected) ->
                Assert.assertEquals(actual, expected, path + " equals check failed"));

        OPS.put("notNull", (path, actual, expected) ->
                Assert.assertNotNull(actual, path + " should not be null"));

        OPS.put("regex", (path, actual, expected) -> {
            Assert.assertTrue(Pattern.matches((String) expected, String.valueOf(actual)),
                    path + " regex check failed. Actual=" + actual);
        });

        OPS.put("gte", (path, actual, expected) -> {
            double a = Double.parseDouble(String.valueOf(actual));
            double e = Double.parseDouble(String.valueOf(expected));
            Assert.assertTrue(a >= e, path + " gte check failed. Actual=" + a + ", Expected >=" + e);
        });

        OPS.put("in", (path, actual, expected) -> {
            Assert.assertTrue(((List<?>) expected).contains(actual),
                    path + " in check failed. Actual=" + actual + ", Expected in " + expected);
        });
    }

    // Hàm chính: đọc expect JSON, check implicit + explicit
    public static void assertFromJson(String responseJson, Map<String, Object> expectJson) {
        // 1) Implicit assertions (key-value)
        Map<String, Object> implicit = (Map<String, Object>) expectJson.get("assertions");
        if (implicit != null) {
            for (Map.Entry<String, Object> entry : implicit.entrySet()) {
                String path = "$." + entry.getKey();
                String rule = String.valueOf(entry.getValue());
                Object actual = JsonPath.read(responseJson, path);

                if ("NOT_NULL".equalsIgnoreCase(rule)) {
                    OPS.get("notNull").accept(path, actual, null);
                } else if (rule.startsWith("REGEX:")) {
                    String pattern = rule.substring(6);
                    OPS.get("regex").accept(path, actual, pattern);
                } else {
                    OPS.get("equals").accept(path, actual, rule);
                }
            }
        }

        // 2) Explicit assertions (list of object)
        List<Map<String, Object>> explicit = (List<Map<String, Object>>) expectJson.get("assertions_explicit");
        if (explicit != null) {
            for (Map<String, Object> a : explicit) {
                String path = (String) a.get("path");
                String op = (String) a.get("op");
                Object expected = a.get("value");
                Object actual = JsonPath.read(responseJson, path);

                TriConsumer<String, Object, Object> func = OPS.get(op);
                if (func == null) throw new RuntimeException("Unsupported operator: " + op);
                func.accept(path, actual, expected);
            }
        }
    }

    // Interface nhỏ để dùng lambda 3 tham số
    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
