package me.hgko.networkinfo.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import me.hgko.networkinfo.domain.LteSignalInfo;
import me.hgko.networkinfo.manager.DataManager;
import me.hgko.networkinfo.util.CommonUtils;

/**
 * Created by inspace on 2018-10-30.
 */
public class LocationFusedService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final DataManager dataManager = DataManager.getInstance();

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi providerApi;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        googleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        providerApi = LocationServices.FusedLocationApi;

        googleApiClient.connect();

        return START_STICKY;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
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
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!CommonUtils.checkPermission(getBaseContext())) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000);
            providerApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        providerApi.removeLocationUpdates(googleApiClient, locationListener);
        googleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        providerApi.removeLocationUpdates(googleApiClient, locationListener);
        googleApiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
