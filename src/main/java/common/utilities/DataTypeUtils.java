// src/main/java/common/utilities/DataTypeUtils.java
package common.utilities;

public final class DataTypeUtils {
    private DataTypeUtils() {}
    public static boolean isNumeric(String s) {
        if (s == null) return false;
        try { new java.math.BigDecimal(s); return true; }
        catch (NumberFormatException e) { return false; }
    }
    public static boolean isBoolean(String s) {
        return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
    }
}
