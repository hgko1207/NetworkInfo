package me.hgko.networkinfo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.hgko.networkinfo.db.DBHandler;
import me.hgko.networkinfo.domain.Constants;
import me.hgko.networkinfo.domain.NetworkInfo;
import me.hgko.networkinfo.manager.DataManager;

import static me.hgko.networkinfo.util.CommonUtils.checkInternetConnection;

public class SynchronizeService extends Service {

    private final DataManager dataManager = DataManager.getInstance();

    private Timer timer;
    private DBHandler dbHandler;
    private int mode;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mode = dataManager.getMode();
        dbHandler = new DBHandler(getBaseContext());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        new SynchronizeTask().execute();
                    }
                }, 0, getSynchronizeDelay());
            }
        }, 1000 * 60);

        return START_STICKY;
    }

    private int getSynchronizeDelay() {
        switch (mode) {
            case 1:
            case 2:
                return 1000 * 15;
            case 3:
                return 1000 * 3;
            case 4:
                return 1000 * 3;
            default:
                return 1000 * 10;
        }
    }

    private class SynchronizeTask extends AsyncTask<SensorEvent, Void, String> {
        @Override
        protected String doInBackground(SensorEvent... events) {
            return (checkInternetConnection(getBaseContext())) ? sendToServer() : null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                Toast.makeText(getBaseContext(), response, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String sendToServer() {
        try {
            JSONArray jsonArray = new JSONArray();

            List<NetworkInfo> networkInfos = dbHandler.selectInfo();
            if (networkInfos.size() > 0) {
                for (NetworkInfo info : networkInfos) {
                    JSONObject object = new JSONObject();
                    object.put("ctname", info.getCtname());
                    object.put("con", info.getCon());
                    jsonArray.put(object);
                }

                Log.d("networkLog", "sendToServer => " + jsonArray.toString());

                Socket socket = new Socket(Constants.HOST, Constants.PORT);
                if (socket != null) {
                    if (socket.isConnected()) {
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                        JSONObject contentObj = new JSONObject();
                        contentObj.put("jsonArray", jsonArray);
                        writer.println(contentObj.toString());

//                    Log.d("bservice", contentObj.toString());
                    } else {
                        Log.d("bservice", "[&CubeThyme] TAS connection is closed\n");
                    }
                    socket.close();
                }

                dbHandler.deleteAll();
                return "success";
            }
        } catch (Exception e) {
//            Log.d("ex", "Error occured while syncing: " + e.getMessage());
            return "wait";
        }
        return "no data";
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("networkLog", "SynchronizeService onTaskRemoved()");
        if (timer != null) {
            timer.cancel();
        }

        dbHandler.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("networkLog", "SynchronizeService onDestroy()");
        if (timer != null) {
            timer.cancel();
        }

        dbHandler.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

