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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyGamesActivity extends AppCompatActivity {

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
                startActivity(new Intent(MyGamesActivity.this, NewGameActivity.class));
                finish();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        Query gameQuery = ref.child("games").child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        final List<String> gameNames = new ArrayList<String>();

        // TODO: SAM: Only fetch the games that belong to the current user's ID.

        gameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot gameSnapshot: dataSnapshot.getChildren()) {
                    gameNames.add(gameSnapshot.getKey()); // Because game names are used as keys
                    mItems.add(gameSnapshot.getKey());
                    Log.i("MyGamesActivity", mItems.toString());
                }

                ListView mListView = (ListView) findViewById(R.id.public_games_list_view);
                ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(MyGamesActivity.this,
                        android.R.layout.simple_list_item_1, mItems);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("FirebaseHelper", "loadGames:onCancelled", databaseError.toException());
            }
        });
    }

}
