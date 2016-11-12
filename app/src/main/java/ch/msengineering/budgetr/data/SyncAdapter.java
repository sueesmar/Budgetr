/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.msengineering.budgetr.data;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.msengineering.budgetr.LoginActivity;
import ch.msengineering.budgetr.LoginRequest;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * URL for base directory of remote DB-Server
     */
//    private static final String BASE_URL_REMOTE_SERVER = "http://192.168.10.109/budgetr/";
    private static final String BASE_URL_REMOTE_SERVER = "http://whitehat.ch/";

    /**
     * URL to send expenditure to during a sync.
     */
    private static final String REQUEST_URL_SAVE_EXPENDITURE = BASE_URL_REMOTE_SERVER + "SaveExpenditure.php";

    /**
     * URL to send salary to during a sync.
     */
    private static final String REQUEST_URL_SAVE_SALARY =  BASE_URL_REMOTE_SERVER + "SaveSalary.php";

    /**
     * URL to request element count of expenditure during a sync.
     */
    private static final String REQUEST_URL_GET_COUNT_EXPENDITURE =  BASE_URL_REMOTE_SERVER + "GetCountExpenditure.php";

    /**
     * URL to request element count of salary during a sync.
     */
    private static final String REQUEST_URL_GET_COUNT_SALARY =  BASE_URL_REMOTE_SERVER + "GetCountSalary.php";

    /**
     * URL to request element count of salary during a sync.
     */
    private static final String REQUEST_URL_DELETE_SALARY =  BASE_URL_REMOTE_SERVER + "DeleteSalary.php";

    /**
     * URL to request element count of salary during a sync.
     */
    private static final String REQUEST_URL_DELETE_EXPENDITURE =  BASE_URL_REMOTE_SERVER + "DeleteExpenditure.php";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[] {
            BudgetrContract.ExpenditureEntry._ID,
            BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT,
            BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE,
            BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION,
            BudgetrContract.ExpenditureEntry.COLUMN_NAME_PAYMENTMETHODE,
            BudgetrContract.ExpenditureEntry.COLUMN_NAME_RECEIPT,
            BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE};

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /***
     * Send the local table data to web-interface, which stores each element on the MySQL-Table.
     * @param localDataArray {@link JSONArray} with all the local table elements.
     * @param requestURL URL to send request to, depending if sending expenditure or earnings.
     */
    public void sendDataToServer(final JSONArray localDataArray, String requestURL) {

        // Response received from the server
        Response.Listener<JSONArray> responseListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonResponse) {

                Log.i(TAG,"Request: " + localDataArray.toString());
                Log.i(TAG,"Response: "+ jsonResponse.toString());

                try {
                    boolean success = jsonResponse.getJSONObject(0).getBoolean("success");

                    if (success) {
                        Log.i(TAG, "Response, successfull");

                        //Code when response is successfull, till now > nothing.

                    } else {
                        //TODO Dialog for sync-Problem.
                        /*
                         * Handling of Error in Sync-Problem, but not working yet.
                         *
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Sync failed")
                                .setNegativeButton("OK", null)
                                .create()
                                .show();
                        */
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, error.toString());
            }
        };

        //Create the request, send it to the specified URL and get die answer.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, requestURL, localDataArray, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        queue.add(jsonArrayRequest);
    }

    /***
     * Truncates the table in the requestURL on remote database.
     * @param requestURL URL to web-service to delete the table.
     */
    public void deleteDataFromServer(String requestURL){
        // Response received from the server
        Response.Listener<JSONArray> responseListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonResponse) {

                try {
                    boolean success = jsonResponse.getJSONObject(0).getBoolean("success");

                    if (success) {
                        Log.i(TAG, "Response, successfull");

                        //Code when response is successfull, till now > nothing.

                    } else {
                        //TODO Dialog for sync-Problem.
                        /*
                         * Handling of Error in Sync-Problem, but not working yet.
                         *
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Sync failed")
                                .setNegativeButton("OK", null)
                                .create()
                                .show();
                        */
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, error.toString());
            }
        };

        //Create the request, send it to the specified URL and get die answer.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, requestURL, null, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        queue.add(jsonArrayRequest);
    }

    /***
     * Gets the number of elements of the requested table from the remote database.
     * @param requestURL URL to the web-interface for the requested table.
     * @return Number of elements in table.
     */
    public void getElementCountRemoteDb(final RemoteServerCallback callback, String requestURL){

        final int[] countRemoteDb = new int[1];

        // Response received from the server
        Response.Listener<JSONArray> responseListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonResponse) {

                try {
                    countRemoteDb[0] = jsonResponse.getJSONObject(0).getInt("count");
                    callback.onSuccess(countRemoteDb[0]);
                    //Log.i(TAG, "Count on Remote DB: " + countRemoteDb[0]);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };


        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, error.toString());
            }
        };

        //Create the request, send it to the specified URL and get die answer.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, requestURL, null, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        queue.add(jsonArrayRequest);
    }

    /***
     * Interface for getting the result of the volley HTTP-Request to the onPerform-Method
     */
    public interface RemoteServerCallback{
        void onSuccess(int result);
    }

    /***
     * Gets the number of entries in the expenditure table of the local sqlite database.
     * @return Number of entries.
     */
    public int getExpenditureCountLocalDb(){

        String[] projection = {
                BudgetrContract.ExpenditureEntry._ID,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT};

        Cursor cursor = mContentResolver.query(
                BudgetrContract.ExpenditureEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    /***
     * Gets the number of entries in the earnings table of the local sqlite database.
     * @return Number of entries
     */
    public int getEarningsCountLocalDb(){

        String[] projection = {
                BudgetrContract.SalaryEntry._ID,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT};

        Cursor cursor = mContentResolver.query(
                BudgetrContract.SalaryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    /***
     * Read all data from expenditureTable and create {@link JSONArray}.
     * @return All data in one {@link JSONArray}
     */
    public JSONArray getExpenditureTableInJsonArray(){

        String[] projection = {
                BudgetrContract.ExpenditureEntry._ID,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT};

        Cursor cursor = mContentResolver.query(
                BudgetrContract.ExpenditureEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        // Find the columns of expenditure attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry._ID);
        int descriptionColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION);
        int dateColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE);
        int placeColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE);
        int valueColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT);

        // Variables for sending to server
        int expenditureId;
        String expenditureDescription;
        String expenditureDate;
        String expenditurePlace;
        double expenditureValue;

        JSONArray expenditureArray = new JSONArray();

        if (cursor != null && cursor.getCount() > 0) {

            while (!cursor.isAfterLast()) {

                // Read the expenditure attributes from the Cursor for the current expenditure
                expenditureId = cursor.getInt(idColumnIndex);
                expenditureDescription = cursor.getString(descriptionColumnIndex);
                expenditureDate = cursor.getString(dateColumnIndex);
                expenditurePlace = cursor.getString(placeColumnIndex);
                expenditureValue = cursor.getDouble(valueColumnIndex);

                JSONObject currentExpenditure = new JSONObject();

                try {
                    currentExpenditure.put("idexpenditure", expenditureId);
                    currentExpenditure.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION, expenditureDescription);
                    currentExpenditure.put("expenditureDate", expenditureDate);
                    currentExpenditure.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE, expenditurePlace);
                    currentExpenditure.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT, expenditureValue);

                    //Add the values of the current JSONObject to the JSONArray
                    expenditureArray.put(currentExpenditure);

                    cursor.moveToNext();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        return expenditureArray;
    }

    /***
     * Read all data from salaryTable and create {@link JSONArray}.
     * @return All data in one {@link JSONArray}
     */
    public JSONArray getEarningsTableInJsonArray(){

        String[] projection = {
                BudgetrContract.SalaryEntry._ID,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE};

        Cursor cursor = mContentResolver.query(
                BudgetrContract.SalaryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        // Find the columns of expenditure attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry._ID);
        int valueColumnIndex = cursor.getColumnIndex(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT);
        int dateColumnIndex = cursor.getColumnIndex(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE);

        // Variables for sending to server
        int salaryId;
        String salaryDate;
        double salaryValue;

        JSONArray earningArray = new JSONArray();

        if (cursor != null && cursor.getCount() > 0) {

            while (!cursor.isAfterLast()) {

                // Read the expenditure attributes from the Cursor for the current expenditure
                salaryId = cursor.getInt(idColumnIndex);
                salaryDate = cursor.getString(dateColumnIndex);
                salaryValue = cursor.getDouble(valueColumnIndex);

                JSONObject currentEarning = new JSONObject();

                try {
                    currentEarning.put("idsalary", salaryId);
                    currentEarning.put(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE, salaryDate);
                    currentEarning.put(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT, salaryValue);

                    //Add the values of the current JSONObject to the JSONArray
                    earningArray.put(currentEarning);

                    cursor.moveToNext();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        Log.i(TAG, "EarningArray: "+ earningArray.toString());

        return earningArray;
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");

        //Get count for local database on device
        int countLocalExpenditures = getExpenditureCountLocalDb();
        int countLocalEarnings = getEarningsCountLocalDb();

        Log.i(TAG, "Expenditure local: " + countLocalExpenditures + ", Earnings  local: " + countLocalEarnings);

        //Get count of remote entries for expenditures
        final int countRemoteExpenditures[] = new int[1];
        getElementCountRemoteDb(new RemoteServerCallback() {
            @Override
            public void onSuccess(int result) {
                countRemoteExpenditures[0] = result;
            }
        }, REQUEST_URL_GET_COUNT_EXPENDITURE);

        //Get count of remote entries for salary
        final int countRemoteEarnings[] = new int[1];
        getElementCountRemoteDb(new RemoteServerCallback() {
            @Override
            public void onSuccess(int result) {
                countRemoteEarnings[0] = result;
                Log.i(TAG, "Expenditure remote: " + countRemoteExpenditures[0] + ", Earnings remote: " + countRemoteEarnings[0]);
            }
        }, REQUEST_URL_GET_COUNT_SALARY);

        //Wait for answer of remoteCount to be populated back to local variables
        try {
            Thread.sleep(3000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        //Store response in local variables, so we can use them in following checks of difference of
        //remote and local count
        int remoteCountExpenditures = countRemoteExpenditures[0];
        int remoteCountEarnings = countRemoteEarnings[0];

        //send data to server if count is not equal
        if(countLocalEarnings != remoteCountEarnings){

            //Delete all entries on remote server
            deleteDataFromServer(REQUEST_URL_DELETE_SALARY);
            Log.i(TAG, "Salary table deleted.");

            //All earning elements from local database in JSONArray
            JSONArray earningArray = getEarningsTableInJsonArray();

            sendDataToServer(earningArray, REQUEST_URL_SAVE_SALARY);
        }

        if(countLocalExpenditures != remoteCountExpenditures) {

            //Delete all entries on remote server
            deleteDataFromServer(REQUEST_URL_DELETE_EXPENDITURE);
            Log.i(TAG, "Expenditure table deleted.");

            //All expenditure elements from local database in JSONArray
            JSONArray expenditureArray = getExpenditureTableInJsonArray();

            sendDataToServer(expenditureArray, REQUEST_URL_SAVE_EXPENDITURE);
        }

        Log.i(TAG, "Network synchronization complete");
    }
}
