package mobileappdev.assassingame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        final EditText gameTitleET = (EditText)findViewById(R.id.game_title_TF);
        final RadioButton selectedRadioButton = getSelectedRadioButton();

        Button invitePlayersButton = (Button)findViewById(R.id.invite_players_button);
        invitePlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validGameName(gameTitleET.getText().toString())) {
//                    gameTitleET.setTextColor(Color.RED);
                    return;
                }

                Intent intent = new Intent(NewGameActivity.this, InvitePlayersActivity.class);
                Game.getInstance().setPublic(selectedRadioButton.getText().toString().contains("Public"));
                startActivity(intent);
            }
        });

        /*Button cancelButton = (Button)findViewById(R.id.cancel_game_creation_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewGameActivity.this, MainActivity.class));
            }
        });

        Button createButton = (Button)findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Game gameInstance = Game.getInstance(getApplicationContext());
                gameInstance.setGameName(gameTitleET.getText().toString());
                startActivity(new Intent(NewGameActivity.this, GameBoardActivity.class));
            }
        });*/

    }

    private RadioButton getSelectedRadioButton() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        return (RadioButton) findViewById(selectedId);
    }

    private boolean validGameName(String gameName) {
        List<String> allGameNames = FirebaseHelper.getAllGameNames();
        if (allGameNames.contains(gameName)) {
            Toast.makeText(this, "Game already exists. Please Choose a different name..", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
