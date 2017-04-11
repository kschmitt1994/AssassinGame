package mobileappdev.assassingame;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyGamesActivity extends AppCompatActivity {

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
                startActivity(new Intent(MyGamesActivity.this, NewGameActivity.class));
                finish();
            }
        });

        mProgressDialog = new Spinner(this);
        mProgressDialog.show("Hang on!", "Fetching the games you have created. Please wait...", false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "MyGames: Firebase current user is null.", Toast.LENGTH_LONG).show();
            return;
        }

        final String displayName = currentUser.getDisplayName();
        if (displayName == null) {
            Toast.makeText(this, "MyGames: Firebase current user is null.", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference games = ref.child("games");

        games.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Object adminObject = snapshot.child("admin").getValue();
                    if (adminObject == null) {
                        Log.i("MyGame", snapshot.getKey() + " doesn't have an admin value");
                        continue;
                    }
                    if (displayName.equals(adminObject.toString())) {
                        mItems.add(snapshot.getKey());
                    }
                }

                ListView mListView = (ListView) findViewById(R.id.public_games_list_view);
                ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(MyGamesActivity.this,
                        android.R.layout.simple_list_item_1, mItems);
                mListView.setAdapter(mAdapter);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String gameName = mItems.get(position);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final String gameTypeRef = "games/" + gameName + "/type";
                        DatabaseReference ref = database.getReference(gameTypeRef);

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Object gameTypeObj = dataSnapshot.getValue();
                                if (gameTypeObj == null) {
                                    Log.w(MyGamesActivity.class.getSimpleName(), gameName + "'s status is null.");
                                    return;
                                }
                                String gameType = gameTypeObj.toString();
                                proceed(gameName, gameType);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(MyGamesActivity.class.getSimpleName(), gameName + "'s status is null.");
                            }
                        });

                    }
                });
                mProgressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(MyGamesActivity.class.getSimpleName(), "getMyGames:onCancelled");
            }
        });

    }

    private void proceed(String gameName, String gameType) {
        Game gameInstance = Game.getInstance();
        gameInstance.setGameName(gameName);
        gameInstance.setPublic("public".equals(gameType));
        gameInstance.setGameAdmin(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        DatabaseHandler handler = new DatabaseHandler(this, gameName);
        gameInstance.setName2PlayerMap(handler.getAllName2PlayerMap());
        startActivity(new Intent(MyGamesActivity.this, InvitePlayersActivity.class));
    }

}
