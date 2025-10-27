package framework.core;

import java.util.*;

public class ApiRegistry {

//    Map cột Excel → class test tương ứng
//    private static final Map<String, String> MAP = Map.of(
//            "login_case_id", "tests.test_scripts.api.user.account.LoginTest",
//
//            "quote_fee_case_id", "tests.test_scripts.api.booking.create_booking.QuoteFeeTest",
//
//            "create_booking_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatchTest",
//            "create_booking_Voucher_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatchVoucherTest",
//            "create_booking_4_player_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatch4PlayerTest",
//
//            "edit_booking_at_tee_time_id", "tests.test_scripts.api.booking.edit_booking.EditBookingAtTeeTimeTest",
//            "edit_booking_1_player_id", "tests.test_scripts.api.booking.edit_booking.EditBooking1PlayerTest",
//
//            "get_list_case_id", "tests.test_scripts.api.booking.create_booking.GetBookingListSelectTest",
//            "get_booking_price_case_id", "tests.test_scripts.api.booking.create_booking.GetBookingPriceTest",
//
//            "check_in_bag_id", "tests.test_scripts.api.booking.checkin.CheckInBagTest"
//    );
    private static final Map<String, String> MAP = new HashMap<>() {{
        put("login_case_id", "tests.test_scripts.api.user.account.LoginTest");

        put("quote_fee_case_id", "tests.test_scripts.api.booking.create_booking.QuoteFeeTest");

        put("create_booking_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatchTest");
        put("create_booking_Voucher_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatchVoucherTest");
        put("create_booking_4_player_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatch4PlayerTest");

        put("edit_booking_at_tee_time_id", "tests.test_scripts.api.booking.edit_booking.EditBookingAtTeeTimeTest");
        put("edit_booking_1_player_id", "tests.test_scripts.api.booking.edit_booking.EditBooking1PlayerTest");

        put("get_list_case_id", "tests.test_scripts.api.booking.create_booking.GetBookingListSelectTest");
        put("get_booking_price_case_id", "tests.test_scripts.api.booking.create_booking.GetBookingPriceTest");

        put("check_in_bag_id", "tests.test_scripts.api.booking.checkin.CheckInBagTest");
        put("undo_check_in_bag_id", "tests.test_scripts.api.booking.checkin.UndoCheckInBagTest");
    }};

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
                "create_booking_Voucher_case_id",
                "create_booking_4_player_case_id",

                "edit_booking_at_tee_time_id",
                "edit_booking_1_player_id",

                "get_list_case_id",
                "get_booking_price_case_id",

                "check_in_bag_id",
                "undo_check_in_bag_id"
        );
    }
}
