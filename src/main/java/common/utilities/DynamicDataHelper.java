package common.utilities; // <-- Gói này phải khớp với vị trí bạn đặt file

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import common.utilities.ConfigReader;


public class DynamicDataHelper {

    // Định dạng ngày tháng mà API của bạn mong muốn, ví dụ: "dd/MM/yyyy"
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public static String resolveDynamicValue(String value) {
        if (value == null || !value.contains("{{")) {
            // Nếu không chứa từ khóa, trả về ngay lập tức để tiết kiệm thời gian
            return value;
        }

        // Tạo các pattern để tìm kiếm từ khóa
        // Pattern cho {{TODAY}}
        Pattern todayPattern = Pattern.compile("\\{\\{TODAY\\}\\}", Pattern.CASE_INSENSITIVE);
        // Pattern cho {{TODAY+n}} hoặc {{TODAY-n}}
        Pattern todayMathPattern = Pattern.compile("\\{\\{TODAY\\s*([+-])\\s*(\\d+)\\}\\}", Pattern.CASE_INSENSITIVE);

        // --- Xử lý {{TODAY}} ---
        Matcher todayMatcher = todayPattern.matcher(value);
        if (todayMatcher.find()) {
            String todayString = LocalDate.now().format(DATE_FORMATTER);
            value = todayMatcher.replaceAll(todayString);
        }

        // --- Xử lý {{TODAY+/-n}} ---
        Matcher todayMathMatcher = todayMathPattern.matcher(value);
        while (todayMathMatcher.find()) {
            String operator = todayMathMatcher.group(1); // Lấy dấu "+" hoặc "-"
            int days = Integer.parseInt(todayMathMatcher.group(2)); // Lấy số ngày
            LocalDate newDate;
            if (operator.equals("+")) {
                newDate = LocalDate.now().plusDays(days);
            } else {
                newDate = LocalDate.now().minusDays(days);
            }
            value = value.replace(todayMathMatcher.group(0), newDate.format(DATE_FORMATTER));
        }

        return value;
    }
}