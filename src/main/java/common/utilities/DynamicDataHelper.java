package common.utilities; // <-- Gói này phải khớp với vị trí bạn đặt file

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DynamicDataHelper {

    // Định dạng ngày tháng mà API của bạn mong muốn, ví dụ: "dd/MM/yyyy"
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Nếu muốn theo giờ VN (đúng với bối cảnh của bạn), set zone cố định:
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final Pattern TODAY_PATTERN = Pattern.compile("\\{\\{TODAY\\}\\}", Pattern.CASE_INSENSITIVE);
    private static final Pattern TODAY_MATH_PATTERN = Pattern.compile("\\{\\{TODAY\\s*([+-])\\s*(\\d+)\\}\\}", Pattern.CASE_INSENSITIVE);

    public static String resolveDynamicValue(String input) {
        if (input == null || !input.contains("{{")) return input;

        String value = input;

        // 1) Thay {{TODAY}} trước (replaceAll đơn giản)
        String todayStr = LocalDate.now(DEFAULT_ZONE).format(DATE_FORMATTER);
        value = TODAY_PATTERN.matcher(value).replaceAll(todayStr);

        // 2) Thay {{TODAY±n}} bằng appendReplacement để không “lệch pha”
        Matcher m = TODAY_MATH_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String op = m.group(1);
            int days = Integer.parseInt(m.group(2));
            LocalDate date = LocalDate.now(DEFAULT_ZONE);
            date = op.equals("+") ? date.plusDays(days) : date.minusDays(days);
            String repl = date.format(DATE_FORMATTER);
            m.appendReplacement(sb, Matcher.quoteReplacement(repl));
        }
        m.appendTail(sb);
        value = sb.toString();

        return value;
    }
}