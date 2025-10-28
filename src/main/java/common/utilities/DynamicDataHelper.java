package common.utilities; // <-- Gói này phải khớp với vị trí bạn đặt file

import org.testng.ITestContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicDataHelper {
    // ===== Config ngày tháng =====
    private static final ZoneId   DEFAULT_ZONE   = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ====== Regex pattern ======
    private static final Pattern TODAY_PATTERN      = Pattern.compile("\\{\\{TODAY}}", Pattern.CASE_INSENSITIVE);
    private static final Pattern TODAY_MATH_PATTERN = Pattern.compile("\\{\\{TODAY\\s*([+-])\\s*(\\d+)}}", Pattern.CASE_INSENSITIVE);

    // NEW: {{CHECKSUM}}
    private static final Pattern CHECKSUM_PATTERN   = Pattern.compile("\\{\\{CHECKSUM}}", Pattern.CASE_INSENSITIVE);

    private DynamicDataHelper() {}

    /** Backward-compat (không có ctx): xử lý TODAY, TODAY±N, bỏ qua CHECKSUM */
    public static String resolveDynamicValue(String input) {
        return resolveDynamicValue(input, null);
    }

    /** Bản đầy đủ: xử lý TODAY, TODAY±N, CHECKSUM (cần ctx) */
    public static String resolveDynamicValue(String input, ITestContext ctx) {
        if (input == null || !input.contains("{{")) return input;

        String value = input;

        // 1) {{TODAY}}
        value = TODAY_PATTERN.matcher(value).replaceAll(today());

        // 2) {{TODAY+N}} / {{TODAY-N}}
        Matcher m = TODAY_MATH_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String op   = m.group(1); // + | -
            int days    = Integer.parseInt(m.group(2));
            LocalDate base = LocalDate.now(DEFAULT_ZONE);
            LocalDate d = op.equals("+") ? base.plusDays(days) : base.minusDays(days);
            m.appendReplacement(sb, Matcher.quoteReplacement(d.format(DATE_FORMATTER)));
        }
        m.appendTail(sb);
        value = sb.toString();

        // 3) {{CHECKSUM}}  (chỉ resolve nếu có ctx)
        if (ctx != null) {
            Matcher cs = CHECKSUM_PATTERN.matcher(value);
            if (cs.find()) {
                String apiKey      = ConfigReader.getProperty("api_key"); // chú ý key trong config.properties
                String partnerUid  = str(ctx.getAttribute("PARTNER_UID"));
                String courseUid   = str(ctx.getAttribute("COURSE_UID"));
                String bookingUid  = str(ctx.getAttribute("BOOKING_UID_0"));
                String bookingDate = str(ctx.getAttribute("BOOKING_DATE_0"));

                String raw = (apiKey==null?"":apiKey)
                        + partnerUid + courseUid + bookingUid + bookingDate;

                // 🧩 Log debug từng thành phần và chuỗi nối
                System.out.println("=========== DEBUG CHECKSUM ===========");
                System.out.println("api_key      = " + apiKey);
                System.out.println("partner_uid  = " + partnerUid);
                System.out.println("course_uid   = " + courseUid);
                System.out.println("booking_uid  = " + bookingUid);
                System.out.println("booking_date = " + bookingDate);
                System.out.println("-------------------------------------");
                System.out.println("RAW String   = [" + raw + "]");
                System.out.println("SHA256 HEX   = " + ChecksumHelper.sha256(raw));
                System.out.println("=====================================");

                String checksum = ChecksumHelper.sha256(raw);
                value = cs.replaceAll(Matcher.quoteReplacement(checksum));
            }
        }

        return value;
    }

    private static String today() {
        return LocalDate.now(DEFAULT_ZONE).format(DATE_FORMATTER);
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }
/*
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

 */
}