package tests.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // Annotation này sẽ được dùng ở cấp độ Class
public @interface IntegrationFlow {
    String flowFile(); // Đường dẫn đến file IntegrationFlows.xlsx
    String sheetName() default "flows"; // Tên sheet, mặc định là "flows"
}