package mobileappdev.assassingame.util;

import org.junit.runner.RunWith;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import mobileappdev.assassingame.activities.LoginActivityTest;
import mobileappdev.assassingame.activities.SearchPlayerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
                LoginActivityTest.class,
                SearchPlayerTest.class
        }
)

public class androidTestSuite {
    //Intentionally left blank
}
