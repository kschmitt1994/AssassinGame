package mobileappdev.assassingame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/13/2017
 */

public class InvitePlayersActivity extends AppCompatActivity implements SearchOpListener, InvitedPlayerListChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abc);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frameLayout);
        if (fragment == null) {
            fragment = new SearchPlayerFragment();
            fm.beginTransaction()
                    .add(R.id.frameLayout, fragment)
                    .commit();
        }

        Fragment fragment1 = fm.findFragmentById(R.id.frameLayout2);
        if (fragment1 == null) {
            fragment1 = new SearchPlayerResultFragment();
            fm.beginTransaction()
                    .add(R.id.frameLayout2, fragment1)
                    .commit();
        }

        Fragment fragment2 = fm.findFragmentById(R.id.frameLayout3);
        if (fragment2 == null) {
            fragment2 = new InvitedPlayersFragment();
            fm.beginTransaction()
                    .add(R.id.frameLayout3, fragment2)
                    .commit();
        }

        Button sendInvitesButton = (Button) findViewById(R.id.button);
        sendInvitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvite();
            }
        });
    }

    private void sendInvite() {
        LinkedList<Player> players = Game.getInstance(getApplicationContext()).getPlayers();
        for (Player player : players) {
            // TODO: 3/14/2017 Send Invites
        }

    }

    @Override
    public void updateSearchResult() {
        SearchPlayerResultFragment fragment =
                (SearchPlayerResultFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.frameLayout2);
        fragment.update();
    }

    @Override
    public void playerInvited() {
        InvitedPlayersFragment fragment =
                (InvitedPlayersFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.frameLayout3);
        fragment.update();
    }
}
