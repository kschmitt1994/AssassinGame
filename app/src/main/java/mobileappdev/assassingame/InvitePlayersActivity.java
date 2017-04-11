package mobileappdev.assassingame;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Map;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/13/2017
 */

public class InvitePlayersActivity extends AppCompatActivity implements SearchOpListener, InvitedPlayerListChangeListener {

    private SearchPlayerResultFragment mSPRFragment;
    private InvitedPlayersFragment mIPFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_players);

        FragmentManager fm = getSupportFragmentManager();
        SearchPlayerFragment SPFragment = (SearchPlayerFragment) fm.findFragmentById(R.id.frameLayout);
        if (SPFragment == null) {
            SPFragment = new SearchPlayerFragment();
            fm.beginTransaction().add(R.id.frameLayout, SPFragment).commit();
        }

        mSPRFragment = (SearchPlayerResultFragment) fm.findFragmentById(R.id.frameLayout2);
        if (mSPRFragment == null) {
            mSPRFragment = new SearchPlayerResultFragment();
            fm.beginTransaction().add(R.id.frameLayout2, mSPRFragment).commit();
        }

        mIPFragment = (InvitedPlayersFragment) fm.findFragmentById(R.id.frameLayout3);
        if (mIPFragment == null) {
            mIPFragment = new InvitedPlayersFragment();
            fm.beginTransaction().add(R.id.frameLayout3, mIPFragment).commit();
        }

        Button cancelButton = (Button) findViewById(R.id.cancel_game);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelGameCreation();
            }
        });

        Button proceedButton = (Button) findViewById(R.id.invite);
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteAndProceed();
            }
        });
    }

    private void cancelGameCreation() {
        new AsyncTaskCancelGame().execute(this);

        Game gameInstance = Game.getInstance();
        gameInstance.removeAllPlayers();

        startActivity(new Intent(InvitePlayersActivity.this, MainActivity.class));
        finish();
    }

    private void inviteAndProceed() {
        new InvitePlayersAsyncTask().execute();
        Toast.makeText(InvitePlayersActivity.this, "Sending invites...", Toast.LENGTH_SHORT).show();
//        mSPRFragment.resetPlayers2Invite();
        startActivity(new Intent(InvitePlayersActivity.this, GameBoardActivity.class));
        finish();
    }

    private class InvitePlayersAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Game instance = Game.getInstance();
            FirebaseHelper.sendInvite(instance.getAllPlayerNames(), instance.getGameName(), instance.getGameAdmin());
            return null;
        }
    }


    @Override
    public void updateSearchResult() {
        mSPRFragment.update();
    }

    @Override
    public void update() {
        mIPFragment.update();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
