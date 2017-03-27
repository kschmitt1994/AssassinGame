package mobileappdev.assassingame;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignUpPageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("FIREBASE", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("FIREBASE", "onAuthStateChanged:signed_out");
                }
            }
        };

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

                // We need to make sure password1Value and password2Value are equal.

                if (password1.equals(password2) && username.length() > 0 && email.length() > 0 && password1.length() > 0) {
                    // Passwords match; send user to Firebase

                    createAccount(username, email, password1);

                } else if (!password1.equals(password2)){
                    // Throw error dialogue: "Passwords do not match"
                    Log.d("ERROR", "Passwords do not match!");
                    DialogFragment errorFragment = new SignUpErrorFragment();
                    errorFragment.show(getFragmentManager(), "SignUpError");
                } else {
                    // Throw error dialogue: "Incomplete text fields "
                    Log.d("ERROR", "Incomplete text fields!");
                    DialogFragment errorFragment = new IncompleteFieldErrorFragment();
                    errorFragment.show(getFragmentManager(), "SignUpError");
                }
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

    private void pushUserToDatabase(String email, String username) {
        String userReference = "users/" + username;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference emailRef = database.getReference(userReference + "/email");
        emailRef.setValue(email);

        DatabaseReference locationRef = database.getReference(userReference + "/location");
        locationRef.child("lat").setValue(0);
        locationRef.child("lng").setValue(0);
    }

    private void createAccount(final String username, final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("Firebase", "createUserWithEmail:onComplete:" + task.isSuccessful());

                    if (!task.isSuccessful()) {
                        Toast.makeText(SignUpPageActivity.this, "Auth failed", Toast.LENGTH_SHORT).show();
                    } else {
                        pushUserToDatabase(email, username);
                        // Now we need to add the username to the Firebase auth object for later
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username).build();
                        if (user == null)
                            Log.e(SignUpPageActivity.class.getSimpleName(), "Got null user from Firebase");

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("USER", "User profile updated.");
                                    }
                                }
                            });

                        // just ignore this
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference deviceRef = database.getReference("users/" +
                                username + "/device/" + deviceToken);
                        DatabaseReference newDeviceRef = deviceRef.push();
                        newDeviceRef.setValue("test device");
                        startActivity(new Intent(SignUpPageActivity.this, MainActivity.class));
                    }

                }

            });
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
