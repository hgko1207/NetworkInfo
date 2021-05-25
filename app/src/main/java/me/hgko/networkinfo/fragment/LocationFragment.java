package me.hgko.networkinfo.fragment;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.util.CommonUtils;

/**
 * Created by inspace on 2018-09-21.
 */
public class LocationFragment extends Fragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Unbinder unbinder;
    @BindView(R.id.onOffImage)
    ImageView onOffImage;
    @BindView(R.id.allProvidersText)
    TextView allProvidersText;
    @BindView(R.id.enableProvidersText)
    TextView enableProvidersText;
    @BindView(R.id.providerText)
    TextView providerText;
    @BindView(R.id.latitudeText)
    TextView latitudeText;
    @BindView(R.id.longitudeText)
    TextView longitudeText;
    @BindView(R.id.accuracyText)
    TextView accuracyText;
    @BindView(R.id.timeText)
    TextView timeText;
    @BindView(R.id.addressText)
    TextView addressText;
    @BindView(R.id.speedText)
    TextView speedText;
    @BindView(R.id.altitudeText)
    TextView altitudeText;
    @BindView(R.id.currentSpeedText)
    TextView currentSpeedText;

    private LocationManager locationManager;

    private boolean isVisible;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        unbinder = ButterKnife.bind(this, view);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        isVisible = true;

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible) {
            if (isVisibleToUser) { // 화면에 실제로 보일때
                getProviders();
                initLocation();
            } else { // preload 될 때(전페이지에 있을때)
                locationManager.removeUpdates(locationListener);
            }
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            Log.d("networkLog", "LocationFragment => " + location.toString());
            setLocationInfo(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            locationManager.removeUpdates(locationListener);
//            initLocation();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private void initLocation() {
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final String bestProvider = locationManager.getBestProvider(criteria, true);
        Log.d("test", "bestProvider = " + bestProvider);

        // GPS 정보 가져오기
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 현재 네트워크 상태 값 알아오기
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.d("test", "isProviderEnabled = " + isGPSEnabled + ", " + isNetworkEnabled);

        if (!isGPSEnabled) {

        }

        if (!CommonUtils.checkPermission(getActivity())) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 1, locationListener);
        }

//        // GPS 프로바이더 사용가능여부
//        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        // 네트워크 프로바이더 사용가능여부
//        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        if (!isGPSEnabled && !isNetworkEnabled) {
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
//            onOffImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_gps_off, null));
//        }
//
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
//        } else {
//            Location location = null;
//            if (isNetworkEnabled) {
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
//                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
//                if (locationManager != null) {
//                    provider = LocationManager.NETWORK_PROVIDER;
//                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                }
//            }
//            // if GPS Enabled get lat/long using GPS Services
//            if (isGPSEnabled) {
//                if (location == null) {
//                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
//                    if (locationManager != null) {
//                        provider = LocationManager.GPS_PROVIDER;
//                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    }
//                }
//            }
//
//            Log.d("MainActivity", "location = " + location);
//
//            if (location != null) {
//                setLocationInfo(location);
//            }
//        }
    }

    private void setLocationInfo(Location location) {
        providerText.setText(location.getProvider());
        onOffImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_gps_on, null));
        latitudeText.setText(String.valueOf(location.getLatitude()));
        longitudeText.setText(String.valueOf(location.getLongitude()));
        altitudeText.setText(String.valueOf(location.getAltitude()));
        speedText.setText(String.valueOf(location.getSpeed()) + " m/s");

        double currentSpeed = round(location.getSpeed(), 3, BigDecimal.ROUND_HALF_UP);
        double kmphSpeed = round((currentSpeed * 3.6), 3, BigDecimal.ROUND_HALF_UP);
        currentSpeedText.setText(kmphSpeed + " km/h");
        accuracyText.setText(String.valueOf(location.getAccuracy()) + " m");

        timeText.setText(dateFormat.format(new Date(location.getTime())));
        String address = CommonUtils.getAddress(getContext(), location.getLatitude(), location.getLongitude());
        addressText.setText(address);
    }

    public double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    private void getProviders() {
        String result = "App Providers : ";
        List<String> providers = locationManager.getAllProviders();
        for (String provider : providers) {
            result += provider + " ";
        }
        allProvidersText.setText(result);

        result = "Enabled Providers : ";
        List<String> enabledProviders = locationManager.getProviders(true);
        for (String provider : enabledProviders) {
            result += provider + " ";
        }
        enableProvidersText.setText(result);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        locationManager.removeUpdates(locationListener);
    }
}
