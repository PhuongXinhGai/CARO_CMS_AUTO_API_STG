package common.utilities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ChecksumHelper {
    /** SHA-256 -> hex lowercase */
    public static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate SHA-256", e);
        }
    }
}
