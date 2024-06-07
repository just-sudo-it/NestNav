package Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AddAccommodationRequest extends Request {
    private Accommodation accommodation;
    public AddAccommodationRequest(Accommodation accommodation) {
        super(RequestType.ADD);
        this.accommodation = accommodation;
    }
    public Accommodation getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(Accommodation accommodation) {
        this.accommodation = accommodation;
    }
}
