package ch.msengineering.budgetr;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import ch.msengineering.budgetr.data.BudgetrContract;

/**
 * Created by Marcel on 05.11.2016.
 */

public class ExpendituresCursorAdapter extends CursorAdapter{

    /**
     * Constructs a new {@link EarningsCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ExpendituresCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item_expenditures, parent, false);
    }

    /**
     * This method binds the earnings data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current expenditure can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView descriptionTextView = (TextView) view.findViewById(R.id.tv_expenditures_overview_description);
        TextView dateTextView = (TextView) view.findViewById(R.id.tv_expenditures_overview_date);
        TextView valueTextView = (TextView) view.findViewById(R.id.tv_expenditures_overview_value);

        // Find the columns of expenditure attributes that we're interested in
        int descriptionColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry.COLUMN_NAME_DESCRIPTION);
        int dateColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry.COLUMN_NAME_DATE);
        int valueColumnIndex = cursor.getColumnIndex(BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT);

        // Read the expenditure attributes from the Cursor for the current expenditure
        String expenditureDescription = cursor.getString(descriptionColumnIndex);
        String expenditureDate = cursor.getString(dateColumnIndex);
        float expenditureValue = cursor.getFloat(valueColumnIndex);

        // If the expenditure description is empty string or null, then use some default text
        // that says "What you spent this for?", so the TextView isn't blank.
        if (TextUtils.isEmpty(expenditureDescription)) {
            expenditureDescription = context.getString(R.string.unknown_expenditure_description);
        }

        // Update the TextViews with the attributes for the current expenditure
        descriptionTextView.setText(expenditureDescription);
        dateTextView.setText(expenditureDate);
        valueTextView.setText(String.format("%1$.2f",expenditureValue));
    }

}