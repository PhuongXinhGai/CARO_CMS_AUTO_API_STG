package common.utilities;

import org.testng.ITestContext;

import java.util.Set;

public class ContextHelper {

//    public static void clearContext(ITestContext ctx) {
//        if (ctx == null) return;
//
//        Set<String> keys = ctx.getAttributeNames();
//        for (String key : keys) {
//            ctx.removeAttribute(key);
//        }
//    }
    public static void clearContext(ITestContext ctx) {
        System.out.println("Before clear: " + ctx.getAttributeNames());
        if (ctx == null) return;

        Set<String> keys = ctx.getAttributeNames();
        for (String key : keys) {
            ctx.removeAttribute(key);
        }

        System.out.println("After clear: " + ctx.getAttributeNames());
    }

}
