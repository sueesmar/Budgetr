package ch.msengineering.budgetr.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.BaseColumns;

/**
 * Created by Marcel on 05.11.2016.
 */

public class BudgetrContract {

    private BudgetrContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "ch.mse.myBudget";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://ch.mse.myBudget/users/ is a valid path for
     * looking at users data.
     * */
    public static final String PATH_USERS = "users";
    public static final String PATH_EARNINGS = "salary";
    public static final String PATH_EXPENDITURES = "expenditures";



    /* Inner class that defines the table contents */
    public static class UserEntry implements BaseColumns {

        /** The content URI to access the user data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_USERS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of users.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single user.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;

        public static final String TABLE_NAME = "users";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_REGDATE = "regdate";
    }

    /* Inner class that defines the table contents */
    public static class ExpenditureEntry implements BaseColumns {

        /** The content URI to access the user data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EXPENDITURES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of expenditures.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXPENDITURES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single expenditure.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXPENDITURES;

        public static final String TABLE_NAME = "expenditures";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME_DESCRIPTION= "description";
        public static final String COLUMN_NAME_AMOUNT = "amount";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_RECEIPT = "receipt";
        public static final String COLUMN_NAME_PLACE = "place";
        public static final String COLUMN_NAME_PAYMENTMETHODE = "paymentmethode";
        public static final String COLUMN_NAME_USER_ID = "userid";
    }

    /* Inner class that defines the table contents */
    public static class SalaryEntry implements BaseColumns {

        /** The content URI to access the user data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EARNINGS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of earnings.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EARNINGS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single earning.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EARNINGS;

        public static final String TABLE_NAME = "salary";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME_SALARYMOUNT = "salarymount";
        public static final String COLUMN_NAME_SALARYDATE = "salarydate";
        public static final String COLUMN_NAME_USER_ID = "userid";
    }


}
