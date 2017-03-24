package mobileappdev.assassingame;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JoinGameActivity extends AppCompatActivity {

    ArrayList<String> mItems = new ArrayList<>();

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        Query gameQuery = ref.child("games");
        // final List<String> gameNames = new ArrayList<String>();

        // TODO: SAM: Only fetch the games that are PUBLIC.

        gameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot gameSnapshot: dataSnapshot.getChildren()) {
                    // gameNames.add(gameSnapshot.getKey()); // Because game names are used as keys
                    mItems.add(gameSnapshot.getKey());
                    Log.i("JoinGameActivity", mItems.toString());
                }

                ListView mListView = (ListView) findViewById(R.id.public_games_list_view);
                final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(JoinGameActivity.this,
                        android.R.layout.simple_list_item_1, mItems);
                mListView.setAdapter(mAdapter);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        String playerName = (String) parent.getAdapter().getItem(position);
//                        Toast.makeText(JoinGameActivity.this, playerName + " is added!", Toast.LENGTH_SHORT).show();

                        FirebaseHelper.updatePlayerStatus(
                            mItems.get(position),
                            FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                            PlayerStatus.ALIVE,
                            true // To increase counter
                        );

                        // TODO: SAM: ADD GAME NAME AND OTHER STUFF FROM PlayBoardActivity.java

                        Intent intent = new Intent(JoinGameActivity.this, PlayBoardActivity.class);
                        intent.putExtra(BroadcastHelper.AM_I_ADMIN, false);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("FirebaseHelper", "loadGames:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

}
