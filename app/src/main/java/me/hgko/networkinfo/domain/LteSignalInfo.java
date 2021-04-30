package me.hgko.networkinfo.domain;

import lombok.Data;

/**
 * Created by inspace on 2018-09-21.
 */
@Data
public class LteSignalInfo {

    public static int HOUSE_MODE = 1;
    public static int COMPANY_MODE = 2;
    public static int DRIVING_MODE = 3;
    public static int WALKING_MODE = 4;
    public static int ETC_MODE = 5;

    private String date;

    private int mode;

    private String networkType;

    private String companyType;

    private String manufacturer;

    private String model;

    // RSRP (Reference Signal Received Power) : -140dBm ~ -44dBm으로 1dB 단위로 변경될 수 있다.
    // Get reference signal received power
    private int rsrp;

    // Get reference signal received quality
    private int rsrq;

    // Get reference signal signal-to-noise ratio
    private int rssnr;

    // Get channel quality indicator
    private int cqi;

    // Get signal level as an int from 0..4
    private int level;

    /**
     * ASU : Arbitrary Strength Unit
     * Get the LTE signal level as an asu value between 0..97, 99 is unknown
     * Asu is calculated based on 3GPP RSRP. Refer to 3GPP 27.007 (Ver 10.3.0) Sec 8.69
     */
    private int asuLevel;

    /**
     * Get the timing advance value for LTE, as a value between 0..63.
     * Integer.MAX_VALUE is reported when there is no active RRC
     * connection. Refer to 3GPP 36.213 Sec 4.2.3
     */
    private int timingAdvance;

    // Gets the LTE MCC: (returns 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown)
    // 450은 South Korea를 의미
    private int mcc;

    // Gets theLTE MNC: (returns 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown)
    // SKT: 05, KT: 08, LG U+: 06
    private int mnc;

    // 28-bit Cell Identity, Integer.MAX_VALUE if unknown
    private int ci;

    // Gets the LTE TAC: (returns 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown)
    private int tac;

    // EARFCN(DL)
    private int earfcn;

    // EARFCN(UL)
    private int uplink;

    private int band;

    // 단위 MHz
    private int bandwidth;

    private String cell;

    private int pci;

    /** 위치 정보 추가 */
    private String provider;

    // 위도
    private double longitude;

    // 경도
    private double latitude;

    // 고도
    private double altitude;

    // 속도
    private float speed;

    private float kmhSpeed;

    private float accuracy;

    /** 날씨 정보 추가 2018.10.18 */
    private String weather;

    private int temperature;

    private float humidity;

    private float windSpeed;

    private int windDirection;
}
