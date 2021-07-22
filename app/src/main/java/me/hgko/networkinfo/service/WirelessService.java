package me.hgko.networkinfo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.hgko.networkinfo.db.DBHandler;
import me.hgko.networkinfo.domain.WifiDataInfo;
import me.hgko.networkinfo.manager.DataManager;
import me.hgko.networkinfo.util.CommonUtils;
import me.hgko.networkinfo.util.WifiInfoUtil;

/**
 * Created by hgko on 2018-09-20.
 */
public class WirelessService extends Service {

    private final DataManager dataManager = DataManager.getInstance();

    private final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    private ConnectivityManager manager;
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;

    private WifiManager wifiManager;

    private boolean broacastRegistered;

    private DBHandler dbHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dbHandler = new DBHandler(getBaseContext());

        manager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = (new NetworkRequest.Builder()).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        manager.registerNetworkCallback(networkRequest, wifiNetworkCallback);

        locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);

        return super.onStartCommand(intent, flags, startId);
    }

    private ConnectivityManager.NetworkCallback wifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            Log.d("MobileService", "WIFI 네트워크 연결됨");
            wifiDataInput(true);
        }

        @Override
        public void onLost(Network network) {
            Log.d("MobileService", "WIFI 네트워크 끊어짐");
            wifiDataInput(false);
            dataManager.setWifiDataInfo(null);
        }
    };

    private void wifiDataInput(boolean connected) {
        if (connected) {
            wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
            initWifiScan();
        } else {
            if (broacastRegistered) {
                getBaseContext().unregisterReceiver(wifiScanReceiver);
                broacastRegistered = false;
            }
        }
    }

    private void initWifiScan() {
        if (!broacastRegistered) {
            IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            getBaseContext().registerReceiver(wifiScanReceiver, filter);
            broacastRegistered = true;
        }
        wifiManager.startScan();
    }

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWIFIScanResult();
                wifiManager.startScan();
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    wifiDataInput(true);
                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    wifiDataInput(false);
                }
            }
        }
    };

    private void getWIFIScanResult() {
        wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            WifiDataInfo wifiDataInfo = new WifiDataInfo();
            wifiDataInfo.setDate(dateFormat.format(new Date()));
            wifiDataInfo.setWifiSsid(wifiInfo.getSSID().replace("\"", ""));
            wifiDataInfo.setBSSID(wifiInfo.getBSSID());
            wifiDataInfo.setNetworkId(wifiInfo.getNetworkId());
            wifiDataInfo.setMacAddress(wifiInfo.getMacAddress());

            wifiDataInfo.setRssi(wifiInfo.getRssi());
            wifiDataInfo.setLinkSpeed(wifiInfo.getLinkSpeed());
            DecimalFormat df = new DecimalFormat("#.##");
            wifiDataInfo.setDistance(Float.parseFloat(df.format(calculateDistance(wifiInfo.getRssi(), wifiInfo.getFrequency()))));

            wifiDataInfo.setWifiIp(WifiInfoUtil.intToInetAddress(wifiManager.getDhcpInfo().ipAddress));

            for (ScanResult result : wifiManager.getScanResults()) {
                if (wifiInfo.getSSID().equals("\"" + result.SSID + "\"")) {
                    wifiDataInfo.setCapabilities(result.capabilities.replace("][", ",").replace("[", "").replace("]", ""));
                    wifiDataInfo.setChannelWidth(WifiInfoUtil.getChannelBandwidth(result.channelWidth));
                    wifiDataInfo.setFrequency(result.frequency);
                    wifiDataInfo.setCenterFrequency(result.centerFreq0);
                }
            }

            if (!CommonUtils.checkPermission(getBaseContext())) {
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    wifiDataInfo.setLongitude(location.getLongitude());
                    wifiDataInfo.setLatitude(location.getLatitude());
                    wifiDataInfo.setAltitude(location.getAltitude());
                }
            }

            telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getNetworkOperatorName() != null) {
                wifiDataInfo.setNetworkOperatorName(telephonyManager.getNetworkOperatorName());
            }
            wifiDataInfo.setManufacturer(Build.MANUFACTURER);
            wifiDataInfo.setModel(Build.MODEL);

            dataManager.setWifiDataInfo(wifiDataInfo);

            Gson gson = new Gson();
            String json = gson.toJson(wifiDataInfo);

            Log.d("networkLog", json);

            dbHandler.inputData("WIFI", json);

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
        }
    }

    private double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (manager != null) {
            manager.unregisterNetworkCallback(wifiNetworkCallback);
        }

        if (broacastRegistered) {
            getBaseContext().unregisterReceiver(wifiScanReceiver);
        }

        dbHandler.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.unregisterNetworkCallback(wifiNetworkCallback);
        }

        if (broacastRegistered) {
            getBaseContext().unregisterReceiver(wifiScanReceiver);
        }

        dbHandler.close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
