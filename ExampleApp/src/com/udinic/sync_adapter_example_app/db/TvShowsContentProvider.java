package com.udinic.sync_adapter_example_app.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import static com.udinic.sync_adapter_example_app.db.TvShowsContract.AUTHORITY;

/**
 * Created by Udini on 6/22/13.
 */
public class TvShowsContentProvider extends ContentProvider {

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

    // Content Provider stuff

    private TvShowsDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        Context ctx = getContext();
        dbHelper = new TvShowsDbHelper(ctx);
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
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            // retrieve tv shows list
            case PATH_TOKEN: {
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(TvShowsDbHelper.TVSHOWS_TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case PATH_FOR_ID_TOKEN: {
                int tvShowId = (int)ContentUris.parseId(uri);
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(TvShowsDbHelper.TVSHOWS_TABLE_NAME);
                builder.appendWhere(TvShowsDbHelper.TVSHOWS_COL_ID + "=" + tvShowId);
                return builder.query(db, projection, selection,selectionArgs, null, null, sortOrder);
            }
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = URI_MATCHER.match(uri);
        switch (token) {
            case PATH_TOKEN: {
                long id = db.insert(TvShowsDbHelper.TVSHOWS_TABLE_NAME, null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return TvShowsContract.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = URI_MATCHER.match(uri);
        int rowsDeleted = -1;
        switch (token) {
            case (PATH_TOKEN):
                rowsDeleted = db.delete(TvShowsDbHelper.TVSHOWS_TABLE_NAME, selection, selectionArgs);
                break;
            case (PATH_FOR_ID_TOKEN):
                String tvShowIdWhereClause = TvShowsDbHelper.TVSHOWS_COL_ID + "=" + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    tvShowIdWhereClause += " AND " + selection;
                rowsDeleted = db.delete(TvShowsDbHelper.TVSHOWS_TABLE_NAME, tvShowIdWhereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // Notifying the changes, if there are any
        if (rowsDeleted != -1)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    /**
     * Man..I'm tired..
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}