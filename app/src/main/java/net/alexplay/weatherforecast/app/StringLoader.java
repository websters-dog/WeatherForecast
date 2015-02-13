package net.alexplay.weatherforecast.app;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class StringLoader {

    public static String load(String urlString) throws IOException {
        Log.d("WEATHER_", urlString);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while((bytesRead = inputStream.read(buffer)) > 0){
                    outputStream.write(buffer, 0, bytesRead);
                }
                Log.d("WEATHER_", outputStream.toString());
                return outputStream.toString();
            } else {
                throw new IOException("Response code: \"" + connection.getResponseCode() + "\"");
            }
        } finally {
            connection.disconnect();
        }
    }

}
