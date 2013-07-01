/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.udinic.syncadapter_example_app.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.udinic.sync_adapter_example.authentication.AccountGeneral;
import com.udinic.sync_adapter_example.authentication.ParseComServer;
import com.udinic.syncadapter_example_app.db.TvShowsContract;
import com.udinic.syncadapter_example_app.db.dao.TvShow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    private final AccountManager mAccountManager;
    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {

        Log.d("udinic", TAG + "> onPerformSync account["+account.toString()+"]");

        // TODO investigate third arg
        try {
            String authToken = mAccountManager.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
            String userObjectId = mAccountManager.getUserData(account, AccountGeneral.USERDATA_USER_OBJ_ID);
//            String userObjectId = "A6onlmHup4"; //TODO mAccountManager.getUserData(account, AccountGeneral.USERDATA_USER_OBJ_ID);

            Log.d("udini", "onPerformSync> userObjId[" + userObjectId + "]");
            ParseComServer parseService = new ParseComServer();
            // Get shows from remote
            List<TvShow> remoteTvShows = parseService.getShows(authToken);

            // Get shows from local
            ArrayList<TvShow> localTvShows = new ArrayList<TvShow>();
            Cursor curTvShows = getContext().getContentResolver().query(TvShowsContract.CONTENT_URI, null, null, null, null);
            if (curTvShows != null) {
                while (curTvShows.moveToNext()) {
                    localTvShows.add(TvShow.fromCursor(curTvShows));
                }
                curTvShows.close();
            }

            // TODO REMOVE!
//            localTvShows = new ArrayList<TvShow>();
//            remoteTvShows = new ArrayList<TvShow>();

            // See what Local shows are missing on Remote
            ArrayList<TvShow> showsToRemote = new ArrayList<TvShow>();
            for (TvShow localTvShow : localTvShows) {
                if (!remoteTvShows.contains(localTvShow))
                    showsToRemote.add(localTvShow);
            }

            // See what Remote shows are missing on Local
            ArrayList<TvShow> showsToLocal = new ArrayList<TvShow>();
            for (TvShow remoteTvShow : remoteTvShows) {
                if (!localTvShows.contains(remoteTvShow))
                    showsToLocal.add(remoteTvShow);
            }

            // Updating remote tv shows
            for (TvShow remoteTvShow : showsToRemote) {
                Log.d("udinic", "Local -> Remote [" + remoteTvShow.name + "]");
                parseService.putShow(authToken, userObjectId, remoteTvShow);
            }

            // Updating local tv shows
            int i=0;
            ContentValues showsToLocalValues[] = new ContentValues[showsToLocal.size()];
            for (TvShow localTvShow : showsToLocal) {
                Log.d("udinic", "Remote -> Local [" + localTvShow.name + "]");
                showsToLocalValues[i++] = localTvShow.getContentValues();
            }
            getContext().getContentResolver().bulkInsert(TvShowsContract.CONTENT_URI, showsToLocalValues);

        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

