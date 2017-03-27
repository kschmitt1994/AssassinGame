package mobileappdev.assassingame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by kennyschmitt on 3/24/17.
 */

public class StatsActivity extends AppCompatActivity {

    private TextView mUsernameTextView;
    private TextView mWinsTextView;
    private TextView mLossesTextView;
    private String mUsername;
    private HashMap<String, Integer> stats = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUsername = user.getDisplayName();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference statsRef = database.getReference("users/" + mUsername + "/stats");
            statsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        stats.put(snapshot.getKey(), Integer.parseInt(snapshot.getValue().toString()));
                        Log.d("Firebase", snapshot.getKey() + snapshot.toString());
                    }

                    // This way, any time we have an update in this data, the view will be modified
                    // to reflect those changes. Test it out yourself in the Firebase console!

                    mWinsTextView = (TextView) findViewById(R.id.num_wins_view);
                    mWinsTextView.setText(stats.get("wins").toString());

                    mLossesTextView = (TextView) findViewById(R.id.num_losses_view);
                    mLossesTextView.setText(stats.get("losses").toString());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("Firebase", "Failed to read value.", error.toException());
                }
            });
        }

        mUsernameTextView = (TextView) findViewById(R.id.user_name_view);

        mUsernameTextView.setText(mUsername);

        //Todo: pull player's number of wins and losses from database
    }

    @NonNull
    private String getMyUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences(LogInActivity.MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LogInActivity.USER_NAME, "undefined");
    }

}
