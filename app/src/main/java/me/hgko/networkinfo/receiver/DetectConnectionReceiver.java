package me.hgko.networkinfo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import me.hgko.networkinfo.service.NCubeConnectionService;

import static me.hgko.networkinfo.util.CommonUtils.checkInternetConnection;
import static me.hgko.networkinfo.util.CommonUtils.startServiceOnce;
import static me.hgko.networkinfo.util.CommonUtils.stopServiceOnce;

public class DetectConnectionReceiver extends BroadcastReceiver {
    /**
     * Listening for internet connection state
     * if there is no available internet connection then stopping Bind Service otherwise starting that Service again
     * Because The Bind service runs in foreground and lasts long time
     * that means battery consumption will be increased, so we need to stop foreground services for reducing battery consumption
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("networkLog", "DetectConnectionReceiver");
        if (checkInternetConnection(context)) {
            startServiceOnce(NCubeConnectionService.class, context);
        } else {
            stopServiceOnce(NCubeConnectionService.class, context);
        }
    }
}
