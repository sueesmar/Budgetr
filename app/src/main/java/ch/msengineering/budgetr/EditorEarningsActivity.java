package ch.msengineering.budgetr;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ch.msengineering.budgetr.data.BudgetrContract;

public class EditorEarningsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * Identifier for the earnings data loader
     */
    private static final int EXISTING_EARNINGS_LOADER = 0;

    /**
     * Content URI for the existing earning (null if it's a new earning)
     */
    private Uri mCurrentEarningUri;

    /**
     * EditText field to enter the earning's amount
     */
    private EditText mSalarymountEditText;

    /**
     * EditText field to enter the earning's date
     */
    private EditText mDateEditText;

    /**
     * Boolean flag that keeps track of whether the earning has been edited (true) or not (false)
     */
    private boolean mEarningHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mEarningHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mEarningHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_earnings);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new earning or editing an existing one.
        Intent intent = getIntent();
        mCurrentEarningUri = intent.getData();

        // If the intent DOES NOT contain a earning content URI, then we know that we are
        // creating a new earning.
        if (mCurrentEarningUri == null) {
            // This is a new earning, so change the app bar to say "Add a Earning"
            setTitle(getString(R.string.editor_earnings_activity_title_new_earning));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a earning that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing earning, so change app bar to say "Edit Earning"
            setTitle(getString(R.string.editor_earnings_activity_title_edit_earning));

            // Initialize a loader to read the earning data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_EARNINGS_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mSalarymountEditText = (EditText) findViewById(R.id.et_editor_earnings_salarymount);
        mDateEditText = (EditText) findViewById(R.id.et_editor_earnings_date);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mSalarymountEditText.setOnTouchListener(mTouchListener);
        mDateEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * Get user input from editor and save earning into database.
     */
    private void saveEarning() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String salarymountString = mSalarymountEditText.getText().toString().trim();
        String dateString = mDateEditText.getText().toString().trim();

        // Check if this is supposed to be a new earning
        // and check if all the fields in the editor are blank
        if (mCurrentEarningUri == null &&
                TextUtils.isEmpty(salarymountString) && TextUtils.isEmpty(dateString)) {
            // Since no fields were modified, we can return early without creating a new earning.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and earning attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE, dateString);
        // If the amount is not provided by the user, don't try to parse the string into an
        // float value. Use 0 by default.
        float amount = 0;
        if (!TextUtils.isEmpty(salarymountString)) {
            amount = Float.parseFloat(salarymountString);
        }
        values.put(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT, amount);

        // Determine if this is a new or existing earning by checking if mCurrentEarningUri is null or not
        if (mCurrentEarningUri == null) {
            // This is a NEW earning, so insert a new earning into the provider,
            // returning the content URI for the new earning.
            Uri newUri = getContentResolver().insert(BudgetrContract.SalaryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_earnings_insert_earning_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_earnings_insert_earning_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING earning, so update the earning with content URI: mCurrentEarningUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentEarningUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentEarningUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_earnings_update_earning_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_earnings_update_earning_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor_activity.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor_activity, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new earning, hide the "Delete" menu item.
        if (mCurrentEarningUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save earning to database
                saveEarning();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the earning hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mEarningHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorEarningsActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorEarningsActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the earning hasn't changed, continue with handling back button press
        if (!mEarningHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all earning attributes, define a projection that contains
        // all columns from the earning table
        String[] projection = {
                BudgetrContract.SalaryEntry._ID,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT,
                BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentEarningUri,         // Query the content URI for the current earning
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of earning attributes that we're interested in
            int amountColumnIndex = cursor.getColumnIndex(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT);
            int dateColumnIndex = cursor.getColumnIndex(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE);

            // Extract out the value from the Cursor for the given column index
            float amount = cursor.getFloat(amountColumnIndex);
            String date = cursor.getString(dateColumnIndex);


            // Update the views on the screen with the values from the database
            mSalarymountEditText.setText(String.format("%1$.2f",amount));
            mDateEditText.setText(date);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mSalarymountEditText.setText("");
        mDateEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the earning.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this earning.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the earning.
                deleteEarning();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the earning.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the earning in the database.
     */
    private void deleteEarning() {
        // Only perform the delete if this is an existing earning.
        if (mCurrentEarningUri != null) {
            // Call the ContentResolver to delete the earning at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentEarningUri
            // content URI already identifies the earning that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentEarningUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_earnings_delete_earning_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_earnings_delete_earning_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
