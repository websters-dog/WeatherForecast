package net.alexplay.weatherforecast.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        database.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(ForecastCity.FeedEntry.COLUMN_ID, city.id);
            values.put(ForecastCity.FeedEntry.COLUMN_NAME, city.name);
            values.put(ForecastCity.FeedEntry.COLUMN_LATITUDE, city.latitude);
            values.put(ForecastCity.FeedEntry.COLUMN_LONGITUDE, city.longitude);
            database.insertWithOnConflict(ForecastCity.FeedEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            database.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

    }

    public ForecastCity loadCity(long id){

        database.beginTransaction();
        try {
            Cursor c = database.query(
                    ForecastCity.FeedEntry.TABLE,
                    new String[]{ForecastCity.FeedEntry.COLUMN_ID, ForecastCity.FeedEntry.COLUMN_NAME,
                            ForecastCity.FeedEntry.COLUMN_LATITUDE, ForecastCity.FeedEntry.COLUMN_LONGITUDE},
                    ForecastCity.FeedEntry.COLUMN_ID + " = ?",
                    new String[]{"" + id},
                    null, null, null);
            database.setTransactionSuccessful();
            if(c.moveToFirst()){
                return new ForecastCity(c.getLong(0), c.getString(1), c.getFloat(2), c.getFloat(3));
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        return null;
    }

    public ArrayList<ForecastCity> loadCities(){

        ArrayList<ForecastCity> cities = new ArrayList<ForecastCity>();
        database.beginTransaction();
        try {

            Cursor c = database.query(
                    ForecastCity.FeedEntry.TABLE,
                    new String[]{ForecastCity.FeedEntry.COLUMN_ID, ForecastCity.FeedEntry.COLUMN_NAME,
                            ForecastCity.FeedEntry.COLUMN_LATITUDE, ForecastCity.FeedEntry.COLUMN_LONGITUDE},
                    null, null, null, null, null);
            database.setTransactionSuccessful();
            while (c.moveToNext()){
                cities.add(new ForecastCity(c.getLong(0), c.getString(1), c.getFloat(2), c.getFloat(3)));
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

        return cities;
    }

    public void saveForecast(Forecast forecast){

        database.beginTransaction();
        try {

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
            values.put(Forecast.FeedEntry.COLUMN_LOAD_TIME, System.currentTimeMillis());
            database.insertWithOnConflict(Forecast.FeedEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("DB", "put:" + "\ntime=" + forecast.time + "; city=" + forecast.city.name);
            database.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    public ArrayList<Forecast> loadForecasts(long cityId){
        ForecastCity city = loadCity(cityId);
        ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
        database.beginTransaction();
        try {

            Cursor c = database.rawQuery("SELECT "
                    + Forecast.FeedEntry.COLUMN_CITY + " , "
                    + Forecast.FeedEntry.COLUMN_TIME + " , "
                    + Forecast.FeedEntry.COLUMN_TEMP_MIN + " , "
                    + Forecast.FeedEntry.COLUMN_TEMP_MAX + " , "
                    + Forecast.FeedEntry.COLUMN_PRESSURE + " , "
                    + Forecast.FeedEntry.COLUMN_HUMIDITY + " , "
                    + Forecast.FeedEntry.COLUMN_WIND_SPEED + " , "
                    + Forecast.FeedEntry.COLUMN_WIND_ANGLE + " , "
                    + Forecast.FeedEntry.COLUMN_CLOUDS + " , "
                    + Forecast.FeedEntry.COLUMN_ICON_CODE + " FROM "
                    + Forecast.FeedEntry.TABLE
                    + " WHERE " + Forecast.FeedEntry.COLUMN_CITY + " = " + cityId
                    + ";"
                    , null);
            database.setTransactionSuccessful();
            while (c.moveToNext()) {
                Forecast forecast = new Forecast(city, c.getLong(1), c.getFloat(2), c.getFloat(3), c.getFloat(4), c.getFloat(5),
                        c.getFloat(6), c.getFloat(7), c.getFloat(8), c.getString(9));
                forecasts.add(forecast);
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

        return forecasts;
    }

    public ArrayList<Forecast> loadForecasts(long cityId, long timeMin, long timeMax){
        ForecastCity city = loadCity(cityId);
        ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
        database.beginTransaction();
        try {
            String sql = "SELECT "
                    + Forecast.FeedEntry.COLUMN_CITY + " , "
                    + Forecast.FeedEntry.COLUMN_TIME + " , "
                    + Forecast.FeedEntry.COLUMN_TEMP_MIN + " , "
                    + Forecast.FeedEntry.COLUMN_TEMP_MAX + " , "
                    + Forecast.FeedEntry.COLUMN_PRESSURE + " , "
                    + Forecast.FeedEntry.COLUMN_HUMIDITY + " , "
                    + Forecast.FeedEntry.COLUMN_WIND_SPEED + " , "
                    + Forecast.FeedEntry.COLUMN_WIND_ANGLE + " , "
                    + Forecast.FeedEntry.COLUMN_CLOUDS + " , "
                    + Forecast.FeedEntry.COLUMN_ICON_CODE
                    + " FROM " + Forecast.FeedEntry.TABLE
                    + " WHERE " + Forecast.FeedEntry.COLUMN_CITY + " = " + cityId
                    + " AND " + Forecast.FeedEntry.COLUMN_TIME + " >= " + timeMin
                    + " AND " + Forecast.FeedEntry.COLUMN_TIME + " <= " + timeMax
                    + " ORDER BY " + Forecast.FeedEntry.COLUMN_TIME
                    + ";";
            Cursor c = database.rawQuery(sql, null);
            database.setTransactionSuccessful();
            while (c.moveToNext()) {
                Forecast forecast = new Forecast(city, c.getLong(1), c.getFloat(2), c.getFloat(3), c.getFloat(4), c.getFloat(5),
                        c.getFloat(6), c.getFloat(7), c.getFloat(8), c.getString(9));
                forecasts.add(forecast);
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

        return forecasts;
    }

    public Forecast loadForecast(long cityId, long time){
        ForecastCity city = loadCity(cityId);
        database.beginTransaction();
        try {

            String sql = "SELECT "
                    + Forecast.FeedEntry.COLUMN_CITY + " , "
                    + Forecast.FeedEntry.COLUMN_TIME + " , "
                    + Forecast.FeedEntry.COLUMN_TEMP_MIN + " , "
                    + Forecast.FeedEntry.COLUMN_TEMP_MAX + " , "
                    + Forecast.FeedEntry.COLUMN_PRESSURE + " , "
                    + Forecast.FeedEntry.COLUMN_HUMIDITY + " , "
                    + Forecast.FeedEntry.COLUMN_WIND_SPEED + " , "
                    + Forecast.FeedEntry.COLUMN_WIND_ANGLE + " , "
                    + Forecast.FeedEntry.COLUMN_CLOUDS + " , "
                    + Forecast.FeedEntry.COLUMN_ICON_CODE
                    + " FROM " + Forecast.FeedEntry.TABLE
                    + " WHERE " + Forecast.FeedEntry.COLUMN_CITY + " = " + cityId
                    + " AND " + Forecast.FeedEntry.COLUMN_TIME + " = " + time
                    + " AND ((" + System.currentTimeMillis() + " - " + Forecast.FeedEntry.COLUMN_LOAD_TIME + ") < " + Forecast.ACTUAL_TIME + ")"
                    + ";";
            Log.d("DB", sql);
            Cursor c = database.rawQuery(sql, null);
            database.setTransactionSuccessful();
            if(c.moveToFirst()){
                return new Forecast(city, c.getLong(1), c.getFloat(2), c.getFloat(3), c.getFloat(4), c.getFloat(5),
                        c.getFloat(6), c.getFloat(7), c.getFloat(8), c.getString(9));
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

        return null;
    }

}























