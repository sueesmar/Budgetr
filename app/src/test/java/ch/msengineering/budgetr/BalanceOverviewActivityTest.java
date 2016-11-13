package ch.msengineering.budgetr;

import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Marcel on 13.11.2016.
 */

@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class BalanceOverviewActivityTest {
    private BalanceOverviewActivity balanceOverviewActivity;


    @Before
    public void setup() {
        balanceOverviewActivity = Robolectric.setupActivity(BalanceOverviewActivity.class);

    }


    @Test
    public void valdiateActualBalanceTextView(){
        TextView mTotalEarningTextView = (TextView) balanceOverviewActivity.findViewById(R.id.tv_balance_overview_money_earned);
        TextView mTotalExpenditureTextView = (TextView) balanceOverviewActivity.findViewById(R.id.tv_balance_overview_money_spent);
        TextView mActualBalanceTextView = (TextView) balanceOverviewActivity.findViewById(R.id.tv_balance_overview_balance_value);

        balanceOverviewActivity.displayTotalEarnings();
        balanceOverviewActivity.displayTotalExpenditures();
        balanceOverviewActivity.displayActualBalance();

        Double totalEarnings = balanceOverviewActivity.getTotalEarnings();
        Double totalExpenditures = balanceOverviewActivity.getTotalExpenditures();
        Double actualBalance = totalEarnings - totalExpenditures;

        assertNotNull("TextView could not be found", mActualBalanceTextView);
        assertEquals("TextView contains incorrect text",
                String.format("%1$.2f", actualBalance), mActualBalanceTextView.getText());

    }

}
