package net.alexplay.weatherforecast.app;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {

    private Map<String, Drawable> drawableMap = Collections.synchronizedMap(new HashMap<String, Drawable>());
    private Map<LoadListener, String> waitMap = Collections.synchronizedMap(new HashMap<LoadListener, String>());


    public void loadImage(String url, LoadListener loadListener){
        Log.d("WEATHER_ImageLoader_", "load:" + url);
        if(drawableMap.containsKey(url)){
            Log.d("WEATHER_ImageLoader_", "return: " + url);
            loadListener.onLoad(drawableMap.get(url));
        } else {
            if(!waitMap.containsValue(url)){
                waitMap.put(loadListener, url);
                Drawable drawable = null;
                Log.d("WEATHER_ImageLoader_", "loading: " + url);
                try {
                    byte[] bytes = HttpLoader.load(url);
                    drawable = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                drawableMap.put(url, drawable);
                for(LoadListener tmpLoadListener : waitMap.keySet()){
                    if(waitMap.get(loadListener).equals(url)){
                        waitMap.remove(tmpLoadListener);
                        Log.d("WEATHER_ImageLoader_", "return loaded: " + url);
                        loadListener.onLoad(drawable);
                    }
                }
            } else {
                waitMap.put(loadListener, url);
            }
        }
    }


    public interface LoadListener{
        public void onLoad(Drawable drawable);
    }

}
