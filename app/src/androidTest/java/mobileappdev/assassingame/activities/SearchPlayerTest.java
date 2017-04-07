package mobileappdev.assassingame.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import mobileappdev.assassingame.LogInActivity;
import mobileappdev.assassingame.NewGameActivity;
import mobileappdev.assassingame.R;
import mobileappdev.assassingame.SearchPlayerFragment;
import mobileappdev.assassingame.SearchPlayerResultFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static mobileappdev.assassingame.R.id.game_title_TF;
import static mobileappdev.assassingame.R.id.login_emailEditText;
import static mobileappdev.assassingame.R.id.login_passwordEditText;

/**
 * Created by kennyschmitt on 4/7/17.
 */

@RunWith(AndroidJUnit4.class)
public class SearchPlayerTest {

    Random random = new Random();
    private static final String _CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STR_LENGTH = 8;
    @Rule
    public ActivityTestRule<NewGameActivity> intentsTestRule = new ActivityTestRule<NewGameActivity>(NewGameActivity.class);
    private NewGameActivity mActivity;
    private SearchPlayerResultFragment mFragment;

    @Before
    public void setUp() {
        mActivity = intentsTestRule.getActivity();
    }

    @Test
    public void search_registered_player() throws Exception {

        onView(withId(game_title_TF))
                .perform(typeText(getRandomString()), closeSoftKeyboard());
        onView(withId(R.id.invite_players_button)).perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.search_player_box)).perform(typeText("Kenny"), closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.search_results_list_view)).perform(click());
        onView(withId(R.id.player_name)).check(matches(withText("Kenny")));

        //int results = mListView.getAdapter().getCount();
        //assertEquals(playerName, "Kenny");

    }

    @Test
    public void search_unregistered_player() throws Exception {

        onView(withId(game_title_TF))
                .perform(typeText(getRandomString()), closeSoftKeyboard());
        onView(withId(R.id.invite_players_button)).perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.search_player_box)).perform(typeText("Not Kenny"), closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //onView(withId(R.id.search_results_list_view)).check();
        onView(withId(R.id.player_name)).check(doesNotExist());

        //int results = mListView.getAdapter().getCount();
        //assertEquals(playerName, "Kenny");

    }

    public String getRandomString(){
        StringBuffer randStr = new StringBuffer();
        for (int i = 0; i < RANDOM_STR_LENGTH; i++) {
            int number = getRandomNumber();
            char ch = _CHAR.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    private int getRandomNumber() {
        int randomInt = 0;
        randomInt = random.nextInt(_CHAR.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }
}
