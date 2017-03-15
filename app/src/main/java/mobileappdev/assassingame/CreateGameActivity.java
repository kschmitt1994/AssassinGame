package mobileappdev.assassingame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        final EditText gameTitleET = (EditText)findViewById(R.id.game_title_TF);

        Button invitePlayersButton = (Button)findViewById(R.id.invite_players_button);
        invitePlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateGameActivity.this, InvitePlayersActivity.class));
            }
        });

        Button cancelButton = (Button)findViewById(R.id.cancel_game_creation_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateGameActivity.this, MainActivity.class));
            }
        });

        Button createButton = (Button)findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Game gameInstance = Game.getInstance(getApplicationContext());
                gameInstance.setGameName(gameTitleET.getText().toString());
                startActivity(new Intent(CreateGameActivity.this, GameBoardActivity.class));
            }
        });

    }
}
