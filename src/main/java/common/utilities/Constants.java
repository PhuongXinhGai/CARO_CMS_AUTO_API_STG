package common.utilities;

public class Constants {
// user
    public static final String LOGIN_ENDPOINT = "/golf-cms/api/user/login-plain";

// Create Booking
    public static final String BOOKING_PRICE_ENDPOINT = "/golf-cms/api/booking/booking-price";
    public static final String QUOTE_FEE_ENDPOINT = "/golf-cms/api/booking/quote-fee";
    public static final String BOOKING_LIST_SELECT_ENDPOINT = "/golf-cms/api/booking/list/select";

    public static final String CREATE_BOOKING_BATCH_ENDPOINT = "/golf-cms/api/booking/batch";
    public static final String BOOKING_AT_TEE_TIME_ENDPOINT = "/golf-cms/api/booking";
    public static final String  REPORT_BOOKING_STATISTIC_ENDPOINT = "/golf-cms/api/report/booking-statistic";
// Edit booking
    public static final String  BOOKING_UPDATE_ENDPOINT = "/golf-cms/api/booking/update";
    public static final String  BOOKING_UPDATE_AT_TEE_TIME_ENDPOINT = "/golf-cms/api/booking/";
// Check in / undo check in
    public static final String CHECKIN_ENDPOINT = "/golf-cms/api/booking/check-in";
    public static final String CHECKIN_EKYC_ENDPOINT = "/golf-cms/ekyc/v1/member-card/check-in";
    public static final String UNDO_CHECKIN_ENDPOINT = "/golf-cms/api/booking/undo-check-in";
// CICO - check out
    public static final String CHECKOUT_BAG_ENDPOINT = "/golf-cms/api/booking/checkout";
    public static final String CHECKOUT_GROUP_ENDPOINT = "/golf-cms/api/booking/checkout-group";
// CICO - bag
    public static final String GET_BOOKING_BY_BAG_ENDPOINT = "/golf-cms/api/booking/by-bag";
    public static final String GET_FEE_OF_BAG_PLAYER_ENDPOINT = "/golf-cms/api/booking/fee-of-bag";
    public static final String GET_FEE_OF_BAG_BILL_ENDPOINT = "/golf-cms/api/booking/fee-of-bag-bill";
//  CICO - payment
    public static final String INPUT_SINGLE_PAYMENT_ENDPOINT = "/golf-cms/api/payment/single-payment/add-list";
    public static final String SINGLE_PAYMENT_LIST_ENDPOINT = "/golf-cms/api/payment/single-payment/list/item";
//  CICO - Main - sub bag
    public static final String ADD_SUB_BAG_ENDPOINT = "/golf-cms/api/booking/sub-bag/add";
//  CICO - Agency pay
    public static final String DETAIL_AGENCY_PAY_ENDPOINT = "/golf-cms/api/booking/get-detai-agency-pay";
    public static final String UPDATE_AGENCY_PAY_ENDPOINT = "/golf-cms/api/booking/";

//  GO - course information
    public static final String CREATE_FLIGHT_ENDPOINT = "/golf-cms/api/course-operating/flight/create";
    public static final String ADD_BAG_TO_FLIGHT_ENDPOINT = "/golf-cms/api/course-operating/add-bag-to-flight";
    public static final String OUT_ALL_FLIGHT_ENDPOINT = "/golf-cms/api/course-operating/caddie/out-all-in-flight";
    public static final String SIMPLE_OUT_FLIGHT_ENDPOINT = "/golf-cms/api/course-operating/simple-out-flight";
    public static final String UNDO_OUT_FLIGHT_ENDPOINT = "/golf-cms/api/course-operating/undo-timeout";
    public static final String DELETE_ATTACH_FLIGHT_ENDPOINT = "/golf-cms/api/course-operating/caddie/delete-attach";
    public static final String ADD_ROUND_ENDPOINT = "/golf-cms/api/booking/rounds/add";

// POS
    public static final String SERVICE_CART_ADD_ENDPOINT = "/golf-cms/api/service-cart/add";
    public static final String RENTAL_GOLF_CLUB_ENDPOINT = "/golf-cms/api/rental/golf-club";
    public static final String KIOSK_INVENTORY_LIST_ENDPOINT = "/golf-cms/api/kiosk-inventory/list";

// Invoice
    public static final String EINVOICE_PAYMENT_ID_ENDPOINT = "/golf-cms/api/e-invoice/payment/list";
    public static final String INVOICE_DETAIL_COST_ENDPOINT = "/golf-cms/api/e-invoice/invoice/detail-cost";
    public static final String INVOICE_DETAIL_AGENCY_PAID_ENDPOINT = "/golf-cms/api/e-invoice/invoice/detail-agency-paid";

// Report
    public static final String REVENUE_DETAIL_REPORT_ENDPOINT = "/golf-cms/api/report/revenue/report-booking-detail";

// DB
    public static final String DB_BOOKING_LIST_ENDPOINT = "/golf-cms/api/db/iffcqfyqvhkvepzlbsgazrgsnphnaw/qujxgskhoxeayvhj";


    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String APPLICATION_JSON = "application/json";


}
