package mobileappdev.assassingame;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/13/2017
 */

public class SearchPlayerFragment extends Fragment {

    private Spinner mSpinner;
    private SearchOpListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (SearchOpListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_search_player, container, false);
        Button searchButton = (Button)view.findViewById(R.id.search_button);
        final EditText searchBoxValue = (EditText) view.findViewById(R.id.search_player_box);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSearchPlayer();
                String player = searchBoxValue.getText().toString();
                if (player.equals("")) {
                    Toast.makeText(getContext(), "Empty search field!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i("TESTING", "we are entering the getplayerlist() function");
                searchPlayer(player, player.contains("@"));
                searchBoxValue.setText("");
            }
        });
        return view;
    }

    private void resetSearchPlayer() {
        Game.getInstance().setSearchedPlayer(null);
    }

    private void searchPlayer(String playerInfo, boolean byEmail) {
        showSpinner(byEmail);
        if (byEmail) {
            fetchPlayerByEmailID(playerInfo);
        } else {
            fetchPlayerListContainingUserName(playerInfo);
        }

    }

    private void showSpinner(boolean byEmail) {
        String msg = byEmail ? "Finding player by given email ID..." : "Finding all players with given name...";
        mSpinner = new Spinner(this.getContext());
        mSpinner.show("Search", msg, false);
    }

    private void setAndUpdate(List<Player> searchedPlayers) {
        Game.getInstance().setSearchedPlayer(searchedPlayers);
        mListener.updateSearchResult();
    }

    public void fetchPlayerByEmailID(final String emailID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        String fmtEmail = emailID.replaceAll("\\.", "");
        final Player player = Player.getDummyPlayer();
        Query gameQuery = ref.child("emails").equalTo(fmtEmail);

        gameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //todo:SAM: why do we need a for loop here?
                String playerName = null;
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    player.setEmailID(emailID);
                    player.setName(userSnapshot.getValue().toString());
                }
                setAndUpdate(Arrays.asList(player));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("GAMES", "loadGames:onCancelled", databaseError.toException());
            }
        });

    }

    private void fetchPlayerListContainingUserName(final String queriedName) {
        final List<String> playerNames = new ArrayList<>();
        String usersReference = "users/";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(usersReference);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Push the names to our result List.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    playerNames.add(snapshot.getKey());
                }
                getMatchingNames(playerNames, queriedName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FirebaseHelper", "getAllPlayerNames:onCancelled");
            }
        });


    }

    private void getMatchingNames(List<String> allNames, String queriedName) {
        List<Player> playerList = new ArrayList<>();    // To store our player objects.
        String queriedNameLowerCase = queriedName.toLowerCase();
        for (String name : allNames) {
            if (name.toLowerCase().contains(queriedNameLowerCase)) {
                // Here we are creating "default" player objects because we only need their names.
                Player newPlayerResult = Player.getDummyPlayer();
                newPlayerResult.setName(name);
                //todo:SAM: Leave the email id. I just figure out that it won't be used
                playerList.add(newPlayerResult);
            }
        }
        mSpinner.dismiss();
        setAndUpdate(playerList);
    }

}
