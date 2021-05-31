package me.hgko.networkinfo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import me.hgko.networkinfo.activity.MainActivity;
import me.hgko.networkinfo.domain.LteSignalInfo;
import me.hgko.networkinfo.fragment.LocationFragment;
import me.hgko.networkinfo.manager.DataManager;
import me.hgko.networkinfo.util.CommonUtils;

/**
 * Created by hgko on 2018-09-20.
 */
public class LocationDataService extends Service {

    private final DataManager dataManager = DataManager.getInstance();
    private LocationManager locationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        initLocation();

        return START_STICKY;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LteSignalInfo lteSignalInfo = dataManager.getLteSignalInfo();
            if (lteSignalInfo != null) {
                lteSignalInfo.setProvider(location.getProvider());
                lteSignalInfo.setLatitude(location.getLatitude());
                lteSignalInfo.setLongitude(location.getLongitude());
                lteSignalInfo.setAltitude(location.getAltitude());
                lteSignalInfo.setSpeed(location.getSpeed());
                lteSignalInfo.setKmhSpeed((float) (location.getSpeed() * 3.6));
                lteSignalInfo.setAccuracy(location.getAccuracy());
                dataManager.setLteSignalInfo(lteSignalInfo);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("networkLog", "provider : " + provider);
            locationManager.removeUpdates(locationListener);
            initLocation();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private void initLocation() {
        if (!CommonUtils.checkPermission(getBaseContext())) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 1, locationListener);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
