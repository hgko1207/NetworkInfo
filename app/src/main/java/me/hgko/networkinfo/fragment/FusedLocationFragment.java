package me.hgko.networkinfo.fragment;

import android.location.Location;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.util.CommonUtils;

/**
 * Created by inspace on 2018-09-27.
 */

public class FusedLocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    Unbinder unbinder;
    @BindView(R.id.providerText)
    TextView providerText;
    @BindView(R.id.altitudeText)
    TextView altitudeText;
    @BindView(R.id.onOffImage)
    ImageView onOffImage;
    @BindView(R.id.allProvidersText)
    TextView allProvidersText;
    @BindView(R.id.enableProvidersText)
    TextView enableProvidersText;
    @BindView(R.id.speedText)
    TextView speedText;
    @BindView(R.id.currentSpeedText)
    TextView currentSpeedText;

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi providerApi;

    private boolean isVisible;
    private boolean requestLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fused_location, container, false);

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        providerApi = LocationServices.FusedLocationApi;

//        mLocationClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//
//        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(1000);

        isVisible = true;

        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible) {
            if (isVisibleToUser) { // 화면에 실제로 보일때
                googleApiClient.connect();
            } else { // preload 될 때(전페이지에 있을때)
                if (requestLocation) {
                    providerApi.removeLocationUpdates(googleApiClient, locationListener);
                    requestLocation = false;
                }
                googleApiClient.disconnect();
            }
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d("networkLog", location.toString());
                providerText.setText(location.getProvider());
                latitudeText.setText(String.valueOf(location.getLatitude()));
                longitudeText.setText(String.valueOf(location.getLongitude()));
                altitudeText.setText(String.valueOf(location.getAltitude()));
                accuracyText.setText(String.valueOf(location.getAccuracy() + "m"));
                speedText.setText(String.valueOf(location.getSpeed()) + " m/s");

                double currentSpeed = round(location.getSpeed(), 3, BigDecimal.ROUND_HALF_UP);
                double kmphSpeed = round((currentSpeed * 3.6), 3, BigDecimal.ROUND_HALF_UP);
                currentSpeedText.setText(kmphSpeed + " km/h");
                timeText.setText(dateFormat.format(new Date(location.getTime())));
                String address = CommonUtils.getAddress(getContext(), location.getLatitude(), location.getLongitude());
                addressText.setText(address);
            }
        }
    };

    public double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!CommonUtils.checkPermission(getContext())) {
            onOffImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_gps_on, null));

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000);
            providerApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
            requestLocation = true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onOffImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_gps_off, null));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestLocation) {
            providerApi.removeLocationUpdates(googleApiClient, locationListener);
        }
        googleApiClient.disconnect();
        unbinder.unbind();
    }
}
