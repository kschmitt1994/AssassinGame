package mobileappdev.assassingame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static mobileappdev.assassingame.LogInActivity.IS_USER_LOGGED_IN;
import static mobileappdev.assassingame.LogInActivity.MY_PREFERENCES;
import static mobileappdev.assassingame.LogInActivity.USER_NAME;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForInternetConnection();
        checkForLocationServices();

        Intent intent = getIntent();
        String mGameName = intent.getStringExtra(BroadcastHelper.GAME_NAME);

        if (intent.getBooleanExtra(BroadcastHelper.ON_GAME_REQUEST, false)) {
            String player = intent.getStringExtra(BroadcastHelper.PLAYER_NAME);
            String gameReqResponse = intent.getStringExtra(BroadcastHelper.INVITATION_RESPONSE);
            if (InvitationStatus.ACCEPTED.equals(InvitationStatus.getStatusFrom(gameReqResponse))) {
                FirebaseHelper.sendAcceptResponse(player, mGameName);
            } else {
                FirebaseHelper.sendRejectionResponse(player, mGameName);
            }
            finishAffinity(); //app will be closed
        }

        // Track when user signs in and out
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out!
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        Button newGameButton = (Button)findViewById(R.id.new_game);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGame();
            }
        });

        //Sign out, return to Login Activity
        Button logoutButton = (Button)findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                clearSharedPrefData();
                startActivity(new Intent(MainActivity.this, LogInActivity.class));

            }
        });

//        Button settingsButton = (Button)findViewById(R.id.settings);
//        settingsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, ChatActivity.class));
//
//            }
//        });

        Button statsButton = (Button)findViewById(R.id.stats);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
            }
        });

        Button joinGameButton = (Button)findViewById(R.id.join_game);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, JoinGameActivity.class));
            }
        });

        Button myGamesButton = (Button)findViewById(R.id.my_games);
        myGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyGamesActivity.class));
            }
        });

    }

    private void clearSharedPrefData() {
        SharedPreferences sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedpreferences.edit();

        edit.putBoolean(IS_USER_LOGGED_IN, false);
        edit.putString(USER_NAME, null);
        edit.apply();
    }

    private void checkForInternetConnection() {
        if (!ExternalServicesHelper.isConnected(this)) {
            ExternalServicesHelper.buildDialog(this).show();
        }
    }

    public void checkForLocationServices() {
        if (!ExternalServicesHelper.isLocationServicesEnabled(this)) {
            ExternalServicesHelper.showSettingsAlert(this, MainActivity.this);
        }
    }

    private void createGame() {
        startActivity(new Intent(MainActivity.this, NewGameActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed(); //disable the back button. User needs to logout in order to go back to login screen again
    }
}
