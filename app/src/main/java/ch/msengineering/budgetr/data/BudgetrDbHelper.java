package ch.msengineering.budgetr.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Marcel on 05.11.2016.
 */

public class BudgetrDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = BudgetrDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "budgetr.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 3;

    /**
     * Constructs a new instance of {@link BudgetrDbHelper}.
     *
     * @param context of the app
     */
    public BudgetrDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the users table
        String SQL_CREATE_USERS_TABLE =  "CREATE TABLE " + BudgetrContract.UserEntry.TABLE_NAME + " ("
                + BudgetrContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BudgetrContract.UserEntry.COLUMN_NAME_USERNAME + " TEXT NOT NULL, "
                + BudgetrContract.UserEntry.COLUMN_NAME_PASSWORD + " TEXT NOT NULL, "
                + BudgetrContract.UserEntry.COLUMN_NAME_EMAIL + " TEXT, "
                + BudgetrContract.UserEntry.COLUMN_NAME_REGDATE + " TEXT);";

        // Create a String that contains the SQL statement to create the expenditures table
        String SQL_CREATE_EXPENDITURES_TABLE =  "CREATE TABLE " + BudgetrContract.ExpenditureEntry.TABLE_NAME + " ("
                + BudgetrContract.ExpenditureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION + " TEXT, "
                + BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT + " REAL NOT NULL, "
                + BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE + " TEXT, "
                + BudgetrContract.ExpenditureEntry.COLUMN_NAME_RECEIPT + " TEXT, "
                + BudgetrContract.ExpenditureEntry.COLUMN_NAME_PLACE + " TEXT, "
                + BudgetrContract.ExpenditureEntry.COLUMN_NAME_PAYMENTMETHODE + " TEXT, "
                + BudgetrContract.ExpenditureEntry.COLUMN_NAME_USER_ID + " INTEGER,"
                + " FOREIGN KEY (" + BudgetrContract.ExpenditureEntry.COLUMN_NAME_USER_ID + ") REFERENCES " + BudgetrContract.UserEntry.TABLE_NAME + "(" + BudgetrContract.UserEntry._ID + "));";

        String SQL_CREATE_SALARY_TABLE =  "CREATE TABLE " + BudgetrContract.SalaryEntry.TABLE_NAME + " ("
                + BudgetrContract.SalaryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT + " REAL NOT NULL, "
                + BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE + " TEXT, "
                + BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDESCRIPTION + " TEXT, "
                + BudgetrContract.SalaryEntry.COLUMN_NAME_USER_ID + " INTEGER, "
                + " FOREIGN KEY (" + BudgetrContract.SalaryEntry.COLUMN_NAME_USER_ID + ") REFERENCES " + BudgetrContract.UserEntry.TABLE_NAME + "(" + BudgetrContract.UserEntry._ID + "));";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_EXPENDITURES_TABLE);
        db.execSQL(SQL_CREATE_SALARY_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String SQL_DELETE_USER_ENTRIES = "DROP TABLE IF EXISTS " + BudgetrContract.UserEntry.TABLE_NAME;
        String SQL_DELETE_EXPENDITRES_ENTRIES = "DROP TABLE IF EXISTS " + BudgetrContract.ExpenditureEntry.TABLE_NAME;
        String SQL_DELETE_SALARY_ENTRIES = "DROP TABLE IF EXISTS " + BudgetrContract.SalaryEntry.TABLE_NAME;

        db.execSQL(SQL_DELETE_USER_ENTRIES);
        db.execSQL(SQL_DELETE_EXPENDITRES_ENTRIES);
        db.execSQL(SQL_DELETE_SALARY_ENTRIES);

        onCreate(db);

    }
}
