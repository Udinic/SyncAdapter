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
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.udinic.sync_adapter_example.authentication.AccountGeneral;
import com.udinic.sync_adapter_example_app.db.TvShowsContract;
import com.udinic.sync_adapter_example_app.db.dao.TvShow;
import com.udinic.sync_adapter_example_app.syncadapter.ParseComServerAccessor;

import java.util.ArrayList;
import java.util.List;

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
    private String accountName;

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


        /**
         *       Account stuff
         */

        findViewById(R.id.btnSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accountName == null) {
                    Toast.makeText(Main1.this, "Please connect first", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // Performing a sync no matter if it's off
                getContentResolver().requestSync(new Account(accountName, AccountGeneral.ACCOUNT_TYPE),
                        TvShowsContract.AUTHORITY, bundle);
            }
        });

        findViewById(R.id.btnGetAuthTokenConvenient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTokenForAccountCreateIfNeeded(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        });

//        findViewById(R.id.btnAddAccount).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addNewAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
//            }
//        });
//
//        findViewById(R.id.btnGetAuthToken).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showAccountPicker(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
//            }
//        });

//

//        findViewById(R.id.btnInvalidateAuthToken).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showAccountPicker(AUTHTOKEN_TYPE_FULL_ACCESS, true);
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
                            accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                            authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
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
