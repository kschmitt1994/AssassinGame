package mobileappdev.assassingame;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JoinGameActivity extends AppCompatActivity {

    ArrayList<String> mItems = new ArrayList<>();
    private Spinner mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_add_white_24px);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JoinGameActivity.this, NewGameActivity.class));
            }
        });

        mProgressDialog = new Spinner(this);
        mProgressDialog.show("Hang on!", "Fetching public games for you. Please wait...", false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        final DatabaseReference gamesRef = ref.child("games");

        gamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                    Object gameType = gameSnapshot.child("type").getValue();
                    if (gameType == null) {
                        Log.i("JoinGame", gameSnapshot.getKey() + " doesn't have a Type value");
                        continue;
                    }
                    if ("public".equals(gameType.toString())) {
                        mItems.add(gameSnapshot.getKey());
                    }
                }
                mProgressDialog.dismiss();
                populateData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("JoinGameActivity", "loadGames:onCancelled", databaseError.toException());
            }
        });
    }

    private void populateData() {
        ListView mListView = (ListView) findViewById(R.id.public_games_list_view);
        final ArrayAdapter<String> mAdapter = new ArrayAdapter<>(JoinGameActivity.this, android.R.layout.simple_list_item_1, mItems);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String gameName = mItems.get(position);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String gameStatusRef = "games/" + gameName + "/status";
                DatabaseReference ref = database.getReference(gameStatusRef);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object statusObj = dataSnapshot.getValue();
                        if (statusObj == null) {
                            Log.w("JoinGameActivity:", gameName + "'s status is null.");
                            return;
                        }

                        joinGame(gameName, statusObj.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("JoinGameActivity:", gameName + "'s status is null.");
                    }
                });


            }
        });
    }

    private void joinGame(String gameName, String gameStatus) {
        if (GameStatus.FINISHED.equals(GameStatus.getGameStatusFrom(gameStatus))) {
            Toast.makeText(this, "The game is not being played right now. But, you are added to the game.",
                    Toast.LENGTH_SHORT).show();

            String currentUser = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            String playerUrl = "games/" + gameName + "/players/" + currentUser;

            DatabaseReference playerRef = database.getReference(playerUrl);
            playerRef.child("status").setValue(PlayerStatus.NEWLY_JOINED.toString());
            playerRef.child("invite").setValue(InvitationStatus.ACCEPTED.toString());
            playerRef.child("role").setValue(GameCharacter.UNDEFINED.toString());

        } else {
            FirebaseHelper.updatePlayerStatus(gameName, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                    PlayerStatus.ALIVE, true);
            Intent intent = new Intent(JoinGameActivity.this, PlayBoardActivity.class);
            intent.putExtra(BroadcastHelper.GAME_STARTED, true);
            intent.putExtra(BroadcastHelper.GAME_NAME, gameName);
//            intent.putExtra(BroadcastHelper.AM_I_ADMIN, false); //no need since default value is false
            startActivity(intent);
        }
    }

}
