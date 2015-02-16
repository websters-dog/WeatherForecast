package net.alexplay.weatherforecast.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;

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

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editLatitude.getWindowToken(), 0);

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
                            String result = new String(HttpLoader.load(String.format(REQUEST_URL, params[0], params[1])));
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
                                ArrayList<Forecast> forecasts = HttpLoader.processForecastString(result);
                                if (forecasts != null && forecasts.size() > 0) {

                                    city = forecasts.get(0).city;

                                    int count = PRELOAD_COUNT > forecasts.size() ? forecasts.size() : PRELOAD_COUNT;
                                    for(int i = 0; i < count; i++){
                                        DatabaseWorker.get().saveForecast(forecasts.get(i));
                                    }
                                }
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




}
