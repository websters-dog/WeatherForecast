package net.alexplay.weatherforecast.app;

public class Forecast {

    public ForecastCity city;

    public long time;

    public float tempMin;
    public float tempMax;

    public float pressure;

    public float humidity;

    public float windSpeed;
    public float windAngle;

    public float cloudsPercent;
    public String iconCode;

    public Forecast(ForecastCity city, long time, float tempMin, float tempMax, float pressure, float humidity,
                    float windSpeed, float windAngle, float cloudsPercent, String iconCode) {
        this.city = city;
        this.time = time;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.pressure = pressure;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windAngle = windAngle;
        this.cloudsPercent = cloudsPercent;
        this.iconCode = iconCode;
    }

    @Override
    public String toString() {
        return "Forecast: " + "city=" + city.name + "; time=" + time + "; tempMin=" + tempMin + "; tempMax=" + tempMax
                + "; pressure=" + pressure + "; humidity=" + humidity + "; windSpeed=" + windSpeed + "; windAngle=" + windAngle
                + "; cloudsPercent" + cloudsPercent + "; iconCode=" + iconCode;
    }

    public static abstract class FeedEntry {

        public static final String TABLE = "FORECAST";
        public static final String COLUMN_TIME = "TIME";
        public static final String COLUMN_CITY = "CITY";
        public static final String COLUMN_TEMP_MIN = "TEMP_MIN";
        public static final String COLUMN_TEMP_MAX = "TEMP_MAX";
        public static final String COLUMN_PRESSURE = "PRESSURE";
        public static final String COLUMN_HUMIDITY = "HUMIDITY";
        public static final String COLUMN_WIND_SPEED = "WIND_SPEED";
        public static final String COLUMN_WIND_ANGLE = "WIND_ANGLE";
        public static final String COLUMN_CLOUDS = "CLOUDS";
        public static final String COLUMN_ICON_CODE = "ICON_CODE";

    }

    public static abstract class JsonEntry {

        public static final String OBJECTS_ARRAY = "list";
        public static final String TIME = "dt";

        public static final String OBJECT_MAIN = "main";
        public static final String TEMP_MIN = "temp_min";
        public static final String TEMP_MAX = "temp_max";
        public static final String PRESSURE = "pressure";
        public static final String HUMIDITY = "humidity";

        public static final String OBJECT_WIND = "wind";
        public static final String WIND_SPEED = "speed";
        public static final String WIND_ANGLE = "deg";

        public static final String OBJECT_CLOUDS = "clouds";
        public static final String CLOUDS = "all";

        public static final String OBJECTS_WEATHER_ARRAY = "weather";
        public static final String ICON_CODE = "icon";

    }

}
