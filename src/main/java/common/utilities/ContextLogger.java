// src/main/java/common/utilities/ContextLogger.java
package common.utilities;

import org.testng.ITestContext;
import com.aventstack.extentreports.ExtentTest; // extentreports là dependency chung (main/test)

public class ContextLogger {
    public static void logContextAttributes(ITestContext context, ExtentTest test) {
        if (test == null) return;
        test.info("=== Dump ITestContext Attributes ===");
        for (String name : context.getAttributeNames()) {
            Object value = context.getAttribute(name);
            test.info(name + " = " + (value != null ? value.toString() : "null"));
        }
    }
}
