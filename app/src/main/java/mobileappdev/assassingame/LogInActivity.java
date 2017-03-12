package mobileappdev.assassingame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "Assassin_Game";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        Log.i(TAG, "OnCreate method is invoked.");

        Button signUpButton = (Button) findViewById(R.id.login_signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, SignUpPageActivity.class));
            }
        });

        Button signInButton = (Button) findViewById(R.id.login_loginButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText emailValue = (EditText) findViewById(R.id.login_emailEditText);
                String email = emailValue.getText().toString();

                EditText passwordValue = (EditText) findViewById(R.id.login_passwordEditText);
                String password = passwordValue.getText().toString();

                startActivity(new Intent(LogInActivity.this, MainActivity.class));
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Start method is invoked.");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Stop method is invoked.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy method is invoked.");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Pause method is invoked.");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Resume method is invoked.");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "Restart method is invoked.");

    }


}
