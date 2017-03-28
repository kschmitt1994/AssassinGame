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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FirebaseHelper", "getMyGames:onCancelled");
            }
        });

    }

}
