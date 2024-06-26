package Data;

import Concurrency.ThreadSafeList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Accommodation implements Serializable {
    private String roomName;
    private int noOfPersons;
    private String area;
    private int stars;
    private int noOfReviews;
    private String localImagePath;
    private ThreadSafeList<Date> bookedDates = new ThreadSafeList<>();

    public Accommodation(String roomName, int noOfPersons, String area, int stars, int noOfReviews, String localImagePath, List<Date> initialDates) {
        this.roomName = roomName;
        this.noOfPersons = noOfPersons;
        this.area = area;
        this.stars = stars;
        this.noOfReviews = noOfReviews;
        this.localImagePath = localImagePath;
        bookedDates.addAll(initialDates);
    }

    public Accommodation() {}

    public static List<Accommodation> readAccommodationsFromJson(String jsonPath) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(jsonPath)));
        JSONArray jsonArray = new JSONArray(content);

        List<Accommodation> accommodations = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);

            String roomName = jsonObj.getString("roomName");
            int noOfPersons = jsonObj.getInt("noOfPersons");
            String area = jsonObj.getString("area");
            int stars = jsonObj.getInt("stars");
            int noOfReviews = jsonObj.getInt("noOfReviews");
            String localImagePath = jsonObj.getString("roomImage");

            List<Date> initialDates = new ArrayList<>(); // Assume dates are needed, add parsing if JSON contains date info.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            // If JSON has dates, you'd parse them like:
            // JSONArray datesJson = jsonObj.getJSONArray("dates");
            // for (int j = 0; j < datesJson.length(); j++) {
            //     initialDates.add(sdf.parse(datesJson.getString(j)));
            // }

            accommodations.add(new Accommodation(roomName, noOfPersons, area, stars, noOfReviews, localImagePath, initialDates));
        }
        return accommodations;
    }

    public synchronized boolean book(Date startDate, Date endDate) {
        if (isAvailable(startDate, endDate)) {
            bookedDates.addAll(getDatesBetween(startDate, endDate));
            return true;
        }
        return false;
    }

    public synchronized boolean isAvailable(Date startDate, Date endDate) {
        List<Date> datesToCheck = getDatesBetween(startDate, endDate);
        for (Date date : datesToCheck) {
            if (bookedDates.getAll().contains(date)) {
                return false;
            }
        }
        return true;
    }

    private List<Date> getDatesBetween(Date startDate, Date endDate) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }
        return dates;
    }

    public String getRoomName() { return roomName; }
    public String getLocalImagePath() { return localImagePath; }
    public int getNoOfPersons() { return noOfPersons; }
    public String getArea() { return area; }
    public int getStars() { return stars; }
    public int getNoOfReviews() { return noOfReviews; }
    public String getRoomImage() { return localImagePath; }
    public List<Date> getBookedDates() { return bookedDates.getAll(); }
}
