package common.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class RequestLogHelper {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private RequestLogHelper() {
        // helper, không cho new
    }

    /**
     * Build log request dạng JSON đẹp cho mọi method.
     *
     * @param method      GET/POST/PUT/DELETE
     * @param url         URL gốc (chưa có query string)
     * @param queryParams Map query (có thể null)
     * @param body        Body request:
     *                    - String JSON
     *                    - Map / List (sẽ tự convert sang JSON)
     *                    - null nếu không có body
     */
    public static String buildRequestLog(String method,
                                         String url,
                                         Map<String, ?> queryParams,
                                         Object body) {

        Map<String, Object> log = new LinkedHashMap<>();
        log.put("method", method);
        log.put("url", url);

        // ===== Query params =====
        if (queryParams != null && !queryParams.isEmpty()) {
            log.put("query", queryParams);

            String queryString = queryParams.entrySet().stream()
                    .map(e -> e.getKey() + "=" + String.valueOf(e.getValue()))
                    .collect(Collectors.joining("&"));

            log.put("full_url", url + "?" + queryString);
        }

        // ===== Body =====
        if (body != null) {
            if (body instanceof String) {
                String s = ((String) body).trim();
                if (looksLikeJson(s)) {
                    // parse json string -> object để pretty hơn
                    log.put("body", GSON.fromJson(s, Object.class));
                } else {
                    log.put("body", s);
                }
            } else {
                // Map, List, custom object... -> convert JSON
                log.put("body", body);
            }
        }

        // Trả về chuỗi JSON đẹp
        return GSON.toJson(log);
    }

    private static boolean looksLikeJson(String s) {
        return (s.startsWith("{") && s.endsWith("}"))
                || (s.startsWith("[") && s.endsWith("]"));
    }
}
