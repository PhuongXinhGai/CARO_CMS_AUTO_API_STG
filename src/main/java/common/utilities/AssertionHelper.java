package common.utilities;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

public class AssertionHelper {

    // Map các toán tử explicit
    private static final Map<String, TriConsumer<String, Object, Object>> OPS = new HashMap<>();

    static {
        OPS.put("equals", (path, actual, expectedRaw) -> {
            Object expected = normalizeExpected(expectedRaw, actual);

            // Numeric: so sánh số học để tránh lệch kiểu (Integer vs Double…)
            if (actual instanceof Number && expected instanceof Number) {
                BigDecimal a = new BigDecimal(actual.toString());
                BigDecimal e = new BigDecimal(expected.toString());
                Assert.assertEquals(a.compareTo(e), 0,
                        path + " equals check failed. Actual=" + a + ", Expected=" + e);
                return;
            }
            // Rỗng: coi là bằng nhau bất kể class gì
            // Nếu cả hai là Map -> so sánh bằng nội dung (dù khác class)
            if (actual instanceof Map || expected instanceof Map) {
                Map<?, ?> a = toMap(actual);
                Map<?, ?> e = toMap(expected);
                // Nếu cả hai map rỗng thì coi như pass
                if (a.isEmpty() && e.isEmpty()) return;

                Assert.assertEquals(a, e, path + " equals check failed (Map compare)");
                return;
            }

            // Mặc định: so sánh equals
            Assert.assertEquals(actual, expected, path + " equals check failed");
        });

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

        OPS.put("size", (path, actual, expectedRaw) -> {
//            int expected = Integer.parseInt(String.valueOf(expectedRaw));
            int expected;
            try {
                expected = new BigDecimal(String.valueOf(expectedRaw)).intValue();
            } catch (Exception e) {
                throw new RuntimeException("Invalid expected size value: " + expectedRaw, e);
            }

            int actualSize = -1;

            if (actual instanceof Collection) {
                actualSize = ((Collection<?>) actual).size();
            } else if (actual instanceof Map) {
                actualSize = ((Map<?, ?>) actual).size();
            } else if (actual != null && actual.getClass().isArray()) {
                actualSize = java.lang.reflect.Array.getLength(actual);
            } else {
                Assert.fail(path + " size check failed. Actual type is not edit_booking_at_tee_time_request.json Collection/Map/Array.");
            }

            Assert.assertEquals(actualSize, expected,
                    path + " size check failed. Actual size=" + actualSize + ", Expected=" + expected);
        });
    }

//    Hàm nghiệm thu status code riêng biệt
    public static void verifyStatusCode(Response response, Map<String, Object> expectJson) {
        Object expectedStatusObj = expectJson.get("status_code");

        if (expectedStatusObj == null) {
            System.out.println("⚠️ Missing 'status_code' in expected JSON → Skip status check");
            return;
        }

        int actualStatus = response.statusCode();
        int expectedStatus;

        try {
            // Dùng BigDecimal để chuyển an toàn các kiểu số
            expectedStatus = new java.math.BigDecimal(String.valueOf(expectedStatusObj)).intValue();
        } catch (Exception e) {
            throw new RuntimeException("Invalid status_code format in expect JSON: " + expectedStatusObj, e);
        }

        Assert.assertEquals(actualStatus, expectedStatus,
                "❌ HTTP status code mismatch");
    }

    // Hàm chính: đọc expect JSON, check implicit + explicit
    @SuppressWarnings("unchecked")
    public static void assertFromJson(String responseJson, Map<String, Object> expectJson) {

        // 1) Implicit assertions (key-value)
        Map<String, Object> implicit = (Map<String, Object>) expectJson.get("assertions");
        if (implicit != null) {
            for (Map.Entry<String, Object> entry : implicit.entrySet()) {
                String path = entry.getKey();
                String rule = String.valueOf(entry.getValue());
                Object actual = JsonPath.read(responseJson, path);

                if ("NOT_NULL".equalsIgnoreCase(rule)) {
                    OPS.get("notNull").accept(path, actual, null);
                } else if (rule.startsWith("REGEX:")) {
                    String pattern = rule.substring(6);
                    OPS.get("regex").accept(path, actual, pattern);
                } else {
                    // mềm hoá: nếu actual là boolean/number thì convert expected tương ứng
                    Object expected = coerceByActual(rule, actual);
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
    // Chuyển expected về đúng kiểu của actual (nếu có thể)
    // ----- helpers -----
    private static Object coerceByActual(String expectedRaw, Object actual) {
        if (actual instanceof Boolean) {
            if ("TRUE".equalsIgnoreCase(expectedRaw))  return true;
            if ("FALSE".equalsIgnoreCase(expectedRaw)) return false;
        }
        if (actual instanceof Number) {
            try { return new BigDecimal(expectedRaw); } catch (Exception ignore) {}
        }
        // giữ nguyên cho String/null…
        return expectedRaw;
    }

    private static Object normalizeExpected(Object expectedRaw, Object actual) {
        if (expectedRaw == null) return null;

        // Nếu actual là Boolean mà expectedRaw là "true"/"false" -> convert
        if (actual instanceof Boolean && expectedRaw instanceof String) {
            String s = (String) expectedRaw;
            if ("TRUE".equalsIgnoreCase(s))  return true;
            if ("FALSE".equalsIgnoreCase(s)) return false;
        }
        // Nếu actual là Number thì thử convert
        if (actual instanceof Number) {
            try {
                return new BigDecimal(String.valueOf(expectedRaw).trim());
            } catch (NumberFormatException ignore) {}
        }
        // Nếu cả hai là số -> đã xử lý ở equals phía trên
        return expectedRaw;
    }

    // 👉 Helper convert object về Map an toàn
    private static Map<?, ?> toMap(Object obj) {
        if (obj == null) return Collections.emptyMap();
        if (obj instanceof Map) return (Map<?, ?>) obj;

        try {
            // Trường hợp là String JSON như "{}" hoặc "{\"key\":1}"
            String json = String.valueOf(obj).trim();
            if ("{}".equals(json)) return Collections.emptyMap();

            if (json.startsWith("{") && json.endsWith("}")) {
                return new com.google.gson.Gson().fromJson(json, Map.class);
            }
        } catch (Exception ignore) {}

        return Collections.singletonMap("_raw", obj); // fallback để không null
    }


    // Interface nhỏ để dùng lambda 3 tham số
    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
