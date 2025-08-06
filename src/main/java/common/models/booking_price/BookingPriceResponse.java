package common.models.booking_price;

import java.util.List;

public class BookingPriceResponse {
    private List<Booking> data;
    private int total_amount;

    public List<Booking> getData() {
        return data;
    }

    public void setData(List<Booking> data) {
        this.data = data;
    }

    public int getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(int total_amount) {
        this.total_amount = total_amount;
    }
}
