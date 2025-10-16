package framework.core;

import java.util.*;

public class ApiRegistry {

//    Map cột Excel → class test tương ứng
    private static final Map<String, String> MAP = Map.of(
            "login_case_id", "tests.test_scripts.api.user.functional.LoginTest",
            "quote_fee_case_id", "tests.test_scripts.api.booking.functional.QuoteFeeTest",
            "create_booking_case_id", "tests.test_scripts.api.booking.functional.CreateBookingBatchTest",
            "get_list_case_id", "tests.test_scripts.api.booking.functional.GetBookingListSelectTest",
            "get_booking_price_case_id", "tests.test_scripts.api.booking.functional.GetBookingPriceTest"
    );

    /** Lấy class tương ứng với tên cột (VD: login_case_id -> tests.api.login.LoginTest) */
    public static String get(String colName) {
        return MAP.get(colName);
    }

    /** Thứ tự các cột chạy trong flow */
    public static List<String> orderedColumns() {
        return List.of(
                "login_case_id",
                "quote_fee_case_id",
                "create_booking_case_id",
                "get_list_case_id",
                "get_booking_price_case_id"
        );
    }
}
