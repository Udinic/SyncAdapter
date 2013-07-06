package com.udinic.sync_adapter_example_app.db.dao;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;

/**
 * This class represent a TV Show.
 *
 * Created by Udini on 6/22/13.
 */
public class TvShow implements Serializable {

    // DB Table consts
    public static final String TABLE_NAME = "tvshows";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_YEAR = "year";

    // Fields
    public String name;
    public int year;

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "(" +
            COL_ID + " integer primary key autoincrement, " +
            COL_NAME + " text not null, " +
            COL_YEAR + " integer " +
            ");";

    public TvShow(String name, int year) {
        this.name = name;
        this.year = year;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_YEAR, year);
        return values;
    }

    // Create a TvShow object from a cursor
    public static TvShow fromCursor(Cursor curTvShows) {
        String name = curTvShows.getString(curTvShows.getColumnIndex(COL_NAME));
        int year = curTvShows.getInt(curTvShows.getColumnIndex(COL_YEAR));

        return new TvShow(name, year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TvShow tvShow = (TvShow) o;

        if (year != tvShow.year) return false;
        if (!name.equals(tvShow.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + year;
        return result;
    }

    @Override
    public String toString() {
        return name + " (" + year + ")";
    }
}
