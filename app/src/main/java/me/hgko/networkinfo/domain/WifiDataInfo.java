package me.hgko.networkinfo.domain;

import lombok.Data;

/**
 * Created by inspace on 2018-09-21.
 */
@Data
public class WifiDataInfo {

    private String date;

    // 사업자 이름
    private String networkOperatorName;

    private String wifiSsid;

    private String BSSID;

    private int networkId;

    private String macAddress;

    private int rssi;

    // 단위 : Mbps
    private int linkSpeed;

    // 거리 - 단위(m)
    private float distance;

    // 암호화 방식
    private String capabilities;

    private String wifiIp;

    // 채널 폭 - 단위(MHz)
    private int channelWidth;

    // 주파수 - 단위(MHz)
    private int frequency;

    // 중심주파수 - 단위(MHz)
    private int centerFrequency;

    // 위도
    private double longitude;

    // 경도
    private double latitude;

    // 고도
    private double altitude;

    // 기기명
    private String manufacturer;

    // 모델
    private String model;
}
