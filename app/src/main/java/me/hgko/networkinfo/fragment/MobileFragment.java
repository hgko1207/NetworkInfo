package me.hgko.networkinfo.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.util.CommonUtils;
import me.hgko.networkinfo.util.NetworkInfoUtil;

/**
 * Created by inspace on 2018-09-20.
 */
public class MobileFragment extends Fragment {

    Unbinder unbinder;
    @BindView(R.id.speedometer)
    SpeedometerGauge speedometer;
    @BindView(R.id.signalStrengthText)
    TextView signalStrengthText;
    @BindView(R.id.networkNameText)
    TextView networkNameText;
    @BindView(R.id.simNameText)
    TextView simNameText;
    @BindView(R.id.networkTypeText)
    TextView networkTypeText;
    @BindView(R.id.networkStrengthText)
    TextView networkStrengthText;
    @BindView(R.id.signalLevelText)
    TextView signalLevelText;
    @BindView(R.id.dataStateText)
    TextView dataStateText;
    @BindView(R.id.cellTacText)
    TextView cellTacText;
    @BindView(R.id.cellMncText)
    TextView cellMncText;
    @BindView(R.id.cellMccText)
    TextView cellMccText;
    @BindView(R.id.countryCodeText)
    TextView countryCodeText;
    @BindView(R.id.phoneTypeText)
    TextView phoneTypeText;
    @BindView(R.id.deviceIdText)
    TextView deviceIdText;
    @BindView(R.id.phoneNumberText)
    TextView phoneNumberText;
    @BindView(R.id.manufacturerText)
    TextView manufacturerText;
    @BindView(R.id.modelText)
    TextView modelText;

    private ConnectivityManager manager;
    private TelephonyManager telephonyManager;

    private SignalStrengthListener signalStrengthListener;

    private boolean isVisible;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile, container, false);
        unbinder = ButterKnife.bind(this, view);

        isVisible = true;

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("MainActivity", "setUserVisibleHint : " + isVisibleToUser);
        if (isVisible) {
            if (isVisibleToUser) { // 화면에 실제로 보일때
                startSignalStrengthListener();
            } else { // preload 될 때(전페이지에 있을때)
                endSignalStrengthListener();
            }
        }
    }

    private void initView() {
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress) - 140);
            }
        });
        speedometer.setMaxSpeed(80);
        speedometer.setMajorTickStep(10);
        speedometer.setMinorTicks(5);
        speedometer.setLabelTextSize(50);
        speedometer.setDefaultColor(0xFFFFFFFF);

        speedometer.addColoredRange(0, 21, 0xFFE23D1E);
        speedometer.addColoredRange(21, 48, 0xFF43C6CD);
        speedometer.addColoredRange(48, 80, 0xFF6EDB39);
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            NetworkInfo networkInfo = manager.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d("MainActivity", "LTE 네트워크 연결됨");
                startSignalStrengthListener();
            }
        }

        @Override
        public void onLost(Network network) {
            NetworkInfo networkInfo = manager.getNetworkInfo(network);
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    Log.d("MainActivity", "LTE 네트워크 끊어짐");
                    dataStateText.setText("연결해제");
                    endSignalStrengthListener();
                }
            } else {
                dataStateText.setText("연결해제");
                endSignalStrengthListener();
            }
        }
    };

    private class SignalStrengthListener extends PhoneStateListener {

        private Context context;

        public SignalStrengthListener(Context context) {
            this.context = context;
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (!CommonUtils.checkPermission(context)) {
                List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        if (cellInfo.isRegistered()) {
                            CellSignalStrengthLte signalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                            CellIdentityLte identityLte = ((CellInfoLte) cellInfo).getCellIdentity();

                            // gets RSRP cell signal strength:
                            // RSRP (Reference Signal Received Power) : -140dBm ~ -44dBm으로 1dB 단위로 변경될 수 있다.
                            final int rsrp = signalStrengthLte.getDbm();

                            // ASU : Arbitrary Strength Unit
                            final int asuLevel = signalStrengthLte.getAsuLevel();

                            final int level = signalStrengthLte.getLevel();

                            // Gets the LTE MCC: (returns 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown)
                            // 450은 South Korea를 의미
                            final int cellMcc = identityLte.getMcc();

                            // Gets theLTE MNC: (returns 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown)
                            // SKT: 05, KT: 08, LG U+: 06
                            final int cellMnc = identityLte.getMnc();

                            final int cellCi = identityLte.getCi();

                            // Gets the LTE TAC: (returns 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown)
                            final int cellTac = identityLte.getTac();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    speedometer.setSpeed(rsrp + 140, 1000, 300);
                                    signalStrengthText.setText(rsrp + " dBm / " + asuLevel + " ASU");
                                    networkStrengthText.setText(rsrp + " dBm * " + asuLevel + " ASU");
                                    signalLevelText.setText(level + "");

                                    cellTacText.setText(cellCi + " (ECI - E-UTRAN Cell Identifier)" +
                                            "\n" + cellTac + " (TAC - Tracking Area Code)");
                                    cellMncText.setText((cellMnc < 10 ? "0" + cellMnc : cellMnc) + " (MNC - 모바일 네트워크 코드)");
                                    cellMccText.setText(cellMcc + " (MCC - Mobile Country Code)");
                                }
                            });
                        }
                    }
                }
            }

            networkTypeText.setText(NetworkInfoUtil.getNetworkType(telephonyManager));
            dataStateText.setText(isNetworkConnected() ? "데이터 연결됨" : "연결해제");
            phoneTypeText.setText(NetworkInfoUtil.getPhoneType(telephonyManager));

//            Log.d("MainActivity'", "onSignalStrengthsChanged() called with "
//                    + signalStrength + "; gsmSignalStrength = [" + signalStrength.getGsmSignalStrength() + "]");
        }
    }

    private void startSignalStrengthListener() {
        signalStrengthListener = new SignalStrengthListener(this.getContext());
        telephonyManager = (TelephonyManager) this.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);

        networkNameText.setText(telephonyManager.getNetworkOperatorName());
        simNameText.setText(telephonyManager.getSimOperatorName());
        countryCodeText.setText(telephonyManager.getNetworkCountryIso());

        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1002);
        } else {
            deviceIdText.setText(telephonyManager.getDeviceId());
            phoneNumberText.setText(telephonyManager.getLine1Number());
        }

        manufacturerText.setText(Build.MANUFACTURER);
        modelText.setText(Build.MODEL);
    }

    private void endSignalStrengthListener() {
        if (telephonyManager != null) {
            telephonyManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private boolean isNetworkConnected() {
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && activeNetwork.isConnectedOrConnecting();
        } else {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        isVisible = false;
        manager.unregisterNetworkCallback(networkCallback);
        endSignalStrengthListener();
    }
}
