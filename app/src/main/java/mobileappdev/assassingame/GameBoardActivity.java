package mobileappdev.assassingame;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/14/2017
 */

public class GameBoardActivity extends AppCompatActivity {

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
//        publicSwitch.setFocusable(false);
//        publicSwitch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Game.getInstance().setPublic(((Switch)v).isChecked());
//            }
//        });

        FragmentManager fm = getSupportFragmentManager();

        mFragment2 = (InvitedPlayersFragment) fm.findFragmentById(R.id.fragment_game_players);
        if (mFragment2 == null) {
            mFragment2 = new InvitedPlayersFragment();
            fm.beginTransaction().add(R.id.fragment_game_players, mFragment2).commit();
        }

        Button cancelButton = (Button) findViewById(R.id.cancel_game);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelGameCreation();
            }
        });

        mCreateGameButton = (Button) findViewById(R.id.create_game);
        mCreateGameButton.setEnabled(shouldEnableCreateButton());
        mCreateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGame();
            }
        });
    }

    private void cancelGameCreation() {
        new AsyncTaskCancelGame().execute(this);
        Game.getInstance().removeAllPlayers();
        startActivity(new Intent(GameBoardActivity.this, MainActivity.class));
        finish();
    }

    private boolean shouldEnableCreateButton() {
        return true;
//        return getNoOfPlayersInGame(this) >= MINIMUM_PLAYERS_NEEDED;
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
        if (!ExternalServicesHelper.isConnected(this)) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ExternalServicesHelper.isLocationServicesEnabled(this)) {
            Toast.makeText(this, "Plz enable location services!", Toast.LENGTH_SHORT).show();
            return;
        }


        new MyAsyncTask().execute();
        Intent intent = new Intent(GameBoardActivity.this, PlayBoardActivity.class);
        intent.putExtra(BroadcastHelper.AM_I_ADMIN, true);
        intent.putExtra(BroadcastHelper.GAME_STARTED, true);
        intent.putExtra(BroadcastHelper.GAME_NAME, Game.getInstance().getGameName());
        startActivity(intent);
        finish();
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            FirebaseHelper.createGame(Game.getInstance());
            FirebaseHelper.sendGameStartMessage(Game.getInstance().getGameName());
            addPlayersToDatabase();
            return null;
        }
    }

    private void addPlayersToDatabase() {
        Game gameInstance = Game.getInstance();
        DatabaseHandler handler = new DatabaseHandler(this, gameInstance.getGameName());
        for (Player player : gameInstance.getAllPlayers()) {
            handler.addPlayer(player);
        }

    }


    public int getNoOfPlayersInGame(Context context) {
//        return new DatabaseHandler(context, Game.getInstance().getGameName()).getPlayersCount();
        return Game.getInstance().getAllPlayers().size();
    }

}
