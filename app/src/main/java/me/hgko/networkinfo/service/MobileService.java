package me.hgko.networkinfo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.hgko.networkinfo.db.DBHandler;
import me.hgko.networkinfo.domain.Constants;
import me.hgko.networkinfo.domain.LteInfo;
import me.hgko.networkinfo.domain.LteSignalInfo;
import me.hgko.networkinfo.manager.DataManager;
import me.hgko.networkinfo.util.CommonUtils;
import me.hgko.networkinfo.util.NetworkInfoUtil;

/**
 * Created by inspace on 2018-09-20.
 */
public class MobileService extends Service {

    private final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    private final DataManager dataManager = DataManager.getInstance();

    private ConnectivityManager manager;
    private TelephonyManager telephonyManager;

    private SignalStrengthListener signalStrengthListener;

    private LteSignalInfo lteSignalInfo;

    private DBHandler dbHandler;

    private int mode;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.registerNetworkCallback(new NetworkRequest.Builder().build(), lteNetworkCallback);

        dbHandler = new DBHandler(getBaseContext());
        dbHandler.deleteAll();

        mode = dataManager.getMode();

        return START_STICKY;
    }

    private ConnectivityManager.NetworkCallback lteNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            NetworkInfo networkInfo = manager.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d("MobileService", "LTE 네트워크 연결됨");
                startSignalStrengthListener();
            }
        }

        @Override
        public void onLost(Network network) {
            NetworkInfo networkInfo = manager.getNetworkInfo(network);
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    Log.d("MobileService", "LTE 네트워크 끊어짐");
                    endSignalStrengthListener();
                }
            } else {
                endSignalStrengthListener();
            }
        }
    };

    private class SignalStrengthListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (!CommonUtils.checkPermission(getBaseContext())) {
                lteSignalInfo = dataManager.getLteSignalInfo();
                if (lteSignalInfo == null) {
                    lteSignalInfo = new LteSignalInfo();
                }

                if (lteSignalInfo.getProvider() != null && dataManager.getWifiDataInfo() == null) {
                    List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
                    for (CellInfo cellInfo : cellInfoList) {
                        if (cellInfo instanceof CellInfoLte) {
                            if (cellInfo.isRegistered()) {
                                CellSignalStrengthLte signalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                                CellIdentityLte identityLte = ((CellInfoLte) cellInfo).getCellIdentity();

                                lteSignalInfo.setDate(dateFormat.format(new Date()));
                                lteSignalInfo.setNetworkType(NetworkInfoUtil.getNetworkType(telephonyManager));
                                lteSignalInfo.setCompanyType(NetworkInfoUtil.CompanyType.fromCode(identityLte.getMnc()).getName());

                                lteSignalInfo.setRsrp(signalStrengthLte.getDbm());
                                lteSignalInfo.setRsrq(signalStrengthLte.getRsrq());
                                lteSignalInfo.setRssnr(signalStrengthLte.getRssnr());
                                lteSignalInfo.setCqi(signalStrengthLte.getCqi());
                                lteSignalInfo.setAsuLevel(signalStrengthLte.getAsuLevel());
                                lteSignalInfo.setLevel(signalStrengthLte.getLevel());
                                lteSignalInfo.setTimingAdvance(signalStrengthLte.getTimingAdvance());

                                lteSignalInfo.setMcc(identityLte.getMcc());
                                lteSignalInfo.setMnc(identityLte.getMnc());
                                lteSignalInfo.setCi(identityLte.getCi());
                                lteSignalInfo.setTac(identityLte.getTac());
                                lteSignalInfo.setEarfcn(identityLte.getEarfcn());
                                lteSignalInfo.setPci(identityLte.getPci());

                                LteInfo lteInfo = NetworkInfoUtil.getLteInfo(identityLte.getEarfcn());
                                lteSignalInfo.setUplink(lteInfo.getUplink());
                                lteSignalInfo.setBand(lteInfo.getEarfcn().getBand());
                                lteSignalInfo.setBandwidth(lteInfo.getEarfcn().getBandwidth());

                                String cellidHex = DecToHex(identityLte.getCi());
                                String eNBHex = cellidHex.substring(0, cellidHex.length() - 2);
                                String cellID = cellidHex.substring(cellidHex.length() - 2, cellidHex.length());
                                if (isNumeric(cellID)) {
                                    lteSignalInfo.setCell(HexToDec(eNBHex) + "-" + Integer.parseInt(cellID));
                                } else {
                                    lteSignalInfo.setCell(HexToDec(eNBHex) + "");
                                }
                            }
                        }
                    }

                    lteSignalInfo.setMode(mode);
                    lteSignalInfo.setManufacturer(Build.MANUFACTURER);
                    lteSignalInfo.setModel(Build.MODEL);

                    insertLteInfo();
                }

                dataManager.setLteSignalInfo(lteSignalInfo);
            }
            super.onSignalStrengthsChanged(signalStrength);
        }
    }

    private void insertLteInfo() {
        if (mode == 3 || mode == 4) {
            if (lteSignalInfo.getProvider().equals("network") || lteSignalInfo.getSpeed() == 0) {
                dbHandler.deleteData("LTE");
                return;
            }
        }

        Gson gson = new Gson();
        String json = gson.toJson(lteSignalInfo);
        Log.d("networkLog", json);

        dbHandler.inputData("LTE", json);
    }

    private void startSignalStrengthListener() {
        signalStrengthListener = new SignalStrengthListener();
        telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void endSignalStrengthListener() {
        if (telephonyManager != null) {
            telephonyManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    // Decimal -> hexadecimal
    public String DecToHex(int dec) {
        return String.format("%x", dec);
    }

    // hex -> decimal
    public int HexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }

    private boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (manager != null) {
            manager.unregisterNetworkCallback(lteNetworkCallback);
        }

        dbHandler.close();
        endSignalStrengthListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.unregisterNetworkCallback(lteNetworkCallback);
        }

        dbHandler.close();
        endSignalStrengthListener();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
