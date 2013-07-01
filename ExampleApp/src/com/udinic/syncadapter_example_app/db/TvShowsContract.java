package com.udinic.syncadapter_example_app.db;

import android.net.Uri;

/**
 * Created by Udini on 7/2/13.
 */
public class TvShowsContract {

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.udinic.tvshow";
    public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.udinic.tvshow";

    // content://<authority>/<path to type>
    public static final Uri CONTENT_URI = Uri.parse("content://com.udinic.tvshows.provider/tvshows");
}
