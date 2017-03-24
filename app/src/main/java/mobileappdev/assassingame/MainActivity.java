package mobileappdev.assassingame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static mobileappdev.assassingame.LogInActivity.IS_USER_LOGGED_IN;
import static mobileappdev.assassingame.LogInActivity.MY_PREFERENCES;
import static mobileappdev.assassingame.LogInActivity.USER_NAME;

public class MainActivity extends AppCompatActivity {

    // Firebase Authentication objects
    // Required for monitoring authentication state

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // just ignore this
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deviceRef = database.getReference("users/" +
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "/device/" + deviceToken);
        DatabaseReference newDeviceRef = deviceRef.push();
        newDeviceRef.setValue("test device");

        checkForLocationServices(this);

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

                SharedPreferences sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedpreferences.edit();

                edit.putBoolean(IS_USER_LOGGED_IN, false);
                edit.apply();

                startActivity(new Intent(MainActivity.this, LogInActivity.class));

            }
        });

        //Todo: For testing purposes, the settings button links to a chat activity.  Change this later.
        Button settingsButton = (Button)findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));

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

    public void checkForLocationServices(Context context) {

        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            showSettingsAlert(context);
        }
    }

    public void showSettingsAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("GPS is settings");
//        alertDialog.setCancelable(false);
        alertDialog.setMessage("Location needs to be enabled for this game. Please Press Cancel to exit the game");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                MainActivity.this.finish();
                //context.startActivity(new Intent(context, LogInActivity.class));
            }
        });

        // Showing Alert Message
        alertDialog.show();
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

}
