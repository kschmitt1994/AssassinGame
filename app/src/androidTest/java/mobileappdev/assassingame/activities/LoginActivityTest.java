package mobileappdev.assassingame.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import mobileappdev.assassingame.LogInActivity;
import mobileappdev.assassingame.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static mobileappdev.assassingame.R.id.login_emailEditText;
import static mobileappdev.assassingame.R.id.login_passwordEditText;

/**
 * Created by kennyschmitt on 4/4/17.
 */

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    @Rule
    public ActivityTestRule<LogInActivity> intentsTestRule = new ActivityTestRule<LogInActivity>(LogInActivity.class);
    private LogInActivity mActivity;

    @Before
    public void setUp() {
        mActivity = intentsTestRule.getActivity();
    }

    @Test
    public void login_with_registered_account() throws Exception {

        onView(withId(login_emailEditText))
                .perform(typeText("schmitt.177@osu.edu"), closeSoftKeyboard());
        onView(withId(login_passwordEditText))
                .perform(typeText("allyson1"), closeSoftKeyboard());
        onView(withId(R.id.login_loginButton)).perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.new_game)).check(matches(isDisplayed()));
    }

    @Test
    public void login_with_unregistered_username() throws Exception {

        onView(withId(login_emailEditText))
                .perform(typeText("NotKenny"), closeSoftKeyboard());
        onView(withId(login_passwordEditText))
                .perform(typeText("NotAPassword"), closeSoftKeyboard());
        onView(withId(R.id.login_loginButton)).perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.new_game)).check(doesNotExist());    }
}
