package common.utilities;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.ITestContext;

import java.util.List;
import java.util.Map;

public class ExtractHelper {

    /**
     * Extract voucher_apply_uid theo player index
     *
     * playerIndex = 0 → ROUND_ID_0, ROUND_ID_0_ROUND2
     * playerIndex = 1 → ROUND_ID_1, ROUND_ID_1_ROUND2
     * ...
     */
    public static void extractVoucherApplyUid(Response resp, ITestContext ctx, int playerIndex) {
        try {
            JsonPath jp = resp.jsonPath();
            List<Map<String, Object>> rounds = jp.getList("rounds");

            if (rounds == null || rounds.isEmpty()) {
                System.out.println("⚠️ No rounds available in response");
                return;
            }

            // ==== Lấy ROUND_ID_X và ROUND_ID_X_ROUND2 từ ctx ====
            String roundKey1 = "ROUND_ID_" + playerIndex;
            String roundKey2 = "ROUND_ID_" + playerIndex + "_ROUND2";

            String roundId1 = ctx.getAttribute(roundKey1) != null ? ctx.getAttribute(roundKey1).toString() : null;
            String roundId2 = ctx.getAttribute(roundKey2) != null ? ctx.getAttribute(roundKey2).toString() : null;

            for (Map<String, Object> round : rounds) {
                String rid = String.valueOf(round.get("id"));
                String suffix = null;

                if (rid.equals(roundId1)) {
                    suffix = String.valueOf(playerIndex);
                } else if (rid.equals(roundId2)) {
                    suffix = playerIndex + "_ROUND2";
                } else {
                    continue;
                }

                // ==== Lấy danh sách vouchers trong round này ====
                List<Map<String, Object>> vouchers = (List<Map<String, Object>>) round.get("vouchers");
                if (vouchers == null) continue;

                for (Map<String, Object> v : vouchers) {
                    String voucherCode = String.valueOf(v.get("voucher_code"));
                    String voucherApplyUid = String.valueOf(v.get("voucher_apply_uid"));

                    // Key lưu lên ctx
                    String key = "VOUCHER_APPLY_UID_" + voucherCode + "_" + suffix;

                    ctx.setAttribute(key, voucherApplyUid);

                    System.out.println("🔖 Saved to ctx: " + key + "=" + voucherApplyUid);
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ Extract voucher_apply_uid failed: " + e.getMessage());
        }
    }
}
