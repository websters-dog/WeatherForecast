package net.alexplay.weatherforecast.app;

import java.io.Serializable;

public interface ScreenController extends Serializable{

    public void showSearchScreen();
    public void showDateScreen(ForecastCity city);
    public void showForecastScreen(ForecastCity city, long dayZeroTime);
    public void back();
}
