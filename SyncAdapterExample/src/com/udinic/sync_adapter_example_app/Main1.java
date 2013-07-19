package com.udinic.sync_adapter_example_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.udinic.sync_adapter_example.R;
import com.udinic.sync_adapter_example.authentication.AccountGeneral;
import com.udinic.sync_adapter_example_app.db.TvShowsContract;
import com.udinic.sync_adapter_example_app.db.dao.TvShow;
import com.udinic.sync_adapter_example_app.syncadapter.ParseComServerAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 21/03/13
 * Time: 13:50
 */
public class Main1 extends Activity {

    private String TAG = this.getClass().getSimpleName();
    private AccountManager mAccountManager;
    private String authToken = null;
    private Account mConnectedAccount;

    SyncStatusObserver syncObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(final int which) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshSyncStatus();
                }
            });
        }
    };

    Object handleSyncObserver;
    @Override
    protected void onResume() {
        super.onResume();
        handleSyncObserver = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE |
                ContentResolver.SYNC_OBSERVER_TYPE_PENDING, syncObserver);
    }

    @Override
    protected void onPause() {
        if (handleSyncObserver != null)
            ContentResolver.removeStatusChangeListener(handleSyncObserver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mAccountManager = AccountManager.get(this);

        findViewById(R.id.btnShowRemoteList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, List<TvShow>>() {

                    ProgressDialog progressDialog = new ProgressDialog(Main1.this);
                    @Override
                    protected void onPreExecute() {
                        if (authToken == null) {
                            Toast.makeText(Main1.this, "Please connect first", Toast.LENGTH_SHORT).show();
                            cancel(true);
                        } else {
                            progressDialog.show();
                        }
                    }

                    @Override
                    protected List<TvShow> doInBackground(Void... nothing) {
                        ParseComServerAccessor serverAccessor = new ParseComServerAccessor();
                        try {
                            return serverAccessor.getShows(authToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(List<TvShow> tvShows) {
                        progressDialog.dismiss();
                        if (tvShows != null) {
                            showOnDialog("Remote TV Shows", tvShows);
                        }
                    }
                }.execute();
            }
        });

        findViewById(R.id.btnAddShow).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String tvshowsNames[] = getResources().getStringArray(R.array.tvshows_names);
                int tvshowsYears[] = getResources().getIntArray(R.array.tvshows_year);
                int randIdx = new Random(currentTimeMillis()).nextInt(tvshowsNames.length);

                // Creating a Tv Show data object, in order to use some of its convenient methods
                TvShow tvShow = new TvShow(tvshowsNames[randIdx], tvshowsYears[randIdx]);
                Log.d("udinic", "Tv Show to add [id="+randIdx+"]: " + tvShow.toString());

                // Add our Tv show to the local data base. This normally should be done on a background thread
                getContentResolver().insert(TvShowsContract.CONTENT_URI, tvShow.getContentValues());

                Toast.makeText(Main1.this, "Added \"" + tvShow.toString() + "\"", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnShowLocalList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<TvShow> list = readFromContentProvider();
                AlertDialog.Builder builder = new AlertDialog.Builder(Main1.this);
                builder.setAdapter(new ArrayAdapter<TvShow>(Main1.this, android.R.layout.simple_list_item_1, list),null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        findViewById(R.id.btnClearLocalData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deleteing all the TV Shows on the DB. This normally should be done on a background thread
                int numDeleted = getContentResolver().delete(TvShowsContract.CONTENT_URI, null, null);
                Toast.makeText(Main1.this, "Deleted " + numDeleted + " TV shows from the local list", Toast.LENGTH_SHORT).show();
            }
        });


        /**
         *       Account stuff
         */

        findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTokenForAccountCreateIfNeeded(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        });

        findViewById(R.id.btnSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConnectedAccount == null) {
                    Toast.makeText(Main1.this, "Please connect first", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // Performing a sync no matter if it's off
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true); // Performing a sync no matter if it's off
                getContentResolver().requestSync(mConnectedAccount, TvShowsContract.AUTHORITY, bundle);
            }
        });

        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                invalidateAuthToken(new Account("udi@udinic.com", AccountGeneral.ACCOUNT_TYPE), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);

//                ContentResolver.cancelSync(mConnectedAccount, TvShowsContract.AUTHORITY);

//                Account account = mAccountManager.getAccountsByType("com.google")[0];
//                Bundle bundle = new Bundle();
//                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // Performing a sync no matter if it's off
//                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true); // Performing a sync no matter if it's off
//                getContentResolver().requestSync(account, "com.android.contacts", bundle);


//                ContentResolver.removePeriodicSync(mConnectedAccount, TvShowsContract.AUTHORITY, new Bundle());
//                ContentResolver.addPeriodicSync(mConnectedAccount, TvShowsContract.AUTHORITY, new Bundle(), 60*60);
            }
        });

        ((CheckBox)findViewById(R.id.cbIsSyncable)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mConnectedAccount == null) {
                    Toast.makeText(Main1.this, "Please connect first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Setting the syncable state of the sync adapter
                String authority = TvShowsContract.AUTHORITY;
                ContentResolver.setIsSyncable(mConnectedAccount, authority, isChecked ? 1 : 0);
            }
        });

        ((CheckBox)findViewById(R.id.cbAutoSync)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mConnectedAccount == null) {
                    Toast.makeText(Main1.this, "Please connect first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Setting the autosync state of the sync adapter
                String authority = TvShowsContract.AUTHORITY;
                ContentResolver.setSyncAutomatically(mConnectedAccount,authority, isChecked);
            }
        });

//        findViewById(R.id.btnSyncSetting).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (accountName == null) {
//                    Toast.makeText(Main1.this, "Please connect first", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                getContentResolver().addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE |
//                        ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
//                        ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, new SyncStatusObserver() {
//                    @Override
//                    public void onStatusChanged(int which) {
//                        Log.d("udinic", "TvShowsSyncAdapter status change ["+which+"]");
//                    }
//                });
//
//                Account account = new Account(accountName, AccountGeneral.ACCOUNT_TYPE);
//                String authority = TvShowsContract.AUTHORITY;
//                ContentResolver.setIsSyncable(account,authority, 0);
////                ContentResolver.setSyncAutomatically(account, authority,true);
////                ContentResolver.addPeriodicSync(account,authority, new Bundle(), 10);
//
////                ContentResolver.setIsSyncable(new Account(AccountGeneral.ACCOUNT_NAME, AccountGeneral.ACCOUNT_TYPE),
////                        TvShowsContract.AUTHORITY, 1);
////                getContentResolver().addPeriodicSync(new Account(AccountGeneral.ACCOUNT_NAME, AccountGeneral.ACCOUNT_TYPE),
////                        TvShowsContract.AUTHORITY,null,);
//            }
//        });
    }

    private void showOnDialog(String title, List<TvShow> tvShows) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main1.this);
        builder.setTitle(title);
        builder.setAdapter(new ArrayAdapter<TvShow>(Main1.this, android.R.layout.simple_list_item_1, tvShows),null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void refreshSyncStatus() {
        String status;

        if (ContentResolver.isSyncActive(mConnectedAccount, TvShowsContract.AUTHORITY))
            status = "Status: Syncing..";
        else if (ContentResolver.isSyncPending(mConnectedAccount, TvShowsContract.AUTHORITY))
            status = "Status: Pending..";
        else
            status = "Status: Idle";

        ((TextView) findViewById(R.id.status)).setText(status);
        Log.d("udinic", "refreshSyncStatus> " + status);
    }

    private void initButtonsAfterConnect() {
        String authority = TvShowsContract.AUTHORITY;

        // Get the syncadapter settings and init the checkboxes accordingly
        int isSyncable = ContentResolver.getIsSyncable(mConnectedAccount, authority);
        boolean autSync = ContentResolver.getSyncAutomatically(mConnectedAccount, authority);

        ((CheckBox)findViewById(R.id.cbIsSyncable)).setChecked(isSyncable > 0);
        ((CheckBox)findViewById(R.id.cbAutoSync)).setChecked(autSync);

        findViewById(R.id.cbIsSyncable).setEnabled(true);
        findViewById(R.id.cbAutoSync).setEnabled(true);
        findViewById(R.id.status).setEnabled(true);
        findViewById(R.id.btnShowRemoteList).setEnabled(true);
        findViewById(R.id.btnSync).setEnabled(true);
        findViewById(R.id.btnConnect).setEnabled(false);

        refreshSyncStatus();
    }

    private List<TvShow> readFromContentProvider() {
        Cursor curTvShows = getContentResolver().query(TvShowsContract.CONTENT_URI, null, null, null, null);

        ArrayList<TvShow> shows = new ArrayList<TvShow>();

        if (curTvShows != null) {
            while (curTvShows.moveToNext())
                shows.add(TvShow.fromCursor(curTvShows));
            curTvShows.close();
        }
        return shows;
    }

    /**
     * Get an auth token for the account.
     * If not exist - add it and then return its auth token.
     * If one exist - return its auth token.
     * If more than one exists - show a picker and return the select account's auth token.
     * @param accountType
     * @param authTokenType
     */
    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            String accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                            mConnectedAccount = new Account(accountName, AccountGeneral.ACCOUNT_TYPE);
                            authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                            initButtonsAfterConnect();
                            showMessage(((authToken != null) ? "SUCCESS!\ntoken: " + authToken : "FAIL"));
                            Log.d("udinic", "GetTokenForAccount Bundle is " + bnd);

                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
        , null);
    }

    private void showMessage(final String msg) {
        if (msg == null || msg.trim().equals(""))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }











    /**
     * Show all the accounts registered on the account manager. Request an auth token upon user select.
     * @param authTokenType
     */
    private void showAccountPicker(final String authTokenType, final boolean invalidate) {

        final Account availableAccounts[] = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }

            // Account picker
            new AlertDialog.Builder(this).setTitle("Pick Account").setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, name), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(invalidate)
                        invalidateAuthToken(availableAccounts[which], authTokenType);
                    else
                        getExistingAccountAuthToken(availableAccounts[which], authTokenType);
                }
            }).show();
        }
    }

    /**
     * Add new account to the account manager
     * @param accountType
     * @param authTokenType
     */
    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    showMessage("Account was created");
                    Log.d("udinic", "AddNewAccount Bundle is " + bnd);

                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }, null);
    }

    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    for (String key : bnd.keySet()) {
                        Log.d("udinic", "Bundle[" + key + "] = " + bnd.get(key));
                    }

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
                    Log.d("udinic", "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Invalidates the auth token for the account
     * @param account
     * @param authTokenType
     */
    private void invalidateAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null,null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    mAccountManager.invalidateAuthToken(account.type, authtoken);
                    showMessage(account.name + " invalidated");
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }


}
