package me.hgko.networkinfo.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.hgko.networkinfo.R;
import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

/**
 * Created by inspace on 2018-10-16.
 */
public class WeatherFragment extends Fragment implements YahooWeatherInfoListener {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    Unbinder unbinder;
    @BindView(R.id.logitudeText)
    TextView logitudeText;
    @BindView(R.id.latitudeText)
    TextView latitudeText;
    @BindView(R.id.datetimeText)
    TextView datetimeText;
    @BindView(R.id.addressText)
    TextView addressText;
    @BindView(R.id.weatherText)
    TextView weatherText;
    @BindView(R.id.temperatureText)
    TextView temperatureText;
    @BindView(R.id.humidityText)
    TextView humidityText;
    @BindView(R.id.pressureText)
    TextView pressureText;
    @BindView(R.id.windDirectionText)
    TextView windDirectionText;
    @BindView(R.id.windSpeedText)
    TextView windSpeedText;
    @BindView(R.id.windChillText)
    TextView windChillText;
    @BindView(R.id.visibilityText)
    TextView visibilityText;

    private YahooWeather yahooWeather;

    private Timer timer;

    private boolean isVisible;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        yahooWeather = YahooWeather.getInstance(5000, false);

        isVisible = true;

        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible) {
            if (isVisibleToUser) { // 화면에 실제로 보일때
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
            } else { // preload 될 때(전페이지에 있을때)
                if (timer != null) {
                    timer.cancel();
                }
            }
        }
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
        if (weatherInfo != null) {
            datetimeText.setText(dateFormat.format(new Date()));
            logitudeText.setText(weatherInfo.getAddress().getLongitude() + "");
            latitudeText.setText(weatherInfo.getAddress().getLatitude() + "");
            addressText.setText(weatherInfo.getAddress().getAddressLine(0));
            weatherText.setText(weatherInfo.getCurrentText());
            temperatureText.setText(weatherInfo.getCurrentTemp() + " ºC");
            humidityText.setText(weatherInfo.getAtmosphereHumidity() + " %");
            pressureText.setText(weatherInfo.getAtmospherePressure());
            windDirectionText.setText(weatherInfo.getWindDirection() + "˚");
            windSpeedText.setText(weatherInfo.getWindSpeed() + " m/s");
            windChillText.setText(weatherInfo.getWindChill() + " °F");
            visibilityText.setText(weatherInfo.getAtmosphereVisibility());
        }
    }

    private void searchByGPS() {
        yahooWeather.setNeedDownloadIcons(true);
        yahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        yahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        yahooWeather.queryYahooWeatherByGPS(getContext(), this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        isVisible = false;
        unbinder.unbind();
    }
}
