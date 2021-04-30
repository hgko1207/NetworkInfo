package me.hgko.networkinfo.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by inspace on 2018-07-30.
 */

public class WifiInfoUtil {

    /**
     * Wifi 연결 여부
     *
     * @return
     */
    public static boolean isWifiConnected(ConnectivityManager manager) {
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && activeNetwork.isConnectedOrConnecting();
        } else { // 네트워크가 불가능한 상태
            return false;
        }
    }

    /**
     * MAC Address 불러오기
     *
     * @return
     */
    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0"))
                    continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    /**
     * Wifi 주파수 대역에 따른 채널값 구하는 함수
     *
     * @param freq
     * @return
     */
    public static int convertFrequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            if (freq == 2484)
                return (freq - 2412) / 5;
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }

    public static int getChannelBandwidth(int channelWidth) {
        switch (channelWidth) {
            case 0:
                return 20;
            case 1:
                return 40;
            case 2:
                return 80;
            case 3:
            case 4:
                return 160;
            default:
                return 0;
        }
    }

    public static String intToInetAddress(int hostAddress) {
        return String.format(
                "%d.%d.%d.%d",
                (hostAddress & 0xff),
                (hostAddress >> 8 & 0xff),
                (hostAddress >> 16 & 0xff),
                (hostAddress >> 24 & 0xff));
    }
}
