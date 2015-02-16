package net.alexplay.weatherforecast.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class FragmentSearch extends Fragment {

    private final static String REQUEST_URL = "http://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric";
    private final static int PRELOAD_COUNT = 15;

    private EditText editLongitude;
    private EditText editLatitude;

    private ScreenController screenController;

    public FragmentSearch() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_fragment, container, false);

        editLatitude = (EditText) rootView.findViewById(R.id.e_latitude);
        editLongitude= (EditText) rootView.findViewById(R.id.e_longitude);

        Button buttonSearch = (Button) rootView.findViewById(R.id.b_search);


        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                float latitude = 0;
                float longitude = 0;
                try {
                    latitude = Float.parseFloat(editLatitude.getText().toString());
                    longitude = Float.parseFloat(editLongitude.getText().toString());

                    if (latitude > 90 || latitude < -90
                            || longitude > 180 || longitude < -180){
                        throw new NumberFormatException();
                    }

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getResources().getString(R.string.incorrect_search_data), Toast.LENGTH_LONG).show();
                    return;
                }

                (new AsyncTask<Float, Void, String>() {

                    @Override
                    protected String doInBackground(Float... params) {
                        try {
                            String result = StringLoader.load(String.format(REQUEST_URL,
                                    params[0], params[1]));
                            return result;
                        } catch (final IOException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            return "";
                        }
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        if(result.length() > 0){
                            ForecastCity city = null;
                            try {
                                city = processForecast(result);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            screenController.showDateScreen(city);
                        }

                    }
                }).execute(latitude, longitude);
            }
        });

        return rootView;
    }

    public void setScreenController(ScreenController screenController) {
        this.screenController = screenController;
    }

    public ForecastCity processForecast(String jsonString) throws JSONException {
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
        int count = PRELOAD_COUNT > jsonForecastArray.length() ? jsonForecastArray.length() : PRELOAD_COUNT;
        for(int i = 0; i < count; i++){
            JSONObject jsonForecast = jsonForecastArray.getJSONObject(i);
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
            DatabaseWorker.get().saveForecast(forecast);
        }

        ArrayList<Forecast> loadActualForecasts = DatabaseWorker.get().loadForecasts(city.id);
        Log.d("WEATHER_", System.currentTimeMillis() + " LOADED: " + loadActualForecasts.size());
        for(Forecast forecast : loadActualForecasts){
            Log.d("WEATHER_", "LOADED:" + forecast.toString());
        }

        return city;

    }


}
