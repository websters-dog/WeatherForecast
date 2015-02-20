package net.alexplay.weatherforecast.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentDays extends Fragment {

    public static final String KEY_CITY = "CITY";

    public static int DAYS_COUNT = 5;
    public static long NYCHTHEMERON = 1000 * 60 * 60 * 24;
    public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MMMM/yyyy");

    private ForecastCity city;

    public FragmentDays() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.days_fragment, container, false);

        TextView textHeader = (TextView) rootView.findViewById(R.id.t_days_header);
        ListView listDates = (ListView) rootView.findViewById(R.id.l_dates);
        listDates.setAdapter(new DaysAdapter());

        this.city = (ForecastCity) getArguments().get(KEY_CITY);
        textHeader.setText(city.name);

        return rootView;
    }

    private class DaysAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return DAYS_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return new Date(System.currentTimeMillis() + NYCHTHEMERON * position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.date_list_item, null);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.t_date);
            Date date = (Date) getItem(position);
            textView.setText(DATE_FORMAT.format(date));
            convertView.setOnClickListener(new DateListClickListener(date.getTime() - (date.getTime() % NYCHTHEMERON)));
            return convertView;
        }
    }

    private class DateListClickListener implements View.OnClickListener{

        private long dayZeroTime;

        public DateListClickListener(long dayZeroTime) {
            this.dayZeroTime = dayZeroTime;
        }

        @Override
        public void onClick(View v) {

            FragmentForecasts fragmentForecasts = new FragmentForecasts();

            Bundle bundle = new Bundle();
            bundle.putSerializable(FragmentForecasts.KEY_CITY, city);
            bundle.putSerializable(FragmentForecasts.KEY_START_TIME, dayZeroTime);
            fragmentForecasts.setArguments(bundle);

            View view = getView();
            if (view != null) {
                ViewParent parent = view.getParent();
                if (parent != null) {
                    getFragmentManager().beginTransaction()
                            .replace(((ViewGroup) parent).getId(), fragmentForecasts).addToBackStack(MainActivity.BACK_STACK_NAME).commit();
                }
            }

        }
    }

}
