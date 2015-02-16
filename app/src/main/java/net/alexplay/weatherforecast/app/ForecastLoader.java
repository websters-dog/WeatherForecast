package net.alexplay.weatherforecast.app;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import org.json.JSONException;

import java.io.IOException;
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
                String result = new String(HttpLoader.load(String.format(REQUEST_URL, city.latitude, city.longitude)));
                for (Forecast tmpForecast : HttpLoader.processForecastString(result)){
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


}
