package me.hgko.networkinfo.domain;

import lombok.Data;
import lombok.Getter;

/**
 * Created by inspace on 2018-08-02.
 */
@Data
public class LteInfo {

    private int uplink;

    private EARFCN earfcn;

    @Getter
    public enum EARFCN {
        BAND_1(1, 0, 599, 18000, 20),
        BAND_2(2, 600, 1199, 18600, 20),
        BAND_3(3, 1200, 1949, 19200, 20),
        BAND_4(4, 1950, 2399, 19950, 20),
        BAND_5(5, 2400, 2649, 20400, 10),
        BAND_6(6, 2650, 2749, 20650, 10),
        BAND_7(7, 2750, 3449, 20750, 20),
        BAND_8(8, 3450, 3799, 21450, 10),
        BAND_9(9, 3800, 4149, 21800, 20),
        BAND_10(10, 4150, 4749, 22150, 20),
        BAND_11(11, 4750, 4949, 22750, 10),
        BAND_12(12, 5010, 5179, 23010, 10),
        BAND_13(13, 5180, 5279, 23180, 10),
        BAND_14(14, 5280, 5379, 23280, 10),
        BAND_17(17, 5730, 5849, 23730, 10);

        private int band;

        private int downlinkLow;

        private int downlinkHigh;

        private int uplinkLow;

        private int bandwidth;

        EARFCN(int band, int downlinkLow, int downlinkHigh, int uplinkLow, int bandwidth) {
            this.band = band;
            this.downlinkLow = downlinkLow;
            this.downlinkHigh = downlinkHigh;
            this.uplinkLow = uplinkLow;
            this.bandwidth = bandwidth;
        }
    }
}
