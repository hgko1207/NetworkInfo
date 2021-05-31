package me.hgko.networkinfo.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.hgko.networkinfo.R;

/**
 * 구글 지도 화면
 * Created by hgko on 2018-09-20.
 */
public class GoogleMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Unbinder unbinder;
    @BindView(R.id.map)
    MapView mapView;

    private GoogleMap map;

    private GoogleApiClient apiClient;
    private FusedLocationProviderApi providerApi;

    private boolean isVisible;
    private boolean requestLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_google_map, container, false);
        unbinder = ButterKnife.bind(this, view);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        isVisible = true;

        //add~~~~~~~~~~~~~~~~~~~
        apiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        providerApi = LocationServices.FusedLocationApi;

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible) {
            if (isVisibleToUser) { // 화면에 실제로 보일때
                apiClient.connect();
            } else { // preload 될 때(전페이지에 있을때)
                if (requestLocation) {
                    providerApi.removeLocationUpdates(apiClient, locationListener);
                    requestLocation = false;
                }
                apiClient.disconnect();
            }
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            moveMapCamera(location);
        }
    };

    private void moveMapCamera(Location location) {
        if (map != null) {
            map.clear();

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//            map.animateCamera(CameraUpdateFactory.zoomTo(16));

            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location));
//            Address address = getAddress(location);
//            if (address != null) {
//                markerOptions.title(address.getLocality() + " " + address.getThoroughfare());
//                markerOptions.snippet(address.getAddressLine(0));
//            }
            markerOptions.position(latLng);
            map.addMarker(markerOptions);
        }
    }

    /**
     * 위도, 경도 값으로 주소 얻어오기
     * @param location
     * @return
     */
    private Address getAddress(Location location) {
        Geocoder geocoder = new Geocoder(getContext());

        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            return addressList.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = providerApi.getLastLocation(apiClient);
            if (location != null) {
                moveMapCamera(location);
            }
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        providerApi.requestLocationUpdates(apiClient, locationRequest, locationListener);
        requestLocation = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        if (requestLocation) {
            providerApi.removeLocationUpdates(apiClient, locationListener);
        }
        apiClient.disconnect();
        unbinder.unbind();
    }
}
