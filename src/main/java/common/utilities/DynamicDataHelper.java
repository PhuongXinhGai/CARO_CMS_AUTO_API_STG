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
    private static final Pattern CHECKSUM_EKYC_PATTERN = Pattern.compile("\\{\\{CHECKSUM_EKYC_(\\d+)}}", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHECKSUM_SINGLE_PAYMENT_PATTERN = Pattern.compile("\\{\\{CHECKSUM_SINGLE_PAYMENT_\\d+}}", Pattern.CASE_INSENSITIVE);

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

        // 3) {{CHECKSUM_EKYC_N}} – xử lý đa index (0, 1, 2, ...)
        if (ctx != null) {
            Matcher cs = CHECKSUM_EKYC_PATTERN.matcher(value);
            StringBuffer sbCs = new StringBuffer();

            while (cs.find()) {
                // Lấy index N trong CHECKSUM_EKYC_N
                String idx = cs.group(1);

                // ===== Lấy dữ liệu từ context =====
                String apiKey      = ConfigReader.getProperty("api_key"); // lấy từ config.properties
                String partnerUid  = str(ctx.getAttribute("PARTNER_UID"));
                String courseUid   = str(ctx.getAttribute("COURSE_UID"));
                String bookingUid  = str(ctx.getAttribute("BOOKING_UID_" + idx));
                String bookingDate = str(ctx.getAttribute("BOOKING_DATE_" + idx));

                // ===== Tạo chuỗi raw và mã hóa SHA256 =====
                String raw = (apiKey == null ? "" : apiKey)
                        + partnerUid + courseUid + bookingUid + bookingDate;
                String checksum = ChecksumHelper.sha256(raw);

                // ===== Log debug chi tiết =====
                System.out.println("=========== DEBUG CHECKSUM_EKYC_" + idx + " ===========");
                System.out.println("api_key      = " + apiKey);
                System.out.println("partner_uid  = " + partnerUid);
                System.out.println("course_uid   = " + courseUid);
                System.out.println("booking_uid  = " + bookingUid);
                System.out.println("booking_date = " + bookingDate);
                System.out.println("-------------------------------------");
                System.out.println("RAW String   = [" + raw + "]");
                System.out.println("SHA256 HEX   = " + checksum);
                System.out.println("=====================================");

                // Thay thế chính xác match hiện tại bằng checksum tương ứng
                cs.appendReplacement(sbCs, Matcher.quoteReplacement(checksum));
            }

            cs.appendTail(sbCs);
            value = sbCs.toString();
        }


        // 3) {{CHECKSUM}}  (chỉ resolve nếu có ctx)
//        if (ctx != null) {
//            Matcher cs = CHECKSUM_PATTERN.matcher(value);
//            if (cs.find()) {
//                String apiKey      = ConfigReader.getProperty("api_key"); // chú ý key trong config.properties
//                String partnerUid  = str(ctx.getAttribute("PARTNER_UID"));
//                String courseUid   = str(ctx.getAttribute("COURSE_UID"));
//                String bookingUid  = str(ctx.getAttribute("BOOKING_UID_0"));
//                String bookingDate = str(ctx.getAttribute("BOOKING_DATE_0"));
//
//                String raw = (apiKey==null?"":apiKey)
//                        + partnerUid + courseUid + bookingUid + bookingDate;
//
//                // 🧩 Log debug từng thành phần và chuỗi nối
//                System.out.println("=========== DEBUG CHECKSUM ===========");
//                System.out.println("api_key      = " + apiKey);
//                System.out.println("partner_uid  = " + partnerUid);
//                System.out.println("course_uid   = " + courseUid);
//                System.out.println("booking_uid  = " + bookingUid);
//                System.out.println("booking_date = " + bookingDate);
//                System.out.println("-------------------------------------");
//                System.out.println("RAW String   = [" + raw + "]");
//                System.out.println("SHA256 HEX   = " + ChecksumHelper.sha256(raw));
//                System.out.println("=====================================");
//
//                String checksum = ChecksumHelper.sha256(raw);
//                value = cs.replaceAll(Matcher.quoteReplacement(checksum));
//            }
//        }

        // 4) {{CHECKSUM_SINGLE_PAYMENT_N}} – hỗ trợ nhiều index
        if (ctx != null) {
            Matcher csp = CHECKSUM_SINGLE_PAYMENT_PATTERN.matcher(value);
            StringBuffer sbCsp = new StringBuffer();

            while (csp.find()) {
                // Lấy index (VD: 0, 1, 2, ...)
                String fullMatch = csp.group();
                Matcher idxMatcher = Pattern.compile("CHECKSUM_SINGLE_PAYMENT_(\\d+)", Pattern.CASE_INSENSITIVE).matcher(fullMatch);
                String idx = "0";
                if (idxMatcher.find()) idx = idxMatcher.group(1);

                // ===== Lấy dữ liệu từ context =====
                String base64Key  = str(ctx.getAttribute("REACT_APP_KEY_256"));
                String billCode   = str(ctx.getAttribute("BILL_CODE_" + idx));
                String bookingUid = str(ctx.getAttribute("BOOKING_UID_" + idx));
                String dateStr    = today();

                // ===== Giải mã base64 key =====
                String decodedKey = "";
                try {
                    decodedKey = new String(java.util.Base64.getDecoder().decode(base64Key));
                } catch (Exception e) {
                    System.err.println("⚠️ Base64 decode lỗi cho REACT_APP_KEY_256: " + e.getMessage());
                }

                // ===== Ghép chuỗi và hash =====
                String raw = decodedKey + "|" + billCode + "|" + bookingUid + "|" + dateStr;
                String checksum = ChecksumHelper.sha256(raw);

                // ===== Log debug từng cái =====
                System.out.println("=========== DEBUG CHECKSUM_SINGLE_PAYMENT_" + idx + " ===========");
                System.out.println("decodedKey  = " + decodedKey);
                System.out.println("billCode    = " + billCode);
                System.out.println("bookingUid  = " + bookingUid);
                System.out.println("dateStr     = " + dateStr);
                System.out.println("--------------------------------------------");
                System.out.println("RAW String  = [" + raw + "]");
                System.out.println("SHA256 HEX  = " + checksum);
                System.out.println("============================================");

                // Dùng appendReplacement để thay thế chính xác từng match
                csp.appendReplacement(sbCsp, Matcher.quoteReplacement(checksum));
            }

            // Ghép phần còn lại
            csp.appendTail(sbCsp);
            value = sbCsp.toString();
        }



        return value;
    }

    private static String today() {
        return LocalDate.now(DEFAULT_ZONE).format(DATE_FORMATTER);
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }

}