package net.alexplay.weatherforecast.app;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.github.kevinsawicki.http.HttpRequest;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FragmentSearch extends Fragment {

    private final static String REQUEST_URL = "http://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&units=metric";
    private final static int PRELOAD_COUNT = 15;

    private final static float LATITUDE_MIN = -90f;
    private final static float LATITUDE_MAX = 90f;
    private final static float LONGITUDE_MIN = -180f;
    private final static float LONGITUDE_MAX = 180f;

    private EditText editLongitude;
    private EditText editLatitude;
    private View loadingLayout;

    private AsyncTask<Float, Void, ForecastCity> preloadTask;

    public FragmentSearch() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.search_fragment, container, false);

        editLatitude = (EditText) rootView.findViewById(R.id.e_latitude);
        editLongitude = (EditText) rootView.findViewById(R.id.e_longitude);
        loadingLayout = rootView.findViewById(R.id.l_loading);
        loadingLayout.setVisibility(View.GONE);
        loadingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button buttonSearch = (Button) rootView.findViewById(R.id.b_search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchButtonClick();
            }

        });

        return rootView;
    }

    private void onSearchButtonClick(){
        loadingLayout.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editLatitude.getWindowToken(), 0);

        try {
            if(preloadTask != null
                    && preloadTask.getStatus() == AsyncTask.Status.RUNNING){
                return;
            }
            preloadTask = new PreloadAsynkTask();
            preloadTask.execute(getFloatFromField(editLatitude, LATITUDE_MIN, LATITUDE_MAX),
                    getFloatFromField(editLongitude, LONGITUDE_MIN, LONGITUDE_MAX));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getResources().getString(R.string.incorrect_search_data), Toast.LENGTH_LONG).show();
            loadingLayout.setVisibility(View.GONE);
        }
    }

    private float getFloatFromField(EditText editText, float minValue, float maxValue){
        float result = Float.parseFloat(editText.getText().toString());
        if (result > maxValue || result < minValue){
            throw new NumberFormatException();
        }
        return result;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroyView() {
        if(preloadTask != null && preloadTask.getStatus() == AsyncTask.Status.RUNNING){
            preloadTask.cancel(true);
        }
        super.onDestroyView();
    }


    private class PreloadAsynkTask extends AsyncTask<Float, Void, ForecastCity> {

        @Override
        protected ForecastCity doInBackground(Float... params) {
            try {
                HttpRequest request =  HttpRequest.get(String.format(String.format(REQUEST_URL, params[0], params[1])));
                if (request.ok()) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    request.receive(outputStream);
                    ArrayList<Forecast> forecasts = JSONForecastReader.getForecasts(outputStream.toString());
                    if (forecasts != null && forecasts.size() > 0) {
                        int count = PRELOAD_COUNT > forecasts.size() ? forecasts.size() : PRELOAD_COUNT;
                        for (int i = 0; i < count; i++) {
                            DatabaseWorker.get().saveForecast(forecasts.get(i));
                        }
                        return forecasts.get(0).city;
                    }
                } else {
                    throw new HttpRequest.HttpRequestException(new IOException("Loading error: request status=" + request.message()));
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.HttpRequestException e) {
                e.printStackTrace();
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), getResources().getString(R.string.load_error), Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(ForecastCity result) {
            super.onPostExecute(result);
            if (result != null) {
                showDateFragment(result);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.load_error), Toast.LENGTH_LONG).show();
            }
            loadingLayout.setVisibility(View.GONE);
        }
    }

    private void showDateFragment(ForecastCity city){
        FragmentDays fragmentDays = new FragmentDays();
        Bundle bundle = new Bundle();
        bundle.putSerializable(FragmentDays.KEY_CITY, city);
        fragmentDays.setArguments(bundle);
        View view = getView();
        if (view != null) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                getFragmentManager().beginTransaction()
                        .replace(((ViewGroup) parent).getId(), fragmentDays).addToBackStack(MainActivity.BACK_STACK_NAME).commit();
            }
        }
    }
}
