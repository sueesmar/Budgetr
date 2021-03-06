package ch.msengineering.budgetr;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import ch.msengineering.budgetr.data.BudgetrContract;

public class BalanceOverviewActivity extends AppCompatActivity {

    TextView mTotalEarningTextView;
    TextView mTotalExpenditureTextView;
    TextView mActualBalanceTextView;
    TextView mActualBalanceCurrencyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_overview);

        mTotalEarningTextView = (TextView) findViewById(R.id.tv_balance_overview_money_earned);
        mTotalExpenditureTextView = (TextView) findViewById(R.id.tv_balance_overview_money_spent);
        mActualBalanceTextView = (TextView) findViewById(R.id.tv_balance_overview_balance_value);
        mActualBalanceCurrencyTextView = (TextView) findViewById(R.id.tv_balance_overview_money_balance_currency);

        Button bEarnings = (Button) findViewById(R.id.btn_show_earnings);
        bEarnings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BalanceOverviewActivity.this, ListEarningsActivity.class);
                startActivity(intent);
            }
        });

        Button bExpenditures = (Button) findViewById(R.id.btn_show_expenditures);
        bExpenditures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BalanceOverviewActivity.this, ListExpendituresActivity.class);
                startActivity(intent);
            }
        });



    }

    /***
     * Call this method when this activity comes in foreground and update the balance.
     */
    @Override
    protected void onStart(){
        super.onStart();
        displayTotalExpenditures();
        displayTotalEarnings();
        displayActualBalance();
    }

    /***
     * Displays the actual total amount of earnings in the balance overview.
     */
    public void displayTotalEarnings(){

        double amount = getTotalEarnings();

        //Format with two decimals
        mTotalEarningTextView.setText(String.format("%1$.2f",amount));
    }

    /***
     * Displays the actual total amount of expenditures on the balance overview.
     */
    public void displayTotalExpenditures(){

        double amount = getTotalExpenditures();

        mTotalExpenditureTextView.setText(String.format("%1$.2f", amount));
    }

    /***
     * Gets the total amount of expenditures from database
     * @return Total expenditures
     */
    public double getTotalExpenditures(){

        //SQL SUM() over the amount column
        String[] projection = {
                "sum(" + BudgetrContract.ExpenditureEntry.COLUMN_NAME_AMOUNT + ")"};

        Cursor cursor = getContentResolver().query(
                BudgetrContract.ExpenditureEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        return cursor.getDouble(0);
    }

    /***
     * Gets the total amount of earnings from database
     * @return Total earnings
     */
    public double getTotalEarnings(){

        //SQL SUM() over the amount column
        String[] projection = {
                "sum(" + BudgetrContract.SalaryEntry.COLUMN_NAME_SALARYMOUNT + ")"};

        Cursor cursor = getContentResolver().query(
                BudgetrContract.SalaryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        return cursor.getDouble(0);
    }

    /***
     * Calculates actual balance and display the amount on the balance overview.
     */
    public void displayActualBalance(){
        double totalEarnings = getTotalEarnings();
        double totalExpenditures = getTotalExpenditures();

        double actualBalance = totalEarnings - totalExpenditures;

        if(0 > actualBalance){
            mActualBalanceTextView.setTextColor(getResources().getColor(R.color.colorTextBalanceOverviewNegativ));
            mActualBalanceCurrencyTextView.setTextColor(getResources().getColor(R.color.colorTextBalanceOverviewNegativ));
        } else {
            mActualBalanceTextView.setTextColor(getResources().getColor(R.color.colorTextBalanceOverview));
            mActualBalanceCurrencyTextView.setTextColor(getResources().getColor(R.color.colorTextBalanceOverview));
        }

        mActualBalanceTextView.setText(String.format("%1$.2f", actualBalance));

    }

}
