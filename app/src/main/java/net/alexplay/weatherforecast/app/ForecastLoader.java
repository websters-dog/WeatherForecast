package net.alexplay.weatherforecast.app;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ForecastLoader<T> extends HandlerThread {

    private final static String REQUEST_URL = "http://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric";
    private static final int MESSAGE_LOAD = 1;

    private Handler responseHandler;
    private Handler loadHandler;
    private LoadListener<T> loadListener;
    private Map<T, ForecastCity> loadRequests = Collections.synchronizedMap(new HashMap<T, ForecastCity>());
    private Map<T, Long> forecastClocks = Collections.synchronizedMap(new HashMap<T, Long>());
    private DatabaseWorker databaseWorker;

    public ForecastLoader(Handler responseHandler, DatabaseWorker databaseWorker) {
        super("ForecastLoader");
        this.responseHandler = responseHandler;
        this.databaseWorker = databaseWorker;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        loadHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_LOAD){
                    T t = (T) msg.obj;
                    handleRequest(t);
                }
            }
        };
    }

    public void loadForecast(T t, ForecastCity city, long time){
        loadRequests.put(t, city);
        forecastClocks.put(t, time);
        loadHandler.obtainMessage(MESSAGE_LOAD, t).sendToTarget();
    }

    private void handleRequest(final T t){
        final Forecast resultForecast;
        Forecast tmpResultForecast;
        final Long forecastClock = forecastClocks.get(t);
        ForecastCity city = loadRequests.get(t);
        Log.d("WEATHER_", "db=" + databaseWorker + "; city=" + city + "; clock=" + forecastClock);
        tmpResultForecast = databaseWorker.loadForecast(city.id, forecastClock);
        if (tmpResultForecast == null) {
            try {
                String result = StringLoader.load(String.format(REQUEST_URL, city.latitude, city.longitude));
                for (Forecast tmpForecast : processForecastString(result)){
                    if(tmpForecast.time == forecastClock){
                        tmpResultForecast = tmpForecast;
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        resultForecast = tmpResultForecast;
        responseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (forecastClocks.get(t) == forecastClock) {
                    loadRequests.remove(t);
                    forecastClocks.remove(t);
                    loadListener.onLoad(t, resultForecast, null);
                }
            }
        });
    }

    public void setLoadListener(LoadListener<T> loadListener) {
        this.loadListener = loadListener;
    }

    public interface LoadListener<T>{
        public void onLoad(T t, Forecast forecast, Drawable forecastImage);
    }

    public ArrayList<Forecast> processForecastString(String jsonString) throws JSONException {
        JSONObject jsonForecastMain = new JSONObject(jsonString);
        JSONObject jsonCity = jsonForecastMain.getJSONObject(ForecastCity.JsonEntry.OBJECT);

        ForecastCity city = new ForecastCity(
                jsonCity.getLong(ForecastCity.JsonEntry.ID),
                jsonCity.getString(ForecastCity.JsonEntry.NAME),
                (float) jsonCity.getJSONObject(ForecastCity.JsonEntry.OBJECT_CORDS).getDouble(ForecastCity.JsonEntry.LATITUDE),
                (float) jsonCity.getJSONObject(ForecastCity.JsonEntry.OBJECT_CORDS).getDouble(ForecastCity.JsonEntry.LONGITUDE)
        );
        Log.d("WEATHER_", city.toString());
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
            Log.d("WEATHER_", forecast.toString());
        }

        return forecasts;

    }
}
