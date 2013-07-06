package com.udinic.sync_adapter_example_app.db;

import android.net.Uri;

/**
 * Holds the API to the content provider
 *
 * Created by Udini on 7/2/13.
 */
public class TvShowsContract {

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.udinic.tvshow";
    public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.udinic.tvshow";

    public static final String AUTHORITY = "com.udinic.tvshows.provider";
    // content://<authority>/<path to type>
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/tvshows");
}
