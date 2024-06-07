package Data;

import java.util.Date;

public class BookingRequest extends Request {
    private String roomName;
    private Date endDate;
    private Date startDate;
    public BookingRequest(String roomName, Date startDate, Date endDate) {
        super(RequestType.BOOK);
        this.roomName = roomName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public String getRoomName() {
        return roomName;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}