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

        put("check_in_bag_player1_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer1Test");
        put("check_in_bag_player2_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer2Test");
        put("check_in_bag_player3_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer3Test");
        put("check_in_bag_player4_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer4Test");
        put("check_in_ekyc_id", "tests.test_scripts.api.booking.checkin.CheckInEkycTest");
        put("undo_check_in_bag_id", "tests.test_scripts.api.booking.checkin.UndoCheckInBagTest");

        put("create_flight_id", "tests.test_scripts.api.go_course_information.CreateFlightTest");
        put("add_bag_to_flight_id", "tests.test_scripts.api.go_course_information.AddBagToFlightTest");

        put("out_all_flight_id", "tests.test_scripts.api.go_course_information.OutAllFlightTest");
        put("simple_out_flight_player1_id", "tests.test_scripts.api.go_course_information.SimpleOutFlightPlayer1Test");
        put("simple_out_flight_player2_id", "tests.test_scripts.api.go_course_information.SimpleOutFlightPlayer2Test");
        put("simple_out_flight_player3_id", "tests.test_scripts.api.go_course_information.SimpleOutFlightPlayer3Test");
        put("simple_out_flight_player4_id", "tests.test_scripts.api.go_course_information.SimpleOutFlightPlayer4Test");
        put("undo_out_flight_player1_id", "tests.test_scripts.api.go_course_information.UndoOutFlightPlayer1Test");
        put("undo_out_flight_player2_id", "tests.test_scripts.api.go_course_information.UndoOutFlightPlayer2Test");
        put("undo_out_flight_player3_id", "tests.test_scripts.api.go_course_information.UndoOutFlightPlayer3Test");
        put("undo_out_flight_player4_id", "tests.test_scripts.api.go_course_information.UndoOutFlightPlayer4Test");

        put("delete_attach_flight_player1_id", "tests.test_scripts.api.go_course_information.DeleteAttachFlightPlayer1Test");
        put("delete_attach_flight_player2_id", "tests.test_scripts.api.go_course_information.DeleteAttachFlightPlayer2Test");
        put("delete_attach_flight_player3_id", "tests.test_scripts.api.go_course_information.DeleteAttachFlightPlayer3Test");
        put("delete_attach_flight_player4_id", "tests.test_scripts.api.go_course_information.DeleteAttachFlightPlayer4Test");

        put("add_round_player1_id", "tests.test_scripts.api.go_course_information.AddRoundPlayer1Test");

        put("restaurant_create_bill_player1_id", "tests.test_scripts.api.pos.restaurant.RestaurantCreateBillPlayer1Test");
        put("restaurant_get_list_menu_id", "tests.test_scripts.api.pos.restaurant.RestaurantGetListMenuTest");

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

                "check_in_bag_player1_id",
                "check_in_bag_player2_id",
                "check_in_bag_player3_id",
                "check_in_bag_player4_id",
                "check_in_ekyc_id",

                "create_flight_id",
                "add_bag_to_flight_id",
                "out_all_flight_id",
                "simple_out_flight_player1_id",
                "simple_out_flight_player2_id",
                "simple_out_flight_player3_id",
                "simple_out_flight_player4_id",
                "undo_out_flight_player1_id",
                "undo_out_flight_player2_id",
                "undo_out_flight_player3_id",
                "undo_out_flight_player4_id",
                "delete_attach_flight_player1_id",
                "delete_attach_flight_player2_id",
                "delete_attach_flight_player3_id",
                "delete_attach_flight_player4_id",
                "add_round_player1_id",

                "restaurant_create_bill_player1_id",
                "restaurant_get_list_menu_id"
        );
    }
}
