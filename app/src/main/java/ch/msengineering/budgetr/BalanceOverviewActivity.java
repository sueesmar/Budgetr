package ch.msengineering.budgetr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class BalanceOverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_overview);

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




}
