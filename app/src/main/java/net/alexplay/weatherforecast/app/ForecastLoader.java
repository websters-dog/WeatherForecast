package net.alexplay.weatherforecast.app;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.github.kevinsawicki.http.HttpRequest;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ForecastLoader<T> extends HandlerThread {

    private final static String REQUEST_URL = "http://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&units=metric";
    private static final int MESSAGE_LOAD = 1;


    private Handler responseHandler;
    private Handler loadHandler;
    private LoadListener<T> loadListener;
    private Map<T, ForecastCity> forecastCities = Collections.synchronizedMap(new HashMap<T, ForecastCity>());
    private Map<T, Long> forecastClocks = Collections.synchronizedMap(new HashMap<T, Long>());

    private DatabaseWorker databaseWorker;

    public ForecastLoader(Handler responseHandler, DatabaseWorker databaseWorker) {
        super("ForecastLoader");
        this.responseHandler = responseHandler;
        this.databaseWorker = databaseWorker;
    }

    @Override
    protected void onLooperPrepared() {
        synchronized (this) {
            loadHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == MESSAGE_LOAD){
                        T t = (T) msg.obj;
                        handleRequest(t);
                    }
                }
            };
            notifyAll();
        }
    }

    //stop all loadings and clear data for loading
    public void clearQueue(){
        if (loadHandler != null) {
            loadHandler.removeMessages(MESSAGE_LOAD);
        }
        forecastCities.clear();
        forecastClocks.clear();
    }

    //return count of loadings in queue
    public int getActiveLoadCount(){
        return forecastClocks.size();
    }

    //add new loading to queue
    public void loadForecast(T t, ForecastCity city, long time){
        forecastCities.put(t, city);
        forecastClocks.put(t, time);

        synchronized (this) {
            while (loadHandler == null) {
                try {
                    wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        loadHandler.obtainMessage(MESSAGE_LOAD, t).sendToTarget();
    }

    private void handleRequest(final T t){
        Forecast tmpResultForecast;

        final Long forecastClock = forecastClocks.get(t);

        ForecastCity city = forecastCities.get(t);
        if(city == null) return;

        tmpResultForecast = databaseWorker.loadForecast(city.id, forecastClock);
        if (tmpResultForecast == null) {
            try {
                HttpRequest request =  HttpRequest.get(String.format(REQUEST_URL, city.latitude, city.longitude));
                if (request.ok()) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    request.receive(outputStream);
                    for (Forecast tmpForecast : JSONForecastReader.getForecasts(outputStream.toString())){
                        if(tmpForecast.time == forecastClock){
                            tmpResultForecast = tmpForecast;
                            break;
                        }
                    }
                    if (tmpResultForecast != null) {
                        databaseWorker.saveForecast(tmpResultForecast);
                    }
                } else {
                    throw new HttpRequest.HttpRequestException(new IOException("Loading error: request status=" + request.message()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.HttpRequestException e){
                e.printStackTrace();
            }
        }

        final Forecast resultForecast = tmpResultForecast;
        responseHandler.post(new Runnable() {
            @Override
            public void run() {
                Long clock = forecastClocks.get(t);
                if (clock != null && clock.equals(forecastClock)) {
                    forecastCities.remove(t);
                    forecastClocks.remove(t);
                    loadListener.onLoad(t, resultForecast);
                }
            }
        });



    }

    public void setLoadListener(LoadListener<T> loadListener) {
        this.loadListener = loadListener;
    }

    public interface LoadListener<T>{
        public void onLoad(T t, Forecast forecast);
    }


}
