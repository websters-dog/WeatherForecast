package net.alexplay.weatherforecast.app;

public class ForecastCity {

    public long id;
    public String name;
    public float latitude;
    public float longitude;

    public ForecastCity(long id, String name, float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "ForecastCity: id=" + id + "; name=" + name + "; lat=" + latitude + "; lon=" + longitude;
    }

    public static abstract class FeedEntry {

        public static final String TABLE = "CITIES";
        public static final String COLUMN_ID = "ID";
        public static final String COLUMN_NAME = "NAME";
        public static final String COLUMN_LATITUDE = "LATITUDE";
        public static final String COLUMN_LONGITUDE = "LONGITUDE";

    }

    public static abstract class JsonEntry {

        public static final String OBJECT = "city";
        public static final String ID = "id";
        public static final String NAME = "name";

        public static final String OBJECT_CORDS = "coord";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "lon";

    }

}
