package net.alexplay.weatherforecast.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONForecastReader {

    public static ArrayList<Forecast> getForecasts(String jsonString) throws JSONException {
        JSONObject jsonForecastMain = new JSONObject(jsonString);
        JSONObject jsonCity = jsonForecastMain.getJSONObject(ForecastCity.JsonEntry.OBJECT);

        ForecastCity city = new ForecastCity(
                jsonCity.getLong(ForecastCity.JsonEntry.ID),
                jsonCity.getString(ForecastCity.JsonEntry.NAME),
                (float) jsonCity.getJSONObject(ForecastCity.JsonEntry.OBJECT_CORDS).getDouble(ForecastCity.JsonEntry.LATITUDE),
                (float) jsonCity.getJSONObject(ForecastCity.JsonEntry.OBJECT_CORDS).getDouble(ForecastCity.JsonEntry.LONGITUDE)
        );
        DatabaseWorker.get().saveCity(city);

        ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
        JSONArray jsonForecastArray = jsonForecastMain.getJSONArray(Forecast.JsonEntry.OBJECTS_ARRAY);
        JSONObject jsonForecast;
        for(int i = 0; i < jsonForecastArray.length(); i++){
            jsonForecast = jsonForecastArray.getJSONObject(i);
            Forecast forecast = new Forecast(
                    city,
                    jsonForecast.getLong(Forecast.JsonEntry.TIME) * 1000,
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.TEMP_MIN),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.TEMP_MAX),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.PRESSURE),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.HUMIDITY),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_WIND).getDouble(Forecast.JsonEntry.WIND_SPEED),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_WIND).getDouble(Forecast.JsonEntry.WIND_ANGLE),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_CLOUDS).getDouble(Forecast.JsonEntry.CLOUDS),
                    jsonForecast.getJSONArray(Forecast.JsonEntry.OBJECTS_WEATHER_ARRAY).getJSONObject(0).getString(Forecast.JsonEntry.ICON_CODE)
            );
            forecasts.add(forecast);
        }
        return forecasts;
    }

}
