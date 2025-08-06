package common.models.booking_price;

public class Booking {
    private int id;
    private long created_at;
    private long updated_at;
    private String status;
    private String partner_uid;
    private String course_uid;
    private String booking_date;
    private long booking_date_unix;
    private String booking_uid;
    private String bill_code;
    private String booking_code;
    private int total_amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPartner_uid() {
        return partner_uid;
    }

    public void setPartner_uid(String partner_uid) {
        this.partner_uid = partner_uid;
    }

    public String getCourse_uid() {
        return course_uid;
    }

    public void setCourse_uid(String course_uid) {
        this.course_uid = course_uid;
    }

    public String getBooking_date() {
        return booking_date;
    }

    public void setBooking_date(String booking_date) {
        this.booking_date = booking_date;
    }

    public long getBooking_date_unix() {
        return booking_date_unix;
    }

    public void setBooking_date_unix(long booking_date_unix) {
        this.booking_date_unix = booking_date_unix;
    }

    public String getBooking_uid() {
        return booking_uid;
    }

    public void setBooking_uid(String booking_uid) {
        this.booking_uid = booking_uid;
    }

    public String getBill_code() {
        return bill_code;
    }

    public void setBill_code(String bill_code) {
        this.bill_code = bill_code;
    }

    public String getBooking_code() {
        return booking_code;
    }

    public void setBooking_code(String booking_code) {
        this.booking_code = booking_code;
    }

    public int getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(int total_amount) {
        this.total_amount = total_amount;
    }
}
