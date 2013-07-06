package com.udinic.sync_adapter_example_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.udinic.sync_adapter_example.authentication.AccountGeneral;
import com.udinic.sync_adapter_example.authentication.ParseComServer;
import com.udinic.sync_adapter_example_app.db.TvShowsContract;
import com.udinic.sync_adapter_example_app.db.UdinicDbHelper;
import com.udinic.sync_adapter_example_app.db.dao.TvShow;
import com.udinic.sync_adapter_example_app.db.dao.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Udini on 6/21/13.
 */
public class Testing extends Activity {

    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UdinicDbHelper dbHelper = new UdinicDbHelper(this);
        mDb = dbHelper.getWritableDatabase();
//        mDb.execSQL("DELETE FROM " + TABLE_NAME); //clean up the table

//        writeToDB(new TvShow("Local1", 3244));
//        writeToDB(new TvShow("Local2", 8789));
//        writeToDB(new TvShow("Local3", 789));
//        getContentResolver().insert(TvShow.CONTENT_URI, new TvShow("New Show for Content Provider", 2013).getContentValues());
//        putOnWeb();
        getFromWeb();
//        putOnWebBulk();

        List<TvShow> shows = readFromContentProvider();
        for (TvShow show : shows) {
            Log.d("udini", "show["+show.name+"] year["+show.year+"]");
        }
    }

    private boolean writeToDB(TvShow tvShow) {
        return mDb.insert(TvShow.TABLE_NAME, null, tvShow.getContentValues()) == 1;
    }

    private List<TvShow> readFromContentProvider() {
        Cursor curTvShows = getContentResolver().query(TvShowsContract.CONTENT_URI, null, null, null, null);

        ArrayList<TvShow> shows = new ArrayList<TvShow>();

        if (curTvShows != null) {
            while (curTvShows.moveToNext()) {
                shows.add(TvShow.fromCursor(curTvShows));
            }
            curTvShows.close();
        }
        return shows;

    }
    private List<TvShow> readFromDb() {
        Cursor curTvShows = mDb.query(TvShow.TABLE_NAME, null, null, null, null, null, null);
        ArrayList<TvShow> shows = new ArrayList<TvShow>();

        while (curTvShows.moveToNext()) {
            shows.add(TvShow.fromCursor(curTvShows));
        }

        curTvShows.close();
        return shows;
    }

    private void putOnWeb() {

        final ParseComServer udini = new ParseComServer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = udini.userSignIn("qqq", "qqq", "");
//                    udini.putShow(user.getSessionToken(), user.getObjectId(), new TvShow("From Local3", 2012));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    private void getFromWeb() {

        final AccountManagerFuture<Bundle> future = AccountManager.get(this).getAuthToken(new Account("qqq", AccountGeneral.ACCOUNT_TYPE),
                AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,null,this,null,null);

        final ParseComServer udini = new ParseComServer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
//                    User user = udini.userSignIn("qqq", "qqq", "");

//                    List<TvShow> shows = udini.getShows(authtoken);
//                    for (TvShow show : shows)
//                        Log.d("udini", "getFromWeb> show[" + show.toString() + "]");

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }
//    private void putOnWebBulk() {
//        final ParseComServer udini = new ParseComServer();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    User user = udini.userSignIn("qqq", "qqq", "");
//
//                    List<TvShow> shows = new ArrayList<TvShow>();
//                    shows.add(new TvShow("From Local1", 2012));
//                    shows.add(new TvShow("From Local2", 2013));
//
//                    udini.putShows(user, shows);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }).start();
//
//    }
}
