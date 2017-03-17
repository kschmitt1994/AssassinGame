package mobileappdev.assassingame;

import android.content.Context;
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

public class GameBoardActivity extends AppCompatActivity implements InvitedPlayerListChangeListener {

    private static final int MINIMUM_PLAYERS_NEEDED = 4;
    private Button mCreateGameButton;
    private InvitedPlayersFragment mFragment2;

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

        mFragment2 = (InvitedPlayersFragment) fm.findFragmentById(R.id.fragment_game_players);
        if (mFragment2 == null) {
            mFragment2 = new InvitedPlayersFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_game_players, mFragment2)
                    .commit();
        }

        mCreateGameButton = (Button) findViewById(R.id.create_game);
        mCreateGameButton.setEnabled(getNoOfPlayersInGame(this) >= MINIMUM_PLAYERS_NEEDED);
        mCreateGameButton.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void update() {
        mCreateGameButton.setEnabled(mFragment2.getNoOfPlayersInGame() >= MINIMUM_PLAYERS_NEEDED);
    }

    public int getNoOfPlayersInGame(Context context) {
        return new DatabaseHandler(context, Game.getInstance().getGameName()).getPlayersCount();
    }
}
