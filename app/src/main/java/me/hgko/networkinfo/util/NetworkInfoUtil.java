package me.hgko.networkinfo.util;

import android.telephony.TelephonyManager;

import lombok.Getter;
import me.hgko.networkinfo.domain.LteInfo;

/**
 * Created by inspace on 2018-08-02.
 */

public class NetworkInfoUtil {

    @Getter
    public enum CompanyType {
        SKT("SKT", 5),
        KT("KT", 8),
        LG("LG U+", 6);

        private String name;

        private int code;

        CompanyType(String name, int code) {
            this.name = name;
            this.code = code;
        }

        public static CompanyType fromCode(int code) {
            for (CompanyType type : CompanyType.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }

            return null;
        }
    }

    public static LteInfo getLteInfo(int dl) {
        for (LteInfo.EARFCN earfcn : LteInfo.EARFCN.values()) {
            if (earfcn.getDownlinkLow() <= dl && dl <= earfcn.getDownlinkHigh()) {
                LteInfo lteInfo = new LteInfo();
                int between = dl - earfcn.getDownlinkLow();
                lteInfo.setUplink(earfcn.getUplinkLow() + between);
                lteInfo.setEarfcn(earfcn);
                return lteInfo;
            }
        }

        return null;
    }

    public static String getNetworkType(TelephonyManager telephonyManager) {
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "LTE";
            default:
                return "Unknown";
        }
    }

    public static String getPhoneType(TelephonyManager telephonyManager) {
        switch (telephonyManager.getPhoneType()) {
            case TelephonyManager.PHONE_TYPE_NONE:
                return "NONE";
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.PHONE_TYPE_GSM:
                return "GSM";
            case TelephonyManager.PHONE_TYPE_SIP:
                return "SIP";
            default:
                return "Unknown";
        }
    }
}
