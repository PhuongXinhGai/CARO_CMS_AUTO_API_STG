package framework.core;

import java.util.*;

public class ApiRegistry {
    /** Bản đồ ánh xạ giữa tên cột trong Excel và class thực thi test */
    private static final Map<String, String> MAP = new HashMap<>() {{

        // ===== user.account =====
        put("login_case_id", "tests.test_scripts.api.user.account.LoginTest");

        // ===== booking.create_booking =====
        put("quote_fee_case_id", "tests.test_scripts.api.booking.create_booking.QuoteFeeTest");
        put("create_booking_1_player_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatch1PlayerTest");
        put("create_booking_Voucher_1_Player_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatchVoucher1PlayerTest");
        put("create_booking_4_player_case_id", "tests.test_scripts.api.booking.create_booking.CreateBookingBatch4PlayerTest");

        put("get_booking_list_case_id", "tests.test_scripts.api.booking.create_booking.GetBookingListSelectTest");
        put("get_booking_price_case_id", "tests.test_scripts.api.booking.create_booking.GetBookingPriceTest");
        put("get_booking_price_player1_id", "tests.test_scripts.api.booking.create_booking.GetBookingPricePlayer1Test");
        put("get_booking_price_player2_id", "tests.test_scripts.api.booking.create_booking.GetBookingPricePlayer2Test");
        put("get_booking_price_player3_id", "tests.test_scripts.api.booking.create_booking.GetBookingPricePlayer3Test");
        put("get_booking_price_player4_id", "tests.test_scripts.api.booking.create_booking.GetBookingPricePlayer4Test");

        put("abc_case_id", "tests.test_scripts.api.booking/create_booking.ABCTest");
        put("def_id", "tests.test_scripts.api.booking/create_booking.defTest");
        // ===== booking.edit_booking =====
        put("edit_booking_at_tee_time_player1_id", "tests.test_scripts.api.booking.edit_booking.EditBookingAtTeeTimePlayer1Test");
        put("edit_booking_at_tee_time_player2_id", "tests.test_scripts.api.booking.edit_booking.EditBookingAtTeeTimePlayer2Test");
        put("edit_booking_at_tee_time_player3_id", "tests.test_scripts.api.booking.edit_booking.EditBookingAtTeeTimePlayer3Test");
        put("edit_booking_at_tee_time_player4_id", "tests.test_scripts.api.booking.edit_booking.EditBookingAtTeeTimePlayer4Test");

        put("edit_booking_1_player_id", "tests.test_scripts.api.booking.edit_booking.EditBooking1PlayerTest");

        // ===== booking.checkin =====
        put("check_in_bag_player1_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer1Test");
        put("check_in_bag_player2_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer2Test");
        put("check_in_bag_player3_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer3Test");
        put("check_in_bag_player4_id", "tests.test_scripts.api.booking.checkin.CheckInBagPlayer4Test");

        put("Check_In_Ekyc_Player1_id", "tests.test_scripts.api.booking.checkin.CheckInEkycPlayer1Test");
        put("Check_In_Ekyc_Player2_id", "tests.test_scripts.api.booking.checkin.CheckInEkycPlayer2Test");
        put("Check_In_Ekyc_Player3_id", "tests.test_scripts.api.booking.checkin.CheckInEkycPlayer3Test");
        put("Check_In_Ekyc_Player4_id", "tests.test_scripts.api.booking.checkin.CheckInEkycPlayer4Test");

        put("undo_check_in_bag_id", "tests.test_scripts.api.booking.checkin.UndoCheckInBagTest");

        // ===== go_course_information =====
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

        // ===== pos.restaurant =====
        put("restaurant_create_bill_player1_id", "tests.test_scripts.api.pos.restaurant.RestaurantCreateBillPlayer1Test");
        put("restaurant_create_bill_player2_id", "tests.test_scripts.api.pos.restaurant.RestaurantCreateBillPlayer2Test");
        put("restaurant_create_bill_player3_id", "tests.test_scripts.api.pos.restaurant.RestaurantCreateBillPlayer3Test");
        put("restaurant_create_bill_player4_id", "tests.test_scripts.api.pos.restaurant.RestaurantCreateBillPlayer4Test");

        put("restaurant_get_list_menu_id", "tests.test_scripts.api.pos.restaurant.RestaurantGetListMenuTest");

        put("restaurant_add_item_to_bill_player1_id", "tests.test_scripts.api.pos.restaurant.RestaurantAddItemToBillPlayer1Test");
        put("restaurant_add_item_to_bill_player2_id", "tests.test_scripts.api.pos.restaurant.RestaurantAddItemToBillPlayer2Test");
        put("restaurant_add_item_to_bill_player3_id", "tests.test_scripts.api.pos.restaurant.RestaurantAddItemToBillPlayer3Test");
        put("restaurant_add_item_to_bill_player4_id", "tests.test_scripts.api.pos.restaurant.RestaurantAddItemToBillPlayer4Test");

        // ===== pos.kiosk =====
        put("kiosk_create_bill_player1_id", "tests.test_scripts.api.pos.kiosk.KioskCreateBillPlayer1Test");
        put("kiosk_create_bill_player2_id", "tests.test_scripts.api.pos.kiosk.KioskCreateBillPlayer2Test");
        put("kiosk_create_bill_player3_id", "tests.test_scripts.api.pos.kiosk.KioskCreateBillPlayer3Test");
        put("kiosk_create_bill_player4_id", "tests.test_scripts.api.pos.kiosk.KioskCreateBillPlayer4Test");

        put("kiosk_get_list_menu_id", "tests.test_scripts.api.pos.kiosk.KioskGetListMenuTest");

        put("kiosk_add_item_to_bill_player1_id", "tests.test_scripts.api.pos.kiosk.KioskAddItemToBillPlayer1Test");
        put("kiosk_add_item_to_bill_player2_id", "tests.test_scripts.api.pos.kiosk.KioskAddItemToBillPlayer2Test");
        put("kiosk_add_item_to_bill_player3_id", "tests.test_scripts.api.pos.kiosk.KioskAddItemToBillPlayer3Test");
        put("kiosk_add_item_to_bill_player4_id", "tests.test_scripts.api.pos.kiosk.KioskAddItemToBillPlayer4Test");

        // ===== pos.mini_bar =====
        put("mini_bar_create_bill_player1_id", "tests.test_scripts.api.pos.mini_bar.MiniBarCreateBillPlayer1Test");
        put("mini_bar_create_bill_player2_id", "tests.test_scripts.api.pos.mini_bar.MiniBarCreateBillPlayer2Test");
        put("mini_bar_create_bill_player3_id", "tests.test_scripts.api.pos.mini_bar.MiniBarCreateBillPlayer3Test");
        put("mini_bar_create_bill_player4_id", "tests.test_scripts.api.pos.mini_bar.MiniBarCreateBillPlayer4Test");

        put("mini_bar_get_list_menu_id", "tests.test_scripts.api.pos.mini_bar.MiniBarGetListMenuTest");

        put("mini_bar_add_item_to_bill_player1_id", "tests.test_scripts.api.pos.mini_bar.MiniBarAddItemToBillPlayer1Test");
        put("mini_bar_add_item_to_bill_player2_id", "tests.test_scripts.api.pos.mini_bar.MiniBarAddItemToBillPlayer2Test");
        put("mini_bar_add_item_to_bill_player3_id", "tests.test_scripts.api.pos.mini_bar.MiniBarAddItemToBillPlayer3Test");
        put("mini_bar_add_item_to_bill_player4_id", "tests.test_scripts.api.pos.mini_bar.MiniBarAddItemToBillPlayer4Test");

        // ===== pos.driving =====
        put("driving_create_bill_player1_id", "tests.test_scripts.api.pos.driving.DrivingCreateBillPlayer1Test");
        put("driving_create_bill_player2_id", "tests.test_scripts.api.pos.driving.DrivingCreateBillPlayer2Test");
        put("driving_create_bill_player3_id", "tests.test_scripts.api.pos.driving.DrivingCreateBillPlayer3Test");
        put("driving_create_bill_player4_id", "tests.test_scripts.api.pos.driving.DrivingCreateBillPlayer4Test");

        put("driving_get_list_menu_id", "tests.test_scripts.api.pos.driving.DrivingGetListMenuTest");

        put("driving_add_item_to_bill_player1_id", "tests.test_scripts.api.pos.driving.DrivingAddItemToBillPlayer1Test");
        put("driving_add_item_to_bill_player2_id", "tests.test_scripts.api.pos.driving.DrivingAddItemToBillPlayer2Test");
        put("driving_add_item_to_bill_player3_id", "tests.test_scripts.api.pos.driving.DrivingAddItemToBillPlayer3Test");
        put("driving_add_item_to_bill_player4_id", "tests.test_scripts.api.pos.driving.DrivingAddItemToBillPlayer4Test");

        // ===== pos.rental =====
        put("rental_create_bill_player1_id", "tests.test_scripts.api.pos.rental.RentalCreateBillPlayer1Test");
        put("rental_create_bill_player2_id", "tests.test_scripts.api.pos.rental.RentalCreateBillPlayer2Test");
        put("rental_create_bill_player3_id", "tests.test_scripts.api.pos.rental.RentalCreateBillPlayer3Test");
        put("rental_create_bill_player4_id", "tests.test_scripts.api.pos.rental.RentalCreateBillPlayer4Test");

        put("rental_get_list_menu_id", "tests.test_scripts.api.pos.rental.RentalGetListMenuTest");

        put("rental_add_item_to_bill_player1_id", "tests.test_scripts.api.pos.rental.RentalAddItemToBillPlayer1Test");
        put("rental_add_item_to_bill_player2_id", "tests.test_scripts.api.pos.rental.RentalAddItemToBillPlayer2Test");
        put("rental_add_item_to_bill_player3_id", "tests.test_scripts.api.pos.rental.RentalAddItemToBillPlayer3Test");
        put("rental_add_item_to_bill_player4_id", "tests.test_scripts.api.pos.rental.RentalAddItemToBillPlayer4Test");

        // ===== pos.proshop =====
        put("proshop_create_bill_player1_id", "tests.test_scripts.api.pos.proshop.ProshopCreateBillPlayer1Test");
        put("proshop_create_bill_player2_id", "tests.test_scripts.api.pos.proshop.ProshopCreateBillPlayer2Test");
        put("proshop_create_bill_player3_id", "tests.test_scripts.api.pos.proshop.ProshopCreateBillPlayer3Test");
        put("proshop_create_bill_player4_id", "tests.test_scripts.api.pos.proshop.ProshopCreateBillPlayer4Test");

        put("proshop_get_list_menu_id", "tests.test_scripts.api.pos.proshop.ProshopGetListMenuTest");

        put("proshop_add_item_to_bill_player1_id", "tests.test_scripts.api.pos.proshop.ProshopAddItemToBillPlayer1Test");
        put("proshop_add_item_to_bill_player2_id", "tests.test_scripts.api.pos.proshop.ProshopAddItemToBillPlayer2Test");
        put("proshop_add_item_to_bill_player3_id", "tests.test_scripts.api.pos.proshop.ProshopAddItemToBillPlayer3Test");
        put("proshop_add_item_to_bill_player4_id", "tests.test_scripts.api.pos.proshop.ProshopAddItemToBillPlayer4Test");

        // ===== cico =====
        put("booking_by_bag_player1_id", "tests.test_scripts.api.cico.GetBookingByBagPlayer1Test");
        put("booking_by_bag_player2_id", "tests.test_scripts.api.cico.GetBookingByBagPlayer2Test");
        put("booking_by_bag_player3_id", "tests.test_scripts.api.cico.GetBookingByBagPlayer3Test");
        put("booking_by_bag_player4_id", "tests.test_scripts.api.cico.GetBookingByBagPlayer4Test");

        put("add_sub_bag_id", "tests.test_scripts.api.cico.AddSubBagTest");
        put("detail_agency_pay_id", "tests.test_scripts.api.cico.DetailAgencyPayTest");

        put("update_agency_pay_player1_id", "tests.test_scripts.api.cico.UpdateAgencyPayPlayer1Test");
        put("update_agency_pay_player2_id", "tests.test_scripts.api.cico.UpdateAgencyPayPlayer2Test");
        put("update_agency_pay_player3_id", "tests.test_scripts.api.cico.UpdateAgencyPayPlayer3Test");
        put("update_agency_pay_player4_id", "tests.test_scripts.api.cico.UpdateAgencyPayPlayer4Test");

        put("fee_of_bag_player1_id", "tests.test_scripts.api.cico.GetFeeOfBagPlayer1Test");
        put("fee_of_bag_player2_id", "tests.test_scripts.api.cico.GetFeeOfBagPlayer2Test");
        put("fee_of_bag_player3_id", "tests.test_scripts.api.cico.GetFeeOfBagPlayer3Test");
        put("fee_of_bag_player4_id", "tests.test_scripts.api.cico.GetFeeOfBagPlayer4Test");

        put("fee_of_bag_bill_player1_id", "tests.test_scripts.api.cico.GetFeeOfBagBillPlayer1Test");
        put("fee_of_bag_bill_player2_id", "tests.test_scripts.api.cico.GetFeeOfBagBillPlayer2Test");
        put("fee_of_bag_bill_player3_id", "tests.test_scripts.api.cico.GetFeeOfBagBillPlayer3Test");
        put("fee_of_bag_bill_player4_id", "tests.test_scripts.api.cico.GetFeeOfBagBillPlayer4Test");

        put("input_single_payment_id", "tests.test_scripts.api.cico.InputSinglePaymentTest");
        put("single_payment_list_id", "tests.test_scripts.api.cico.SinglePaymentListTest");

        put("check_out_bag_id", "tests.test_scripts.api.cico.CheckOutBagTest");
        put("check_out_group_id", "tests.test_scripts.api.cico.CheckOutGroupTest");

        // ===== invoice =====
        put("e_invoice_id", "tests.test_scripts.api.invoice.EInvoiceTest");

        put("e_invoice_payment_id_player1_id", "tests.test_scripts.api.invoice.EInvoicePaymentIdPlayer1Test");
        put("e_invoice_payment_id_player2_id", "tests.test_scripts.api.invoice.EInvoicePaymentIdPlayer2Test");
        put("e_invoice_payment_id_player3_id", "tests.test_scripts.api.invoice.EInvoicePaymentIdPlayer3Test");
        put("e_invoice_payment_id_player4_id", "tests.test_scripts.api.invoice.EInvoicePaymentIdPlayer4Test");

        put("invoice_detail_cost_id", "tests.test_scripts.api.invoice.InvoiceDetailCostTest");
        put("invoice_detail_agency_paid_id", "tests.test_scripts.api.invoice.InvoiceDetailAgencyPaidTest");

        // ===== report =====
        put("revenue_detail_report_id", "tests.test_scripts.api.report.RevenueDetailReportTest");
        put("revenue_detail_report_player1_id", "tests.test_scripts.api.report.RevenueDetailReportPlayer1Test");
        put("revenue_detail_report_player2_id", "tests.test_scripts.api.report.RevenueDetailReportPlayer2Test");
        put("revenue_detail_report_player3_id", "tests.test_scripts.api.report.RevenueDetailReportPlayer3Test");
        put("revenue_detail_report_player4_id", "tests.test_scripts.api.report.RevenueDetailReportPlayer4Test");


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

                "create_booking_1_player_case_id",
                "create_booking_Voucher_1_Player_case_id",
                "create_booking_4_player_case_id",

                "edit_booking_at_tee_time_player1_id",
                "edit_booking_at_tee_time_player2_id",
                "edit_booking_at_tee_time_player3_id",
                "edit_booking_at_tee_time_player4_id",
                "edit_booking_1_player_id",

                "get_booking_list_case_id",
                "get_booking_price_case_id",
                "get_booking_price_player1_id",
                "get_booking_price_player2_id",
                "get_booking_price_player3_id",
                "get_booking_price_player4_id",

                "check_in_bag_player1_id",
                "check_in_bag_player2_id",
                "check_in_bag_player3_id",
                "check_in_bag_player4_id",
                "Check_In_Ekyc_Player1_id",
                "Check_In_Ekyc_Player2_id",
                "Check_In_Ekyc_Player3_id",
                "Check_In_Ekyc_Player4_id",

                "undo_check_in_bag_id",
                "check_in_bag_player1_id",
                "undo_check_in_bag_id",

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
                "restaurant_create_bill_player2_id",
                "restaurant_create_bill_player3_id",
                "restaurant_create_bill_player4_id",
                "restaurant_get_list_menu_id",
                "restaurant_add_item_to_bill_player1_id",
                "restaurant_add_item_to_bill_player2_id",
                "restaurant_add_item_to_bill_player3_id",
                "restaurant_add_item_to_bill_player4_id",

                "kiosk_create_bill_player1_id",
                "kiosk_create_bill_player2_id",
                "kiosk_create_bill_player3_id",
                "kiosk_create_bill_player4_id",
                "kiosk_get_list_menu_id",
                "kiosk_add_item_to_bill_player1_id",
                "kiosk_add_item_to_bill_player2_id",
                "kiosk_add_item_to_bill_player3_id",
                "kiosk_add_item_to_bill_player4_id",

                "mini_bar_create_bill_player1_id",
                "mini_bar_create_bill_player2_id",
                "mini_bar_create_bill_player3_id",
                "mini_bar_create_bill_player4_id",
                "mini_bar_get_list_menu_id",
                "mini_bar_add_item_to_bill_player1_id",
                "mini_bar_add_item_to_bill_player2_id",
                "mini_bar_add_item_to_bill_player3_id",
                "mini_bar_add_item_to_bill_player4_id",

                "driving_create_bill_player1_id",
                "driving_create_bill_player2_id",
                "driving_create_bill_player3_id",
                "driving_create_bill_player4_id",
                "driving_get_list_menu_id",
                "driving_add_item_to_bill_player1_id",
                "driving_add_item_to_bill_player2_id",
                "driving_add_item_to_bill_player3_id",
                "driving_add_item_to_bill_player4_id",

                "rental_create_bill_player1_id",
                "rental_create_bill_player2_id",
                "rental_create_bill_player3_id",
                "rental_create_bill_player4_id",
                "rental_get_list_menu_id",
                "rental_add_item_to_bill_player1_id",
                "rental_add_item_to_bill_player2_id",
                "rental_add_item_to_bill_player3_id",
                "rental_add_item_to_bill_player4_id",

                "proshop_create_bill_player1_id",
                "proshop_create_bill_player2_id",
                "proshop_create_bill_player3_id",
                "proshop_create_bill_player4_id",
                "proshop_get_list_menu_id",
                "proshop_add_item_to_bill_player1_id",
                "proshop_add_item_to_bill_player2_id",
                "proshop_add_item_to_bill_player3_id",
                "proshop_add_item_to_bill_player4_id",

                "booking_by_bag_player1_id",
                "booking_by_bag_player2_id",
                "booking_by_bag_player3_id",
                "booking_by_bag_player4_id",

                "add_sub_bag_id",
                "detail_agency_pay_id",
                "update_agency_pay_player1_id",
                "update_agency_pay_player2_id",
                "update_agency_pay_player3_id",
                "update_agency_pay_player4_id",

                "fee_of_bag_player1_id",
                "fee_of_bag_player2_id",
                "fee_of_bag_player3_id",
                "fee_of_bag_player3_id",
                "fee_of_bag_bill_player1_id",
                "fee_of_bag_bill_player2_id",
                "fee_of_bag_bill_player3_id",
                "fee_of_bag_bill_player4_id",

                "input_single_payment_id",
                "input_single_payment_id",
                "input_single_payment_id",
                "input_single_payment_id",
                "single_payment_list_id",
                "single_payment_list_id",
                "single_payment_list_id",
                "single_payment_list_id",

                "check_out_bag_id",
                "check_out_bag_id",
                "check_out_bag_id",
                "check_out_bag_id",
                "check_out_group_id",

                "e_invoice_id",
                "e_invoice_payment_id_player1_id",
                "e_invoice_payment_id_player2_id",
                "e_invoice_payment_id_player3_id",
                "e_invoice_payment_id_player4_id",
                "invoice_detail_cost_id",
                "invoice_detail_cost_id",
                "invoice_detail_cost_id",
                "invoice_detail_cost_id",
                "invoice_detail_agency_paid_id",

                "revenue_detail_report_id",
                "revenue_detail_report_player1_id",
                "revenue_detail_report_player2_id",
                "revenue_detail_report_player3_id",
                "revenue_detail_report_player4_id"

        );
    }
}
