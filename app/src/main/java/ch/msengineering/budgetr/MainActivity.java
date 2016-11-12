package ch.msengineering.budgetr;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ch.msengineering.budgetr.data.AuthenticatorService;
import ch.msengineering.budgetr.data.BudgetrContract;


public class MainActivity extends AppCompatActivity {

    // Sync interval constants
    private static final long SYNC_FREQUENCY = 60; //* 60;  // 1 hour (in seconds)

    // Constant for check if setup of syncadapter complete
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    // The authority for the sync adapter's content provider
    private static final String CONTENT_AUTHORITY = BudgetrContract.CONTENT_AUTHORITY;

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "budgetr.msengineering.ch";

    // Dummy account on device for syncadapter
    Account mAccount;

    // Content Resolver
    ContentResolver mResolver;

    // Content Observer for reacting on changes
    LocalTableObserver mObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button bSignup = (Button) findViewById(R.id.bSignup);

        bSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                MainActivity.this.startActivity(registerIntent);
            }
        });

        final Button bSignin = (Button) findViewById(R.id.bSignin);

        bSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(registerIntent);
            }
        });

        // Create the dummy account
        mAccount = CreateSyncAccount(this);

        // Create Content Resolver
        mResolver = getContentResolver();

        //TODO Create it in the onResume()-Method, but it isn't existing...
        /*
         * Create a content observer object.
         * Its code does not mutate the provider, so set
         * selfChange to "false"
         */
        mObserver = new LocalTableObserver(new Handler());

        /*
         * Register the observer for the data table. The table's path
         * and any of its subpaths trigger the observer.
         */
        mResolver.registerContentObserver(BudgetrContract.SalaryEntry.CONTENT_URI, true, mObserver);

        /*
         * Register the observer for the data table. The table's path
         * and any of its subpaths trigger the observer.
         */
        mResolver.registerContentObserver(BudgetrContract.ExpenditureEntry.CONTENT_URI, true, mObserver);
    }

    //TODO Implement method to unregister, when not active.
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mResolver.unregisterContentObserver(mObserver);
//    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
     public static Account CreateSyncAccount(Context context) {
         boolean newAccount = false;
         boolean setupComplete = PreferenceManager
                 .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

         // Create account, if it's missing. (Either first run, or user has deleted account.)
         Account mAccount = AuthenticatorService.GetAccount(ACCOUNT_TYPE);
         AccountManager accountManager =
                 (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
         if (accountManager.addAccountExplicitly(mAccount, null, null)) {
             // Inform the system that this account supports sync
             ContentResolver.setIsSyncable(mAccount, CONTENT_AUTHORITY, 1);
             // Inform the system that this account is eligible for auto sync when the network is up
             ContentResolver.setSyncAutomatically(mAccount, CONTENT_AUTHORITY, true);
             // Recommend a schedule for automatic synchronization. The system may modify this based
             // on other scheduled syncs and network utilization.
             ContentResolver.addPeriodicSync(
                     mAccount, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
             newAccount = true;
         }

         // Schedule an initial sync if we detect problems with either our account or our local
         // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
         // the account list, so wee need to check both.)
         if (newAccount || !setupComplete) {
             TriggerRefresh();
             PreferenceManager.getDefaultSharedPreferences(context).edit()
                     .putBoolean(PREF_SETUP_COMPLETE, true).apply();
         }
         return mAccount;
     }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void TriggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                AuthenticatorService.GetAccount(ACCOUNT_TYPE), // Sync account
                BudgetrContract.CONTENT_AUTHORITY,                 // Content authority
                b);                                             // Extras
    }

    /***
     * Class for observing changing data in database and upload it whenn changed to server.
     */
    public class LocalTableObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public LocalTableObserver(Handler handler) {
            super(handler);
        }

        /*
         * Define a method that's called when data in the
         * observed content provider changes.
         * This method signature is provided for compatibility with
         * older platforms.
         */
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        /*
         * Define a method that's called when data in the
         * observed content provider changes.
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            Bundle b = new Bundle();
            // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            ContentResolver.requestSync(mAccount, CONTENT_AUTHORITY, b);

        }
    }

}