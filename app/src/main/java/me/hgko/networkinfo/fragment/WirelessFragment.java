package me.hgko.networkinfo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.util.WifiInfoUtil;

/**
 * WIFI 정보 화면
 * Created by hgko on 2018-09-20.
 */
public class WirelessFragment extends Fragment {

    Unbinder unbinder;
    @BindView(R.id.ssidText)
    TextView ssidText;
    @BindView(R.id.bssidText)
    TextView bssidText;
    @BindView(R.id.macText)
    TextView macText;
    @BindView(R.id.distanceText)
    TextView distanceText;
    @BindView(R.id.lineSpeedText)
    TextView lineSpeedText;
    @BindView(R.id.rssiText)
    TextView rssiText;
    @BindView(R.id.wsecText)
    TextView wsecText;
    @BindView(R.id.channelText)
    TextView channelText;
    @BindView(R.id.frequencyText)
    TextView frequencyText;
    @BindView(R.id.channelWidthText)
    TextView channelWidthText;
    @BindView(R.id.wifiIpText)
    TextView wifiIpText;

    private ConnectivityManager manager;
    private WifiManager wifiManager;

    private boolean isVisible;
    private boolean broacastRegistered;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wireless, container, false);
        unbinder = ButterKnife.bind(this, view);

        isVisible = true;

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = (new NetworkRequest.Builder()).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        manager.registerNetworkCallback(networkRequest, networkCallback);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible) {
            if (isVisibleToUser) { // 화면에 실제로 보일때
                setDataInput(WifiInfoUtil.isWifiConnected(manager));
            } else { // preload 될 때(전페이지에 있을때)
                if (broacastRegistered) {
                    getContext().unregisterReceiver(wifiScanReceiver);
                    broacastRegistered = false;
                }
            }
        }
    }

    /**
     * WIFI 데이터 표출
     * @param connected
     */
    private void setDataInput(boolean connected) {
        if (connected) {
            wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            initWifiScan();

            ssidText.setText(wifiInfo.getSSID().replace("\"", ""));
            bssidText.setText(wifiInfo.getBSSID());
            macText.setText(WifiInfoUtil.getMacAddress());

            // 암호화 방식
            for (ScanResult result : wifiManager.getScanResults()) {
                if (wifiInfo.getSSID().equals("\"" + result.SSID + "\"")) {
                    wsecText.setText(result.capabilities);
                    int channelWidth = WifiInfoUtil.getChannelBandwidth(result.channelWidth);
                    int centerFrequency = result.centerFreq0;
                    frequencyText.setText(result.frequency + " (" + centerFrequency + ") MHz");
                    channelWidthText.setText(channelWidth + " MHz (" + (centerFrequency - (channelWidth / 2)) + "-"
                            + (centerFrequency + (channelWidth / 2)) + " MHz)");
                }
            }

            // BSS: Base Service Sets
            channelText.setText(WifiInfoUtil.convertFrequencyToChannel(wifiInfo.getFrequency()) + "");
            wifiIpText.setText(WifiInfoUtil.intToInetAddress(wifiManager.getDhcpInfo().ipAddress));
        } else {
            if (broacastRegistered) {
                getContext().unregisterReceiver(wifiScanReceiver);
                broacastRegistered = false;
            }

            ssidText.setText("WIFI 꺼짐");
            bssidText.setText("");
            macText.setText("");
            wsecText.setText("");
            distanceText.setText("");
            lineSpeedText.setText("");
            frequencyText.setText("");
            rssiText.setText("");
            channelWidthText.setText("");
            channelText.setText("");
            wifiIpText.setText("");
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            Log.d("MainActivity", "WIFI 네트워크 연결됨");
            setDataInput(true);
        }

        @Override
        public void onLost(Network network) {
            Log.d("MainActivity", "WIFI 네트워크 끊어짐");
            setDataInput(false);
        }
    };

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWifiScanResult();
                wifiManager.startScan();
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    setDataInput(true);
                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    setDataInput(false);
                }
            }
        }
    };

    /**
     * WIFI Scan Start
     */
    private void initWifiScan() {
        if (!broacastRegistered) {
            IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            getContext().registerReceiver(wifiScanReceiver, filter);
            broacastRegistered = true;
        }
        wifiManager.startScan();
    }

    /**
     * WIFI Scan 결과 불러오기
     */
    private void getWifiScanResult() {
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            DecimalFormat df = new DecimalFormat("#.##");
            distanceText.setText(df.format(calculateDistance(wifiInfo.getRssi(), wifiInfo.getFrequency())) + " 미터");
            lineSpeedText.setText(wifiInfo.getLinkSpeed() + " Mbps");
            rssiText.setText(wifiInfo.getRssi() + " dBm");
        }
    }

    /**
     * 주파수와 RSSI를 통한 WIFI 단말과의 거리 계산
     * @param levelInDb
     * @param freqInMHz
     * @return
     */
    public double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        manager.unregisterNetworkCallback(networkCallback);
        if (broacastRegistered) {
            getContext().unregisterReceiver(wifiScanReceiver);
        }
        unbinder.unbind();
    }
}
