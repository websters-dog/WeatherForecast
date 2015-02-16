package net.alexplay.weatherforecast.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Stack;


public class MainActivity extends ActionBarActivity {


    ScreenController screenController = new ScreenController() {

        private Stack<Fragment> fragmentStack = new Stack<Fragment>();

        private FragmentForecasts fragmentForecasts;
        private FragmentDays fragmentDays;
        private FragmentSearch fragmentSearch;

        @Override
        public void showSearchScreen() {
            if (fragmentStack.size() > 0) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragmentStack.peek())
                        .commit();
            }
            if (fragmentSearch == null) {
                fragmentSearch = new FragmentSearch();
            }
            Bundle bundle = new Bundle();
            fragmentSearch.setArguments(bundle);
            fragmentSearch.setScreenController(this);
            fragmentStack.add(fragmentSearch);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragmentSearch)
                    .commit();
        }

        @Override
        public void showDateScreen(ForecastCity city) {
            if (fragmentStack.size() > 0) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragmentStack.peek())
                        .commit();
            }

            if (fragmentDays == null) {
                fragmentDays = new FragmentDays();
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(FragmentDays.KEY_CITY, city);
            fragmentDays.setArguments(bundle);
            fragmentDays.setScreenController(this);
            fragmentStack.add(fragmentDays);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragmentDays)
                    .commit();

        }

        @Override
        public void showForecastScreen(ForecastCity city, long dayZeroTime) {
            if (fragmentStack.size() > 0) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragmentStack.peek())
                        .commit();
            }
            if (fragmentForecasts == null) {
                fragmentForecasts = new FragmentForecasts();
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(FragmentForecasts.KEY_CITY, city);
            bundle.putSerializable(FragmentForecasts.KEY_START_TIME, dayZeroTime);
            fragmentForecasts.setScreenController(this);
            fragmentForecasts.setArguments(bundle);
            fragmentStack.add(fragmentForecasts);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragmentForecasts)
                    .commit();
        }

        @Override
        public void back() {
            if (fragmentStack.size() > 0) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragmentStack.pop())
                        .commit();
                if (fragmentStack.size() > 0) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, fragmentStack.peek())
                            .commit();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            screenController.showSearchScreen();
        }

        DatabaseWorker.get().setContext(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            screenController.back();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
