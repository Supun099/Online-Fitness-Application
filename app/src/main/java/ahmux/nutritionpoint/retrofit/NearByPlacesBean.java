package ahmux.nutritionpoint.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by dhanushka.
 */

public class NearByPlacesBean {

    private Object[] htmlAttributions;
    private Result[] results;
    private String status;

    public Object[] getHTMLAttributions() { return htmlAttributions; }
    public void setHTMLAttributions(Object[] value) { this.htmlAttributions = value; }

    public Result[] getResults() { return results; }
    public void setResults(Result[] value) { this.results = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }

    public static class Result {
        private Geometry geometry;
        private String name;
        private String place_id;
        private String vicinity;

        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry value) { this.geometry = value; }

        public String getName() { return name; }
        public void setName(String value) { this.name = value; }

        public String getPlaceId() { return place_id; }
        public void setPlaceId(String value) { this.place_id = value; }

        public String getVicinity() { return vicinity; }
        public void setVicinity(String value) { this.vicinity = value; }

        public static class Geometry {
            private Location location;

            public Location getLocation() { return location; }
            public void setLocation(Location value) { this.location = value; }

            public static class Location {
                private double lat;
                private double lng;

                public double getLat() { return lat; }
                public void setLat(double value) { this.lat = value; }

                public double getLng() { return lng; }
                public void setLng(double value) { this.lng = value; }
            }
        }

    }
}
