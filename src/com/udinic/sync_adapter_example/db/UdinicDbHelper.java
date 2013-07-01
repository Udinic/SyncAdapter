package com.udinic.sync_adapter_example.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.udinic.sync_adapter_example.db.dao.TvShow;

import static com.udinic.sync_adapter_example.db.dao.TvShow.TABLE_NAME;

/**
 * Created by Udini on 6/22/13.
 */
public class UdinicDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "udinic.db";
    private static final int DATABASE_VERSION = 1;


    public UdinicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(TvShow.DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(UdinicDbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
