package ch.msengineering.budgetr.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Marcel on 05.11.2016.
 */

public class BudgetrProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = BudgetrProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the users table */
    private static final int USERS = 100;

    /** URI matcher code for the content URI for the earnings table */
    private static final int EARNINGS = 101;

    /** URI matcher code for the content URI for the expenditures table */
    private static final int EXPENDITURES = 102;

    /** URI matcher code for the content URI for a single user in the users table */
    private static final int USER_ID = 103;

    /** URI matcher code for the content URI for a single user in the earnings table */
    private static final int EARNING_ID = 104;

    /** URI matcher code for the content URI for a single user in the users table */
    private static final int EXPENDITURE_ID = 105;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.users/users" will map to the
        // integer code {@link #USERS}. This URI is used to provide access to MULTIPLE rows
        // of the users table.
        sUriMatcher.addURI(BudgetrContract.CONTENT_AUTHORITY, BudgetrContract.PATH_USERS, USERS);

        // The content URI of the form "content://com.example.android.users/users/#" will map to the
        // integer code {@link #USER_ID}. This URI is used to provide access to ONE single row
        // of the users table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.users/users/3" matches, but
        // "content://com.example.android.users/users" (without a number at the end) doesn't match.
        sUriMatcher.addURI(BudgetrContract.CONTENT_AUTHORITY, BudgetrContract.PATH_USERS + "/#", USER_ID);


        sUriMatcher.addURI(BudgetrContract.CONTENT_AUTHORITY, BudgetrContract.PATH_EARNINGS, EARNINGS);

        sUriMatcher.addURI(BudgetrContract.CONTENT_AUTHORITY, BudgetrContract.PATH_EARNINGS + "/#", EARNING_ID);

        sUriMatcher.addURI(BudgetrContract.CONTENT_AUTHORITY, BudgetrContract.PATH_EXPENDITURES, EXPENDITURES);

        sUriMatcher.addURI(BudgetrContract.CONTENT_AUTHORITY, BudgetrContract.PATH_EXPENDITURES + "/#", EXPENDITURE_ID);
    }

    /** Database helper object */
    private BudgetrDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BudgetrDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case USERS:
                // For the USERS code, query the users table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the users table.
                cursor = database.query(BudgetrContract.UserEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case USER_ID:
                // For the USER_ID code, extract out the ID from the URI.
                // For an example URI such as "content://ch.mse.myBudget/users/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = BudgetrContract.UserEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the earnings table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BudgetrContract.UserEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EARNINGS:
                cursor = database.query(BudgetrContract.SalaryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EARNING_ID:
                selection = BudgetrContract.SalaryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the users table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BudgetrContract.SalaryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EXPENDITURES:
                cursor = database.query(BudgetrContract.ExpenditureEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EXPENDITURE_ID:
                selection = BudgetrContract.ExpenditureEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the expenditures table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BudgetrContract.ExpenditureEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USERS:
                return insertUser(uri, contentValues);
            case EARNINGS:
                return insertEarning(uri, contentValues);
            case EXPENDITURES:
                return insertExpenditure(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a user into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertUser(Uri uri, ContentValues values) {
        // Check that the username is not null
        String username = values.getAsString(BudgetrContract.UserEntry.COLUMN_NAME_USERNAME);
        if (username == null) {
            throw new IllegalArgumentException("User requires a username");
        }

        // Check that the password is not null
        String password = values.getAsString(BudgetrContract.UserEntry.COLUMN_NAME_PASSWORD);
        if (password == null) {
            throw new IllegalArgumentException("User requires a password");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new user with the given values
        long id = database.insert(BudgetrContract.UserEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertEarning(Uri uri, ContentValues values) {
        // Check that the salarymount is bigger than 0
        float salarymount = values.getAsFloat(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT);
        if (salarymount < 0) {
            throw new IllegalArgumentException("Salary must be bigger than 0.");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new earning with the given values
        long id = database.insert(BudgetrContract.SalaryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertExpenditure(Uri uri, ContentValues values) {
        // Check that the salarymount is bigger than 0
        float amount = values.getAsFloat(BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT);
        if (amount < 0) {
            throw new IllegalArgumentException("Expenditure must be bigger than 0.");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new earning with the given values
        long id = database.insert(BudgetrContract.SalaryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USERS:
                return updateUser(uri, contentValues, selection, selectionArgs);
            case USER_ID:
                // For the USER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BudgetrContract.UserEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateUser(uri, contentValues, selection, selectionArgs);
            case EARNINGS:
                return updateEarnings(uri, contentValues, selection, selectionArgs);
            case EARNING_ID:
                // For the EARNING_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BudgetrContract.SalaryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateEarnings(uri, contentValues, selection, selectionArgs);
            case EXPENDITURES:
                return updateExpenditure(uri, contentValues, selection, selectionArgs);
            case EXPENDITURE_ID:
                // For the EXPENDITURE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BudgetrContract.ExpenditureEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateExpenditure(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update users in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more users).
     * Return the number of rows that were successfully updated.
     */
    private int updateUser(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link UserEntry#COLUMN_NAME_USERNAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(BudgetrContract.UserEntry.COLUMN_NAME_USERNAME)) {
            String username = values.getAsString(BudgetrContract.UserEntry.COLUMN_NAME_USERNAME);
            if (username == null) {
                throw new IllegalArgumentException("User requires a username");
            }
        }

        if (values.containsKey(BudgetrContract.UserEntry.COLUMN_NAME_PASSWORD)) {
            String password = values.getAsString(BudgetrContract.UserEntry.COLUMN_NAME_PASSWORD);
            if (password == null) {
                throw new IllegalArgumentException("User requires a password");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BudgetrContract.UserEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Update earnings in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more users).
     * Return the number of rows that were successfully updated.
     */
    private int updateEarnings(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link SalaryEntry#COLUMN_NAME_SALARYMOUNT} key is present,
        // check that the amount ist bigger than 0.
        if (values.containsKey(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT)) {
            float salarymount = values.getAsFloat(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT);
            if (salarymount < 0) {
                throw new IllegalArgumentException("Salary must be bigger than 0.");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BudgetrContract.SalaryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Update expenditures in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more users).
     * Return the number of rows that were successfully updated.
     */
    private int updateExpenditure(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ExpendituresEntry#COLUMN_NAME_AMOUNT} key is present,
        // check that the amount ist bigger than 0.
        if (values.containsKey(BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT)) {
            float amount = values.getAsFloat(BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT);
            if (amount < 0) {
                throw new IllegalArgumentException("Amount must be bigger than 0.");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BudgetrContract.ExpenditureEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USERS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BudgetrContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_ID:
                // Delete a single row given by the ID in the URI
                selection = BudgetrContract.UserEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(BudgetrContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EARNINGS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BudgetrContract.SalaryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EARNING_ID:
                // Delete a single row given by the ID in the URI
                selection = BudgetrContract.SalaryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(BudgetrContract.SalaryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EXPENDITURES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BudgetrContract.ExpenditureEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EXPENDITURE_ID:
                // Delete a single row given by the ID in the URI
                selection = BudgetrContract.ExpenditureEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(BudgetrContract.ExpenditureEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USERS:
                return BudgetrContract.UserEntry.CONTENT_LIST_TYPE;
            case USER_ID:
                return BudgetrContract.UserEntry.CONTENT_ITEM_TYPE;
            case EARNINGS:
                return BudgetrContract.SalaryEntry.CONTENT_LIST_TYPE;
            case EARNING_ID:
                return BudgetrContract.SalaryEntry.CONTENT_ITEM_TYPE;
            case EXPENDITURES:
                return BudgetrContract.ExpenditureEntry.CONTENT_LIST_TYPE;
            case EXPENDITURE_ID:
                return BudgetrContract.ExpenditureEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
