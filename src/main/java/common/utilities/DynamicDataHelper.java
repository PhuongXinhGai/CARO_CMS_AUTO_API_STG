package common.utilities; // <-- Gói này phải khớp với vị trí bạn đặt file

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicDataHelper {

    // Định dạng ngày tháng mà API của bạn mong muốn, ví dụ: "dd/MM/yyyy"
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Phương thức này sẽ phân tích một chuỗi đầu vào.
     * Nếu chuỗi đó là một từ khóa động, nó sẽ sinh ra dữ liệu tương ứng.
     * Nếu không, nó sẽ trả về chính chuỗi đó.
     * @param value Chuỗi đọc từ Excel (ví dụ: "{{TODAY}}", "{{TODAY+5}}")
     * @return Dữ liệu đã được xử lý (ví dụ: "10/08/2025", "15/08/2025")
     */
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
        // Dùng vòng lặp while để thay thế tất cả các lần xuất hiện
        while (todayMathMatcher.find()) {
            String operator = todayMathMatcher.group(1); // Lấy dấu "+" hoặc "-"
            int days = Integer.parseInt(todayMathMatcher.group(2)); // Lấy số ngày
            LocalDate newDate;

            if (operator.equals("+")) {
                newDate = LocalDate.now().plusDays(days);
            } else {
                newDate = LocalDate.now().minusDays(days);
            }
            // Thay thế từ khóa tìm thấy bằng ngày đã tính toán
            value = value.replace(todayMathMatcher.group(0), newDate.format(DATE_FORMATTER));
        }

        // Trả về chuỗi sau khi đã xử lý tất cả các từ khóa
        return value;
    }
}