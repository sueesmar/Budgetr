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

/***
 * Class for binding earnings to the {@link android.widget.ListView}
 */
public class EarningsCursorAdapter extends CursorAdapter{

    /**
     * Constructs a new {@link EarningsCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public EarningsCursorAdapter(Context context, Cursor c) {
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
        return LayoutInflater.from(context).inflate(R.layout.list_item_earnings, parent, false);
    }

    /**
     * This method binds the earnings data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current earning can be set on the name TextView
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
        TextView descriptionTextView = (TextView) view.findViewById(R.id.tv_earnings_overview_description);
        TextView dateTextView = (TextView) view.findViewById(R.id.tv_earnings_overview_date);
        TextView valueTextView = (TextView) view.findViewById(R.id.tv_earnings_overview_value);

        // Find the columns of earnings attributes that we're interested in
        int descriptionColumnIndex = cursor.getColumnIndex(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDESCRIPTION);
        int dateColumnIndex = cursor.getColumnIndex(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYDATE);
        int valueColumnIndex = cursor.getColumnIndex(BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT);

        // Read the earning attributes from the Cursor for the current earning
        String earningDescription = cursor.getString(descriptionColumnIndex);
        String earningDate = cursor.getString(dateColumnIndex);
        double earningValue = cursor.getDouble(valueColumnIndex);

        // If the description is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(earningDescription)) {
            earningDescription = context.getString(R.string.unknown_earning_description);
        }

        // Update the TextViews with the attributes for the current earning
        descriptionTextView.setText(earningDescription);
        dateTextView.setText(earningDate);
        valueTextView.setText(String.format("%1$.2f",earningValue));
    }

}
