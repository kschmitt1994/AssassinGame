package mobileappdev.assassingame;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/14/2017
 */

public class GameBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        ((TextView)findViewById(R.id.game_title)).setText(Game.getInstance().getGameName());
        Switch publicSwitch = (Switch) findViewById(R.id.switch1);
        publicSwitch.setChecked(Game.getInstance().isPublic());
        publicSwitch.setEnabled(false);
        publicSwitch.setClickable(false);
        publicSwitch.setFocusable(false);
        publicSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Game.getInstance().setPublic(((Switch)v).isChecked());
            }
        });

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment2 = fm.findFragmentById(R.id.fragment_game_players);
        if (fragment2 == null) {
            fragment2 = new InvitedPlayersFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_game_players, fragment2)
                    .commit();
        }

        Button createGameButton = (Button) findViewById(R.id.create_game);
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGame();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_add_player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_add_player:
                startActivity(new Intent(GameBoardActivity.this, InvitePlayersActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createGame() {
        startActivity(new Intent(GameBoardActivity.this, PlayBoardActivity.class));

    }
}
