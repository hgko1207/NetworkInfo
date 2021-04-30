package me.hgko.networkinfo.manager;

import lombok.Data;
import me.hgko.networkinfo.domain.LteSignalInfo;
import me.hgko.networkinfo.domain.WifiDataInfo;

/**
 * Created by inspace on 2018-10-18.
 */
@Data
public class DataManager {

    private static DataManager instance;

    private DataManager() {
    }

    public static synchronized DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    private int mode;

    private LteSignalInfo lteSignalInfo;

    private WifiDataInfo wifiDataInfo;
}
