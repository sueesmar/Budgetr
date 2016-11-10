package ch.msengineering.budgetr;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.msengineering.budgetr.data.BudgetrContract;

public class ListEarningsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the earnings data loader */
    private static final int EARNINGS_LOADER = 0;

    /** Adapter for the ListView */
    EarningsCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_earnings);

        // Setup FAB to open EditorEarningsActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_earnings);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListEarningsActivity.this, EditorEarningsActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the earning data
        ListView earningsListView = (ListView) findViewById(R.id.list_earnings);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.earnings_empty_view);
        earningsListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of earning data in the Cursor.
        // There is no earning data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new EarningsCursorAdapter(this, null);
        earningsListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        earningsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorEarningsActivity}
                Intent intent = new Intent(ListEarningsActivity.this, EditorEarningsActivity.class);

                // Form the content URI that represents the specific earning that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link SalaryEntry#CONTENT_URI}.
                // For example, the URI would be "content://ch.mse.mybudget.earnings/earnings/2"
                // if the earning with ID 2 was clicked on.
                Uri currentEarningUri = ContentUris.withAppendedId(BudgetrContract.SalaryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentEarningUri);

                // Launch the {@link EditorEarningsActivity} to display the data for the current earning.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(EARNINGS_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded earning data into the database. For debugging purposes only.
     */
    private void insertEarning() {
        // Create a ContentValues object where column names are the keys,
        // and attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT, 100.00);
        values.put(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        values.put(BudgetrContract.SalaryEntry.COLUMN_NAME_USER_ID, 1);

        // Insert a new row into the provider using the ContentResolver.
        // Use the {@link SalaryEntry#CONTENT_URI} to indicate that we want to insert
        // into the earnings database table.
        // Receive the new content URI that will allow us to access data in the future.
        Uri newUri = getContentResolver().insert(BudgetrContract.SalaryEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all earnings in the database.
     */
    private void deleteAllEarnings() {
        int rowsDeleted = getContentResolver().delete(BudgetrContract.SalaryEntry.CONTENT_URI, null, null);
        Log.v("ListEarningsActivity", rowsDeleted + " rows deleted from earnings database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_list_activity.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_list_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertEarning();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllEarnings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                BudgetrContract.SalaryEntry._ID,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                BudgetrContract.SalaryEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link EarningsCursorAdapter} with this new cursor containing updated earning data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
