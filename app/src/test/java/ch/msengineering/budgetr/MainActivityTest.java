package ch.msengineering.budgetr;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * Created by Marcel on 13.11.2016.
 */

@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityTest {
    private MainActivity mainActivity;
    private ShadowActivity shadowMainActivity;

    @Before
    public void setup() {
        mainActivity = Robolectric.setupActivity(MainActivity.class);
        shadowMainActivity = shadowOf(this.mainActivity);
    }

    @Test
    public void validateTextViewContent() {
        Button bSignup = (Button) mainActivity.findViewById(R.id.bSignup);
        Button bSignin = (Button) mainActivity.findViewById(R.id.bSignin);


        assertNotNull("TextView could not be found", bSignup);
        assertEquals("TextView contains incorrect text",
                "New to Budgetr", bSignup.getText());

        assertNotNull("TextView could not be found", bSignin);
        assertEquals("TextView contains incorrect text",
                "Already using Budgetr", bSignin.getText());
    }

    @Test
    public void clickingLogin_shouldStartLoginActivity() {
        View loginButton = mainActivity.findViewById(R.id.bSignin);

        loginButton.performClick();

        Intent startedIntent = shadowMainActivity.getNextStartedActivity();

        assertEquals("click on the loginButton starts the LoginActivity",
                LoginActivity.class.getName(), startedIntent.getComponent().getClassName());
    }

    @Test
    public void clickingLogin_shouldStartRegisterActivity() {
        View registerButton = mainActivity.findViewById(R.id.bSignup);

        registerButton.performClick();

        Intent startedIntent = shadowMainActivity.getNextStartedActivity();

        assertEquals("click on the loginButton starts the RegisterActivity",
                RegisterActivity.class.getName(), startedIntent.getComponent().getClassName());
    }
}