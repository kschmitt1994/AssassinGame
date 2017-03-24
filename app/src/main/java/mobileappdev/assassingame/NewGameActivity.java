package mobileappdev.assassingame;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/15/2017
 */

public class NewGameActivity extends AppCompatActivity {

    private EditText mGameTitleET;
    private Button mProceedButton;
    private Spinner mProgressDialog;
    private Context _this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        _this = this;
        mGameTitleET = (EditText)findViewById(R.id.game_title_TF);
        mGameTitleET.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mProceedButton.setEnabled(s.toString().trim().length() != 0);
            }
        });

        mProceedButton = (Button)findViewById(R.id.invite_players_button);
        mProceedButton.setEnabled(false);
        mProceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new Spinner(_this);
                mProgressDialog.show("Validation", "Validating game name. Please wait...", false);
                fetchAllGameNames();
            }
        });

    }

    @NonNull
    private String getMyUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences(LogInActivity. MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LogInActivity.USER_NAME, "undefined");
    }

    private RadioButton getSelectedRadioButton() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        return (RadioButton) findViewById(selectedId);
    }

    public void fetchAllGameNames() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        Query gameQuery = ref.child("games");
        final List<String> gameNames = new ArrayList<String>();

        gameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot gameSnapshot: dataSnapshot.getChildren()) {
                    gameNames.add(gameSnapshot.getKey()); // Because game names are used as keys
                }
                mProgressDialog.dismiss();
                validateAndSetData(gameNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(NewGameActivity.class.getSimpleName(), "loadGames:onCancelled", databaseError.toException());
            }
        });

    }


    private void validateAndSetData(List<String> allGameNames) {
        String gameName = mGameTitleET.getText().toString().trim();

        if (allGameNames.contains(gameName)) {
            Toast.makeText(this, "Game already exists. Please Choose a different name..", Toast.LENGTH_SHORT).show();
            return;
        }

        Game gameInstance = Game.getInstance();
        gameInstance.setGameName(gameName);
        gameInstance.setPublic(getSelectedRadioButton().getText().toString().contains("Public"));
        gameInstance.setGameAdmin(getMyUserName());

        startActivity(new Intent(NewGameActivity.this, InvitePlayersActivity.class));
//        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
