package me.hgko.networkinfo.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import me.hgko.networkinfo.nCubeThyme.service.INCubeService;
import me.hgko.networkinfo.nCubeThyme.service.NCubeService;
import me.hgko.networkinfo.nCubeThyme.setting.NCubeSettingDataShare;
import me.hgko.networkinfo.domain.Constants;
import me.hgko.networkinfo.util.CommonUtils;

public class NCubeConnectionService extends Service {

    private Context context;

    private static String nCubeSetupData = "";

    private boolean settingState = false;
    private boolean runningState = false;
    private boolean serviceConnection = false;

    private INCubeService nCubeService = null;
    private Thread thread;

    private String phoneNumber;

    private ServiceConnection serveConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            nCubeService = INCubeService.Stub.asInterface(service);
            Log.d("bindService", "onServiceConnected");
            serviceConnection = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("bindService", "onServiceDisconnected");
            serviceConnection = false;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();

        thread = new Thread() {
            @Override
            public void run() {
                if (!CommonUtils.checkPermission(getBaseContext())) {
                    TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                    phoneNumber = telephonyManager.getLine1Number();
                }

                setNCubeSetting();
                startNCube(context);
            }
        };
        thread.start();
        return START_STICKY;
    }

    /**
     * Share server configuration string to NCubeSettingDataShare object
     */
    private void setNCubeSetting() {
        NCubeSettingDataShare settingData = new NCubeSettingDataShare(setDefault());
        nCubeSetupData = settingData.configData;
        if (!nCubeSetupData.isEmpty()) {
            settingState = true;
        }
    }

    /**
     * Start NCube service
     *
     * @param context
     */
    public void startNCube(Context context) {
        /**
         * Check if server configuration is submitted and no connection with the server
         */
        if (settingState && !serviceConnection) {
            /**
             * Define NCubeDataShare object with the configData and put this object to Intent
             * Start NCubeService and Bind server connection
             */
//            if (isServiceRunning(context, NCubeConnectionService.class.getName())) {
//                destroyThread();
//            }

            Intent intent = new Intent(context, NCubeService.class);
            NCubeSettingDataShare regData = new NCubeSettingDataShare(nCubeSetupData);
            intent.putExtra("regData", regData);
            context.startService(intent);
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, NCubeService.class);
            context.bindService(serviceIntent, serveConn, Context.BIND_AUTO_CREATE);

            Log.d("networkLog", "startNCube");
            runningState = true;
        }
    }

    public static boolean isServiceRunning(Context ctx, String serviceClassName) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NCubeConnectionService.class.getName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    private void stopNCube(Context context) {
        if (settingState && runningState) {
            Intent stopIntent = new Intent();
            stopIntent.setClass(context, NCubeService.class);
            try {
                nCubeService.setStopService();
                context.unbindService(serveConn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            context.stopService(stopIntent);
            runningState = false;
            serviceConnection = false;
        }
    }

    private void checkNCube(Context context) {
        if (settingState && runningState) {
            boolean state = false;
            try {
                state = nCubeService.checkRunningState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (state) {
                runningState = true;
            }
        } else if (settingState && !runningState) {
            runningState = false;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        destroyThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyThread();
    }

    /**
     * Stop NCube service and Unbind service connection When service destroying
     */
    private void destroyThread() {
        stopNCube(context);
        if (runningState) {
            unbindService(serveConn);
            runningState = false;
        }
        if (thread != null) {
            if (!thread.isInterrupted()) {
                thread.interrupt();
            }
        }
    }

    private String setDefault() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString(Constants.USER_ID, "unknown user");

        if (phoneNumber == null) {
            phoneNumber = "wifi";
        }

        String defaultXML = String.format(
            "{\n" +
                "\"useprotocol\": \"http\",\n" +
                "\"cse\": {\n" +
                "\"cbhost\": \"125.138.91.207\",\n" +
                "\"cbport\": \"13008\",\n" +
                "\"cbname\": \"Mobius\",\n" +
                "\"cbcseid\": \"/Mobius\",\n" +
                "\"mqttport\": \"1883\"\n" +
                "},\n" +

                "\"ae\": {\n" +
                "\"aeid\": \"S\",\n" +
                "\"appid\": \"0.2.481.1.1\",\n" +
                "\"appname\": \"userID\",\n" +
                "\"appport\": \"9727\",\n" +
                "\"bodytype\": \"xml\",\n" +
                "\"tasport\": \"7622\"\n" +
                "},\n" +

                "\"cnt\": [\n" +
                "{\n" +
                "\"parentpath\": \"/userID\",\n" +
                "\"ctname\": \"Sensors\"\n" +
                "},\n" +

                "{\n" +
                "\"parentpath\": \"/userID\",\n" +
                "\"ctname\": \"LTE\"\n" +
                "},\n" +

                "{\n" +
                "\"parentpath\": \"/userID\",\n" +
                "\"ctname\": \"WIFI\"\n" +
                "},\n" +

                "{\n" +
                "\"parentpath\": \"/userID\",\n" +
                "\"ctname\": \"actuatorTest\"\n" +
                "}\n" +
                "],\n" +
                "\"sub\": [\n" +
                "{\n" +
                "\"parentpath\": \"/userID/actuatorTest\",\n" +
                "\"subname\": \"sub-ctrl\",\n" +
                "\"nu\": \"mqtt://AUTOSET\"\n" +
                "}\n" +
                "]\n" +
            "}\n",
        Constants.DEF_DEST_IP,
        Constants.DEF_DEST_PORT).replace("userID", phoneNumber.replace("+", "").trim());
        Log.d("networkLog", defaultXML);
        return defaultXML;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
