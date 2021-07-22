package me.hgko.networkinfo.domain;

import android.annotation.TargetApi;
import android.os.Build;

import me.hgko.networkinfo.BuildConfig;

@TargetApi(Build.VERSION_CODES.M)
public class Constants {

    public static String PREFERENCE_KEY = "networkSignalInfo";
    public static String USER_ID = "userId";
    public static String LTE_MODE = "lte_mode";

    public static Integer SYNCHRONIZE_DELAY = 1000 * 30;
    public static Integer FLAG_START = 1;
    public static Integer FLAG_STOP = 0;

    public static String HOST = "localhost";
    public static Integer PORT = 7622;

    public static String DEF_DEST_IP = "49.175.97.214";
    public static String DEF_DEST_PORT = "3000";

    public static String YES = "Yes";
    public static String NO = "No";
    public static String MISSED_PERMISSION = "Required permissions not granted completely";
    public static String ASK_GRANT_MISSED_PERMISSIONS = "Do you want to grant missed permissions again?";
}
