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

public class ListExpendituresActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the expenditures data loader */
    private static final int EXPENDITURES_LOADER = 0;

    /** Adapter for the ListView */
    ExpendituresCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_expenditures);

        // Setup FAB to open EditorExpendituresActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_expenditures);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListExpendituresActivity.this, EditorExpendituresActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the expenditure data
        ListView expendituresListView = (ListView) findViewById(R.id.list_expenditures);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.expenditures_empty_view);
        expendituresListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of expenditure data in the Cursor.
        // There is no expenditure data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ExpendituresCursorAdapter(this, null);
        expendituresListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        expendituresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorExpenditureActivity}
                Intent intent = new Intent(ListExpendituresActivity.this, EditorExpendituresActivity.class);

                // Form the content URI that represents the specific earning that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ExpenditureEntry#CONTENT_URI}.
                // For example, the URI would be "content://ch.mse.mybudget.expenditures/expenditures/2"
                // if the expenditure with ID 2 was clicked on.
                Uri currentExpenditureUri = ContentUris.withAppendedId(BudgetrContract.ExpenditureEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentExpenditureUri);

                // Launch the {@link EditorExpendituresActivity} to display the data for the current expenditures.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(EXPENDITURES_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded expenditure data into the database. For debugging purposes only.
     */
    private void insertExpenditure() {
        // Create a ContentValues object where column names are the keys,
        // and attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION, "Food");
        values.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE, new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date()));
        values.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_USER_ID, 1);
        values.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT, 20.55);
        values.put(BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE, "Migros");

        // Insert a new row into the provider using the ContentResolver.
        // Use the {@link ExpenditureEntry#CONTENT_URI} to indicate that we want to insert
        // into the expenditures database table.
        // Receive the new content URI that will allow us to access data in the future.
        Uri newUri = getContentResolver().insert(BudgetrContract.ExpenditureEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all earnings in the database.
     */
    private void deleteAllExpenditures() {
        int rowsDeleted = getContentResolver().delete(BudgetrContract.ExpenditureEntry.CONTENT_URI, null, null);
        Log.v("ListExpendituresActivit", rowsDeleted + " rows deleted from expenditures database");
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
                insertExpenditure();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllExpenditures();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                BudgetrContract.ExpenditureEntry._ID,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE,
                BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                BudgetrContract.ExpenditureEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ExpendituresCursorAdapter} with this new cursor containing updated expenditure data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}