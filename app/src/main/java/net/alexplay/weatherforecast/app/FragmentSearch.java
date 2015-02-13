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

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSearch extends Fragment {

    private final static String REQUEST_URL = "http://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric";

    private EditText editLongitude;
    private EditText editLatitude;

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

                (new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {
                        try {
                            String result = StringLoader.load(String.format(REQUEST_URL,
                                    Float.parseFloat(editLatitude.getText().toString()), Float.parseFloat(editLongitude.getText().toString())));
                            return result;
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            return "";
                        }
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        if(result.length() > 0){
                            try {
                                saveForecast(result);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                }).execute();
            }
        });

        return rootView;
    }

    public void saveForecast(String jsonString) throws JSONException {
        JSONObject jsonForecastMain = new JSONObject(jsonString);
        JSONObject jsonCity = jsonForecastMain.getJSONObject(ForecastCity.JsonEntry.OBJECT);

        ForecastCity city = new ForecastCity(
                jsonCity.getLong(ForecastCity.JsonEntry.ID),
                jsonCity.getString(ForecastCity.JsonEntry.NAME),
                (float) jsonCity.getJSONObject(ForecastCity.JsonEntry.OBJECT_CORDS).getDouble(ForecastCity.JsonEntry.LATITUDE),
                (float) jsonCity.getJSONObject(ForecastCity.JsonEntry.OBJECT_CORDS).getDouble(ForecastCity.JsonEntry.LONGITUDE)
                );
        Log.d("WEATHER_", city.toString());

        ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
        JSONArray jsonForecastArray = jsonForecastMain.getJSONArray(Forecast.JsonEntry.OBJECTS_ARRAY);
        JSONObject jsonForecast;
        for(int i = 0; i < jsonForecastArray.length(); i++){
            jsonForecast = jsonForecastArray.getJSONObject(i);
            forecasts.add(new Forecast(
                    city,
                    jsonForecast.getLong(Forecast.JsonEntry.TIME),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.TEMP_MIN),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.TEMP_MAX),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.PRESSURE),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_MAIN).getDouble(Forecast.JsonEntry.HUMIDITY),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_WIND).getDouble(Forecast.JsonEntry.WIND_SPEED),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_WIND).getDouble(Forecast.JsonEntry.WIND_ANGLE),
                    (float) jsonForecast.getJSONObject(Forecast.JsonEntry.OBJECT_CLOUDS).getDouble(Forecast.JsonEntry.CLOUDS),
                    jsonForecast.getJSONArray(Forecast.JsonEntry.OBJECTS_WEATHER_ARRAY).getJSONObject(0).getString(Forecast.JsonEntry.ICON_CODE)
                    ));
            Log.d("WEATHER_", forecasts.get(forecasts.size() - 1).toString());
        }


    }


}
