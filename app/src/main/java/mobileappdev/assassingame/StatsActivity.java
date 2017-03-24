package mobileappdev.assassingame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by kennyschmitt on 3/24/17.
 */

public class StatsActivity extends AppCompatActivity {

    private TextView mUsernameTextView;
    private TextView mWinsTextView;
    private TextView mLossesTextView;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mUsername = getMyUserName();

        String usersReference = "users/" + mUsername;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(usersReference);

        mUsernameTextView = (TextView) findViewById(R.id.user_name_view);

        mUsernameTextView.setText(mUsername);

        mWinsTextView = (TextView) findViewById(R.id.num_wins_view);

        mWinsTextView.setText("999");

        mLossesTextView = (TextView) findViewById(R.id.num_losses_view);

        mLossesTextView.setText("1");

        //Todo: pull player's number of wins and losses from database
    }

    @NonNull
    private String getMyUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences(LogInActivity.MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LogInActivity.USER_NAME, "undefined");
    }

}
