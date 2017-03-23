package mobileappdev.assassingame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.List;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/15/2017
 */

public class NewGameActivity extends AppCompatActivity {

    private EditText mGameTitleET;
    private Button mProceedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

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

        final RadioButton selectedRadioButton = getSelectedRadioButton();

        mProceedButton = (Button)findViewById(R.id.invite_players_button);
        mProceedButton.setEnabled(false);
        mProceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gameName = mGameTitleET.getText().toString().trim();
                if (!validGameName(gameName)) {
//                    gameTitleET.setTextColor(Color.RED);
                    return;
                }

                Game gameInstance = Game.getInstance();
                gameInstance.setGameName(gameName);
                gameInstance.setPublic(selectedRadioButton.getText().toString().contains("Public"));

                startActivity(new Intent(NewGameActivity.this, InvitePlayersActivity.class));
            }
        });

//        Button cancelButton = (Button)findViewById(R.id.cancel_game);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(NewGameActivity.this, MainActivity.class));
//            }
//        });

//        Button createButton = (Button)findViewById(R.id.create_game);
//        createButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                FirebaseHelper.setGamePublic(mGameTitleET.getText().toString());
////                Game gameInstance = Game.getInstance(getApplicationContext());
////                gameInstance.setGameName(mGameTitleET.getText().toString());
//                //startActivity(new Intent(NewGameActivity.this, GameBoardActivity.class));
//            }
//        });

    }

    private RadioButton getSelectedRadioButton() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        return (RadioButton) findViewById(selectedId);
    }

    private boolean validGameName(String gameName) {
        List<String> allGameNames = FirebaseHelper.getAllGameNames();
        if (allGameNames.contains(gameName)) {
            Toast.makeText(this, "Game already exists. Please Choose a different name..",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
