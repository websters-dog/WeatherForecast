package net.alexplay.weatherforecast.app;

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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

public class FragmentForecasts extends Fragment {


    public static final String KEY_CITY = "CITY";
    public static final String KEY_START_TIME = "START_TIME";

    private static  final String RAIN_URL = "https://ssl.gstatic.com/onebox/weather/256/rain.png";
    private static  final String SUNNY_URL = "https://ssl.gstatic.com/onebox/weather/256/sunny.png";
    private static  final String CLOUDY_URL = "https://ssl.gstatic.com/onebox/weather/256/cloudy.png";

    public static long NYCHTHEMERON = 1000 * 60 * 60 * 24;
    public static long TIME_INTERVAL = 1000 * 60 * 60 * 3;

    public static final SimpleDateFormat DATE_FORMAT_HEADER = new SimpleDateFormat("dd/MMM/yyyy");
    public static final SimpleDateFormat DATE_FORMAT_LIST = new SimpleDateFormat("HH:mm z");

    static {
        DATE_FORMAT_HEADER.setTimeZone(new SimpleTimeZone(0, "GMT"));
        DATE_FORMAT_LIST.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }

    private ForecastLoader<View> forecastLoader;

    private ForecastCity city;

    private long startTime;
    private long openTime;


    public FragmentForecasts() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.forecasts_fragment, container, false);

        this.city = (ForecastCity) getArguments().get(KEY_CITY);

        TextView textHeader = (TextView) rootView.findViewById(R.id.t_forecast_header);
        ListView listForecasts = (ListView) rootView.findViewById(R.id.l_forecasts);

        forecastLoader = new ForecastLoader<View>(new Handler(), DatabaseWorker.get());
        forecastLoader.setLoadListener(new ForecastLoader.LoadListener<View>() {
            @Override
            public void onLoad(View view, Forecast forecast) {

                TextView textView = (TextView) view.findViewById(R.id.t_forecast);
                ImageView imageView = (ImageView) view.findViewById(R.id.i_forecast);

                if (forecast != null) {

                    String text = String.format(getResources().getString(R.string.forecast_line),
                            DATE_FORMAT_LIST.format(new Date(forecast.time)),
                            forecast.tempMin, forecast.tempMax, forecast.pressure, forecast.humidity, forecast.cloudsPercent, forecast.windSpeed);
                    textView.setText(text);

                    String imageUrl;
                    if(forecast.iconCode.contains("01") || forecast.iconCode.contains("02")){
                        imageUrl = SUNNY_URL;
                    } else if(forecast.iconCode.contains("03") || forecast.iconCode.contains("04")){
                        imageUrl = CLOUDY_URL;
                    } else {
                        imageUrl = RAIN_URL;
                    }
                    Picasso.with(getActivity()).load(imageUrl)
                            .placeholder(R.drawable.spinner).error(R.drawable.error).into(imageView);

                } else {
                    Picasso.with(getActivity()).load(R.drawable.error).into(imageView);
                }
            }
        });
        forecastLoader.start();
        forecastLoader.getLooper();

        startTime = getArguments().getLong(KEY_START_TIME);
        openTime = System.currentTimeMillis();
        textHeader.setText(city.name + "\n" + DATE_FORMAT_HEADER.format(new Date(startTime)));
        listForecasts.setAdapter(new DaysAdapter());

        return rootView;
    }

    @Override
    public void onDestroyView() {
        forecastLoader.clearQueue();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        forecastLoader.quit();
        super.onDetach();
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
            textView.setText(String.format(DATE_FORMAT_LIST.format(new Date(forecastClock)) + "\n" + getResources().getString(R.string.loading)));
            ImageView imageView = (ImageView) convertView.findViewById(R.id.i_forecast);
            Picasso.with(getActivity()).load(R.drawable.spinner).into(imageView);
            forecastLoader.loadForecast(convertView, city, forecastClock);
            return convertView;
        }
    }

}
