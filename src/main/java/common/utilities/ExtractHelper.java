package common.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.ITestContext;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ExtractHelper {
    private ExtractHelper() {
        // tránh khởi tạo
    }

    /**
     * Extract voucher_apply_uid theo player index
     *
     * playerIndex = 0 → ROUND_ID_0, ROUND_ID_0_ROUND2
     * playerIndex = 1 → ROUND_ID_1, ROUND_ID_1_ROUND2
     * Extract response của api fee_of_bag
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

                // id của round từ response → convert về long
                Object idObj = round.get("id");
                long ridLong;

                if (idObj instanceof Number) {
                    ridLong = ((Number) idObj).longValue();
                } else {
                    ridLong = Long.parseLong(String.valueOf(idObj));
                }

                // id từ context → convert về long
                long roundIdLong1 = roundId1 == null ? -1L : Long.parseLong(roundId1);
                long roundIdLong2 = roundId2 == null ? -1L : Long.parseLong(roundId2);

                String suffix = null;

                if (ridLong == roundIdLong1) {
                    suffix = String.valueOf(playerIndex);
                } else if (ridLong == roundIdLong2) {
                    suffix = playerIndex + "_ROUND2";
                } else {
                    continue;
                }

                // Extract vouchers như cũ
                List<Map<String, Object>> vouchers = (List<Map<String, Object>>) round.get("vouchers");
                if (vouchers == null) continue;

                for (Map<String, Object> v : vouchers) {
                    String voucherCode = String.valueOf(v.get("voucher_code"));
                    String voucherApplyUid = String.valueOf(v.get("voucher_apply_uid"));

                    if (voucherApplyUid == null || voucherApplyUid.equals("null")) continue;

                    String key = "VOUCHER_APPLY_UID_" + voucherCode + "_" + suffix;
                    ctx.setAttribute(key, voucherApplyUid);

                    System.out.println("🔖 Saved to ctx: " + key + "=" + voucherApplyUid);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Extract voucher_apply_uid failed: " + e.getMessage());
        }
    }

    // ================== 2) BULK APPLY – đọc từ requestBody ==================
    public static void extractVoucherApplyBulk(String requestBody, ITestContext ctx) {
        try {
            Gson gson = new Gson();
            Type reqType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> reqMap = gson.fromJson(requestBody, reqType);

            Object dataObj = reqMap.get("data");
            if (!(dataObj instanceof List<?>)) {
                System.out.println("⚠️ data[] not found in request body");
                return;
            }

            List<?> dataList = (List<?>) dataObj;

            for (Object itemObj : dataList) {
                if (!(itemObj instanceof Map<?, ?>)) continue;
                Map<String, Object> dataItem = (Map<String, Object>) itemObj;

                // ================================
                // 1) Xử lý mảng voucher_apply[]
                // ================================
                Object vaObj = dataItem.get("voucher_apply");
                if (vaObj instanceof List<?>) {
                    List<?> vouchers = (List<?>) vaObj;

                    for (Object vObj : vouchers) {
                        if (!(vObj instanceof Map<?, ?>)) continue;

                        Map<String, Object> v = (Map<String, Object>) vObj;

                        // voucher_code
                        String voucherCode = String.valueOf(v.get("voucher_code"));

                        // id (convert tránh 35537.0)
                        Object idObj = v.get("id");
                        String id;
                        if (idObj instanceof Number) {
                            id = String.valueOf(((Number) idObj).longValue());
                        } else {
                            id = String.valueOf(idObj);
                        }

                        // Save vào context
                        ctx.setAttribute("VOUCHER_CODE_" + voucherCode, voucherCode);
                        ctx.setAttribute("VOUCHER_ID_" + voucherCode, id);

                        System.out.println("🔖 Saved: VOUCHER_CODE_" + voucherCode + "=" + voucherCode);
                        System.out.println("🔖 Saved: VOUCHER_ID_" + voucherCode + "=" + id);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ extractVoucherApplyBulk failed: " + e.getMessage());
        }
    }



}
