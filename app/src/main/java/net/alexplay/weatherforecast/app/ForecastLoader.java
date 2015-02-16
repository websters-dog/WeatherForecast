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

    private final static String REQUEST_URL = "http://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&units=metric";
    private static final int MESSAGE_LOAD = 1;

    private static  final String RAIN_URL = "https://ssl.gstatic.com/onebox/weather/256/rain.png";
    private static  final String SUNNY_URL = "https://ssl.gstatic.com/onebox/weather/256/sunny.png";
    private static  final String CLOUDY_URL = "https://ssl.gstatic.com/onebox/weather/256/cloudy.png";

    private Handler responseHandler;
    private Handler loadHandler;
    private LoadListener<T> loadListener;
    private Map<T, ForecastCity> forecastCities = Collections.synchronizedMap(new HashMap<T, ForecastCity>());
    private Map<T, Long> forecastClocks = Collections.synchronizedMap(new HashMap<T, Long>());

    private ImageLoader imageLoader;

    private DatabaseWorker databaseWorker;

    public ForecastLoader(Handler responseHandler, DatabaseWorker databaseWorker) {
        super("ForecastLoader");
        this.responseHandler = responseHandler;
        this.databaseWorker = databaseWorker;
        imageLoader = new ImageLoader();
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
        forecastCities.put(t, city);
        forecastClocks.put(t, time);
        loadHandler.obtainMessage(MESSAGE_LOAD, t).sendToTarget();
    }

    private void handleRequest(final T t){
        Forecast tmpResultForecast;
        final Long forecastClock = forecastClocks.get(t);
        ForecastCity city = forecastCities.get(t);
        if(city == null) return;

        tmpResultForecast = databaseWorker.loadForecast(city.id, forecastClock);
        if (tmpResultForecast == null) {
            Log.d("LOADER", "load online: city=" + city.id + "; t=" + forecastClock);
            try {
                String result = new String(HttpLoader.load(String.format(REQUEST_URL, city.latitude, city.longitude)));
                for (Forecast tmpForecast : HttpLoader.processForecastString(result)){
                    if(tmpForecast.time == forecastClock){
                        tmpResultForecast = tmpForecast;
                        break;
                    }
                }
                if (tmpResultForecast != null) {
                    Log.d("LOADER", "save online: city=" + city.id + "; t=" + tmpResultForecast.time);
                    databaseWorker.saveForecast(tmpResultForecast);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("LOADER", "load from db");
        }

        final Forecast resultForecast = tmpResultForecast;
        if(resultForecast != null){
            String imageUrl;
            if(resultForecast.iconCode.contains("01") || resultForecast.iconCode.contains("02")){
                imageUrl = SUNNY_URL;
            } else if(resultForecast.iconCode.contains("03") || resultForecast.iconCode.contains("04")){
                imageUrl = CLOUDY_URL;
            } else {
                imageUrl = RAIN_URL;
            }
            imageLoader.loadImage(imageUrl, new ImageLoader.LoadListener() {
                @Override
                public void onLoad(final Drawable drawable) {
                    responseHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (forecastClocks.get(t) == forecastClock) {
                                forecastCities.remove(t);
                                forecastClocks.remove(t);
                                loadListener.onLoad(t, resultForecast, drawable);
                            }
                        }
                    });
                }
            });
        } else {
            responseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (forecastClocks.get(t) == forecastClock) {
                        forecastCities.remove(t);
                        forecastClocks.remove(t);
                        loadListener.onLoad(t, resultForecast, null);
                    }
                }
            });
        }



    }

    public void setLoadListener(LoadListener<T> loadListener) {
        this.loadListener = loadListener;
    }

    public interface LoadListener<T>{
        public void onLoad(T t, Forecast forecast, Drawable forecastImage);
    }


}
