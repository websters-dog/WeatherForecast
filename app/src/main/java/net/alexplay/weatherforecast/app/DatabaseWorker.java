package net.alexplay.weatherforecast.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DatabaseWorker {
	
	private static DatabaseWorker databaseWorker = new DatabaseWorker();

	private SQLiteDatabase database;
	private DatabaseHelper databaseHelper;

	private DatabaseWorker() {
	}

    public void setContext(Context context){
        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
    }
	
	public static DatabaseWorker get(){
		return databaseWorker;
	}

    public void saveCity(ForecastCity city){

        Cursor c = null;
        database.beginTransaction();
        try {
            c = database.query(
                    ForecastCity.FeedEntry.TABLE,
                    new String[]{ForecastCity.FeedEntry.COLUMN_ID, ForecastCity.FeedEntry.COLUMN_NAME,
                            ForecastCity.FeedEntry.COLUMN_LATITUDE, ForecastCity.FeedEntry.COLUMN_LONGITUDE},
                    ForecastCity.FeedEntry.COLUMN_ID + " = ?",
                    new String[]{"" + city.id},
                    null,
                    null,
                    null
            );
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        if(c != null && !c.moveToFirst()){
            ContentValues values = new ContentValues();
            values.put(ForecastCity.FeedEntry.COLUMN_ID, city.id);
            values.put(ForecastCity.FeedEntry.COLUMN_NAME, city.name);
            values.put(ForecastCity.FeedEntry.COLUMN_LATITUDE, city.latitude);
            values.put(ForecastCity.FeedEntry.COLUMN_LONGITUDE, city.longitude);
            database.insert(ForecastCity.FeedEntry.TABLE, null, values);
        }
    }

    public ForecastCity loadCity(long id){

        Cursor c = null;
        database.beginTransaction();
        try {
            c = database.query(
                    ForecastCity.FeedEntry.TABLE,
                    new String[]{ForecastCity.FeedEntry.COLUMN_ID, ForecastCity.FeedEntry.COLUMN_NAME,
                            ForecastCity.FeedEntry.COLUMN_LATITUDE, ForecastCity.FeedEntry.COLUMN_LONGITUDE},
                    ForecastCity.FeedEntry.COLUMN_ID + " = ?",
                    new String[]{"" + id},
                    null, null, null);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        ForecastCity city = null;
        if(c != null && c.moveToFirst()){
            city = new ForecastCity(c.getLong(0), c.getString(1), c.getFloat(2), c.getFloat(3));
        }
        return city;
    }

    public void saveForecast(Forecast forecast){
        database.beginTransaction();
        try {
            Cursor c = database.rawQuery("SELECT "
                    + Forecast.FeedEntry.COLUMN_CITY + " , " + Forecast.FeedEntry.COLUMN_TIME + " , " + Forecast.FeedEntry.COLUMN_TEMP_MIN
                    + " , " + Forecast.FeedEntry.COLUMN_TEMP_MAX + " , " + Forecast.FeedEntry.COLUMN_PRESSURE + " , " + Forecast.FeedEntry.COLUMN_HUMIDITY
                    + " , " + Forecast.FeedEntry.COLUMN_WIND_SPEED + " , " + Forecast.FeedEntry.COLUMN_WIND_ANGLE + " , " + Forecast.FeedEntry.COLUMN_CLOUDS
                    + " , " + Forecast.FeedEntry.COLUMN_ICON_CODE
                    + " FROM " + Forecast.FeedEntry.TABLE + " WHERE "
                    + Forecast.FeedEntry.COLUMN_CITY + " = " + forecast.city.id + " AND "
                    + Forecast.FeedEntry.COLUMN_TIME + " = " + forecast.time + ";", null);

            if(!c.moveToFirst()){
                ContentValues values = new ContentValues();
                values.put(Forecast.FeedEntry.COLUMN_CITY, forecast.city.id);
                values.put(Forecast.FeedEntry.COLUMN_TIME, forecast.time);
                values.put(Forecast.FeedEntry.COLUMN_TEMP_MIN, forecast.tempMin);
                values.put(Forecast.FeedEntry.COLUMN_TEMP_MAX, forecast.tempMax);
                values.put(Forecast.FeedEntry.COLUMN_PRESSURE, forecast.pressure);
                values.put(Forecast.FeedEntry.COLUMN_HUMIDITY, forecast.humidity);
                values.put(Forecast.FeedEntry.COLUMN_WIND_SPEED, forecast.windSpeed);
                values.put(Forecast.FeedEntry.COLUMN_WIND_ANGLE, forecast.windAngle);
                values.put(Forecast.FeedEntry.COLUMN_CLOUDS, forecast.cloudsPercent);
                values.put(Forecast.FeedEntry.COLUMN_ICON_CODE, forecast.iconCode);
                database.insert(Forecast.FeedEntry.TABLE, null, values);
            } else {
                database.rawQuery("UPDATE " + Forecast.FeedEntry.TABLE + " SET "
                        + Forecast.FeedEntry.COLUMN_TEMP_MIN + " = " + forecast.tempMin + " , "
                        + Forecast.FeedEntry.COLUMN_TEMP_MAX + " = " + forecast.tempMax + " , "
                        + Forecast.FeedEntry.COLUMN_PRESSURE + " = " + forecast.pressure + " , "
                        + Forecast.FeedEntry.COLUMN_HUMIDITY + " = " + forecast.humidity + " , "
                        + Forecast.FeedEntry.COLUMN_WIND_SPEED + " = " + forecast.windSpeed + " , "
                        + Forecast.FeedEntry.COLUMN_WIND_ANGLE + " = " + forecast.windAngle + " , "
                        + Forecast.FeedEntry.COLUMN_CLOUDS + " = " + forecast.cloudsPercent + " , "
                        + Forecast.FeedEntry.COLUMN_ICON_CODE + " = \"" + forecast.iconCode + "\""
                        + " WHERE "
                        + Forecast.FeedEntry.COLUMN_CITY + " = " + forecast.city.id + " AND "
                        + Forecast.FeedEntry.COLUMN_TIME + " = " + forecast.time + ";", null);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    public ArrayList<Forecast> loadActualForecasts(long cityId){
        ForecastCity city = loadCity(cityId);
        Cursor c = null;
        database.beginTransaction();
        try {
            c = database.rawQuery("SELECT "
                    + Forecast.FeedEntry.COLUMN_CITY + " , " + Forecast.FeedEntry.COLUMN_TIME + " , " + Forecast.FeedEntry.COLUMN_TEMP_MIN
                    + " , " + Forecast.FeedEntry.COLUMN_TEMP_MAX + " , " + Forecast.FeedEntry.COLUMN_PRESSURE + " , " + Forecast.FeedEntry.COLUMN_HUMIDITY
                    + " , " + Forecast.FeedEntry.COLUMN_WIND_SPEED + " , " + Forecast.FeedEntry.COLUMN_WIND_ANGLE + " , " + Forecast.FeedEntry.COLUMN_CLOUDS
                    + " , " + Forecast.FeedEntry.COLUMN_ICON_CODE
                    + " FROM " + Forecast.FeedEntry.TABLE
                    + " WHERE "
                    + Forecast.FeedEntry.COLUMN_CITY + " = " + cityId
                    + " AND "
                    + Forecast.FeedEntry.COLUMN_TIME + " > strftime('%s','now')"
                    + ";"
                    , null);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

        ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
        if(c.moveToFirst()){
            do {
                Forecast forecast = new Forecast(city, c.getLong(1), c.getFloat(2), c.getFloat(3), c.getFloat(4), c.getFloat(5),
                        c.getFloat(6), c.getFloat(7), c.getFloat(8), c.getString(9));
                forecasts.add(forecast);
            } while (c.moveToNext());
        }
        return forecasts;
    }

}






















