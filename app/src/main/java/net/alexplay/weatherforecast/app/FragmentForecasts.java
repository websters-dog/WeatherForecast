package net.alexplay.weatherforecast.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

public class FragmentForecasts extends Fragment {


    public static final String KEY_CITY = "CITY";
    public static final String KEY_START_TIME = "START_TIME";

    public static int DAYS_COUNT = 5;
    public static long NYCHTHEMERON = 1000 * 60 * 60 * 24;
    public static long TIME_INTERVAL = 1000 * 60 * 60 * 3;

    public static final SimpleDateFormat DATE_FORMAT_HEADER = new SimpleDateFormat("dd/MMM/yyyy");
    public static final SimpleDateFormat DATE_FORMAT_LIST = new SimpleDateFormat("HH:mm z");

    static {
        DATE_FORMAT_HEADER.setTimeZone(new SimpleTimeZone(0, "GMT"));
        DATE_FORMAT_LIST.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }

    private ForecastLoader<View> forecastLoader;

    private TextView textHeader;
    private ListView listDates;

    private ForecastCity city;
    private ScreenController screenController;

    private long startTime;
    private long openTime;


    public FragmentForecasts() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.forecasts_fragment, container, false);

        this.city = (ForecastCity) getArguments().get(KEY_CITY);

        textHeader = (TextView) rootView.findViewById(R.id.t_forecast_header);
        listDates= (ListView) rootView.findViewById(R.id.l_forecasts);

        startTime = getArguments().getLong(KEY_START_TIME);
        openTime = System.currentTimeMillis();

        forecastLoader = new ForecastLoader<View>(new Handler(), DatabaseWorker.get());
        forecastLoader.start();
        forecastLoader.setLoadListener(new ForecastLoader.LoadListener<View>() {
            @Override
            public void onLoad(View view, Forecast forecast, Drawable forecastDrawable) {
                if (forecast != null) {
                    TextView textView = (TextView) view.findViewById(R.id.t_forecast);
                    String text = String.format(getResources().getString(R.string.forecast_line),
                            DATE_FORMAT_LIST.format(new Date(forecast.time)),
                            forecast.tempMin, forecast.tempMax, forecast.pressure, forecast.humidity, forecast.cloudsPercent, forecast.windSpeed);
                    textView.setText(text);
                    ImageView imageView = (ImageView) view.findViewById(R.id.i_forecast);
                    imageView.setImageDrawable(forecastDrawable);
                }
            }
        });

        listDates.setAdapter(new DaysAdapter());

        textHeader.setText(city.name + "\n" + DATE_FORMAT_HEADER.format(new Date(startTime)));

        return rootView;
    }

    public void setScreenController(ScreenController screenController) {
        this.screenController = screenController;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        forecastLoader.quit();
    }

    private class DaysAdapter extends BaseAdapter{


        @Override
        public int getCount() {
            if(startTime < openTime){
                return (int) ((NYCHTHEMERON) / TIME_INTERVAL - (openTime % NYCHTHEMERON / TIME_INTERVAL + 1));
            } else {
                return (int) ((NYCHTHEMERON - startTime % NYCHTHEMERON) / TIME_INTERVAL);
            }
        }

        @Override
        public Object getItem(int position) {
            if(startTime < openTime){
                return startTime + ((NYCHTHEMERON) / TIME_INTERVAL - getCount() + position) * TIME_INTERVAL;
            } else {
                return startTime + TIME_INTERVAL * position;
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.forecast_list_item, null);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.t_forecast);
            Long forecastClock = (Long) getItem(position);
            textView.setText(String.format(DATE_FORMAT_LIST.format(new Date(forecastClock)) + "\n ---"));
            ImageView imageView = (ImageView) convertView.findViewById(R.id.i_forecast);
            imageView.setImageDrawable(null);
            forecastLoader.loadForecast(convertView, city, forecastClock);
            return convertView;
        }
    }

}
