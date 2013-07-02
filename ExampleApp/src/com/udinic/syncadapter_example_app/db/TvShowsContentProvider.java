package com.udinic.syncadapter_example_app.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.udinic.syncadapter_example_app.db.dao.TvShow;

/**
 * Created by Udini on 6/22/13.
 */
public class TvShowsContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.udinic.tvshows.provider";
    public static final UriMatcher URI_MATCHER = buildUriMatcher();
    public static final String PATH = "tvshows";
    public static final int PATH_TOKEN = 100;
    public static final String PATH_FOR_ID = "tvshows/*";
    public static final int PATH_FOR_ID_TOKEN = 200;

    // Uri Matcher for the content provider
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AUTHORITY;
        matcher.addURI(authority, PATH, PATH_TOKEN);
        matcher.addURI(authority, PATH_FOR_ID, PATH_FOR_ID_TOKEN);
        return matcher;
    }

    //TODO organize the code. Maybe write a Contract class?
    // Content Provider stuff

    private UdinicDbHelper restaurantDb;

    @Override
    public boolean onCreate() {
        Context ctx = getContext();
        restaurantDb = new UdinicDbHelper(ctx);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case PATH_TOKEN:
                return TvShowsContract.CONTENT_TYPE_DIR;
            case PATH_FOR_ID_TOKEN:
                return TvShowsContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = restaurantDb.getWritableDatabase();
        int token = URI_MATCHER.match(uri);
        switch (token) {
            case PATH_TOKEN: {
                long id = db.insert(TvShow.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return TvShowsContract.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
            }
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = restaurantDb.getReadableDatabase();
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            // retrieve tv shows list
            case PATH_TOKEN: {
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(TvShow.TABLE_NAME);
                return builder.query(db, null, null, null, null, null, null);
            }
            default:
                return null;
        }
    }

    /**
     * Man..I'm tired..
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}