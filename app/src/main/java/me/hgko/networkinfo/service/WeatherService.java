package me.hgko.networkinfo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import me.hgko.networkinfo.db.DBHandler;
import me.hgko.networkinfo.domain.LteSignalInfo;
import me.hgko.networkinfo.fragment.LocationFragment;
import me.hgko.networkinfo.fragment.WeatherFragment;
import me.hgko.networkinfo.manager.DataManager;
import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

/**
 * Created by inspace on 2018-10-18.
 */
public class WeatherService extends Service implements YahooWeatherInfoListener {

    private final DataManager dataManager = DataManager.getInstance();

    private Timer timer;

    private YahooWeather yahooWeather;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        yahooWeather = YahooWeather.getInstance(5000, false);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        searchByGPS();
                    }
                });
            }
        }, 0, 1000 * 60 * 10);

        return START_STICKY;
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
        if (weatherInfo != null) {
            LteSignalInfo lteSignalInfo = dataManager.getLteSignalInfo();
            if (lteSignalInfo != null) {
                lteSignalInfo.setWeather(weatherInfo.getCurrentText());
                lteSignalInfo.setTemperature(weatherInfo.getCurrentTemp());
                lteSignalInfo.setHumidity(Float.parseFloat(weatherInfo.getAtmosphereHumidity()));
                lteSignalInfo.setWindDirection(Integer.parseInt(weatherInfo.getWindDirection()));
                lteSignalInfo.setWindSpeed(Float.parseFloat(weatherInfo.getWindSpeed()));
                dataManager.setLteSignalInfo(lteSignalInfo);
            }
        }
    }

    /**
     */
    private void searchByGPS() {
        yahooWeather.setNeedDownloadIcons(true);
        yahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        yahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        yahooWeather.queryYahooWeatherByGPS(getBaseContext(), this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
