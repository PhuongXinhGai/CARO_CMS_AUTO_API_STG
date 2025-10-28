package framework.core;

import java.util.*;

public class ApiRegistry {
    /** Bản đồ ánh xạ giữa tên cột trong Excel và class thực thi test */
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
        put("check_in_ekyc_id", "tests.test_scripts.api.booking.checkin.CheckInEkycTest");
        put("undo_check_in_bag_id", "tests.test_scripts.api.booking.checkin.UndoCheckInBagTest");

        put("create_flight_1_player_id", "tests.test_scripts.api.go_course_information.CreateFlight1PlayerTest");
        put("out_all_flight_id", "tests.test_scripts.api.go_course_information.OutAllFlightTest");

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
                "check_in_ekyc_id",

                "create_flight_1_player_id",
                "out_all_flight_id"

        );
    }
}
