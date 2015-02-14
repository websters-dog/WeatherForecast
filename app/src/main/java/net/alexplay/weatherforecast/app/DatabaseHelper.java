package net.alexplay.weatherforecast.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private  final static String DATABASE_NAME = "weather";
	
	private static final String KEY = " PRIMARY KEY";

	private static final String TYPE_TEXT = " TEXT";
	private static final String TYPE_INTEGER = " INTEGER";
	private static final String TYPE_REAL = " REAL";
	private static final String COMMA_SEP = " , ";
	
	private static final String SQL_CREATE_CITIES =
			"CREATE TABLE " + ForecastCity.FeedEntry.TABLE + " ("
			+ ForecastCity.FeedEntry.COLUMN_ID + TYPE_INTEGER + KEY + COMMA_SEP
			+ ForecastCity.FeedEntry.COLUMN_NAME + TYPE_TEXT + COMMA_SEP
			+ ForecastCity.FeedEntry.COLUMN_LATITUDE + TYPE_REAL + COMMA_SEP
			+ ForecastCity.FeedEntry.COLUMN_LONGITUDE + TYPE_REAL
			+ ");";

    private static final String SQL_CREATE_FORECASTS =
			"CREATE TABLE " + Forecast.FeedEntry.TABLE + " ("
			+ Forecast.FeedEntry.COLUMN_TIME + TYPE_INTEGER + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_CITY + TYPE_INTEGER + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_TEMP_MIN + TYPE_REAL + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_TEMP_MAX + TYPE_REAL + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_PRESSURE + TYPE_REAL + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_HUMIDITY + TYPE_REAL + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_CLOUDS + TYPE_REAL + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_WIND_SPEED + TYPE_REAL + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_WIND_ANGLE + TYPE_REAL + COMMA_SEP
			+ Forecast.FeedEntry.COLUMN_ICON_CODE + TYPE_TEXT + COMMA_SEP
            + KEY + " (" + Forecast.FeedEntry.COLUMN_TIME + COMMA_SEP + Forecast.FeedEntry.COLUMN_CITY + ") "
			+ ");";
	
	public final static int DATABASE_VERSION = 1; 
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_CITIES);
		db.execSQL(SQL_CREATE_FORECASTS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

    public void saveCity(ForecastCity city){

    }

	
	

}
