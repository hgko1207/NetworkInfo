package me.hgko.networkinfo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.hgko.networkinfo.domain.NetworkInfo;

/**
 * Created by inspace on 2018-09-20.
 */

public class DBHandler {

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandler(Context context) {
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * LTE, WiFi 정보 추가
     * @param content
     * @return
     */
    public boolean inputData(String ctname, String content) {
        String sql = "SELECT * FROM network WHERE ctname = '" + ctname + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (!cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("datetime", new Date().getTime());
            values.put("ctname", ctname);
            values.put("con", content);
            return db.insert("network", null, values) > 0;
        } else {
            ContentValues values = new ContentValues();
            values.put("datetime", new Date().getTime());
            values.put("con", content);
            return db.update("network", values, "ctname = ?", new String[]{ctname}) > 0;
        }
    }

    public List<NetworkInfo> selectInfo() {
        String sql = "SELECT * FROM network";
        Cursor cursor = db.rawQuery(sql, null);

        List<NetworkInfo> networkInfos = new ArrayList<>();
        while (cursor.moveToNext()) {
            NetworkInfo info = new NetworkInfo();
            info.setDatetime(cursor.getString(1));
            info.setCtname(cursor.getString(2));
            info.setCon(cursor.getString(3));
            networkInfos.add(info);
        }

        return networkInfos;
    }

    /**
     * 정보 삭제
     * @param ctname
     * @return
     */
    public boolean deleteData(String ctname) {
        return db.delete("network", "ctname = ?", new String[]{ctname}) > 0;
    }

    /**
     * 모든 정보 삭제
     * @return
     */
    public boolean deleteAll() {
        return db.delete("network", null, null) > 0;
    }
}
