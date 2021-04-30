package me.hgko.networkinfo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by inspace on 2018-09-20.
 */

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "networkSignal.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE network (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "datetime TEXT NOT NULL, " +
                "ctname TEXT NOT NULL, " +
                "con TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
