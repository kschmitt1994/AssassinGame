package mobileappdev.assassingame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignUpPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);


        Button signUpButton = (Button) findViewById(R.id.signup_signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText usernameValue = (EditText) findViewById(R.id.signup_userNameEditText);
                String username = usernameValue.getText().toString();

                EditText emailValue = (EditText) findViewById(R.id.signup_emailEditText);
                String email = emailValue.getText().toString();

                EditText password1Value = (EditText) findViewById(R.id.signup_passwordEditText);
                String password1 = password1Value.getText().toString();

                EditText password2Value = (EditText) findViewById(R.id.signup_password2EditText);
                String password2 = password2Value.getText().toString();

                startActivity(new Intent(SignUpPageActivity.this, MainActivity.class));
            }
        });

        Button cancelButton = (Button) findViewById(R.id.signup_cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SignUpPageActivity.this, LogInActivity.class));
            }
        });
    }
}
