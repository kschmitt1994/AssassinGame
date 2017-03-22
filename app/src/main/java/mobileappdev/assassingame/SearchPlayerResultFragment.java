package mobileappdev.assassingame;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/15/2017
 */

public class SearchPlayerResultFragment extends Fragment {

    private InvitedPlayerListChangeListener mListener;
    private DatabaseHandler mDatabaseHandler;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    ArrayList<String> mItems;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItems = new ArrayList<>();
        mDatabaseHandler = new DatabaseHandler(this.getActivity(), Game.getInstance().getGameName());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (InvitedPlayerListChangeListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_player_results, container, false);

        mListView = (ListView)view.findViewById(R.id.search_results_list_view);
        mAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mItems);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                instance.addPlayer(searchedPlayer);
                String playerName = (String) parent.getAdapter().getItem(position);
                Toast.makeText(getActivity(), playerName + " is added!", Toast.LENGTH_LONG).show();

                /* FROM SAM: We just need to push the list of players to Firebase. */

                // TODO:Ajit add actual player to database
                Game instance = Game.getInstance();
                Player searchedPlayer = instance.getSearchedPlayer().get(0);
                // TODO: Ajit we can get below player by iterating over searchedPlayer list which contains result from firebase during search
                Player player = FirebaseHelper.getPlayerListContainingUserName(playerName).get(0);//it's unique. size(list) = 1
                mDatabaseHandler.addPlayer(searchedPlayer);
                FirebaseHelper.sendInvite(searchedPlayer); //send invite to the player who got added

                mItems.remove(searchedPlayer.getName());
                mAdapter.notifyDataSetChanged();

                mListener.update();
            }
        });

        /*TextView searchResultTV = (TextView) view.findViewById(R.id.search_result_TV);
        final Button addButton = (Button) view.findViewById(R.id.add_player_button);
        setComponentsVisibility(new View[]{searchResultTV, addButton}, View.INVISIBLE);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Game instance = Game.getInstance();
                Player searchedPlayer = instance.getSearchedPlayer().get(0);
//                instance.addPlayer(searchedPlayer);
                mDatabaseHandler.addPlayer(searchedPlayer);
                addButton.setEnabled(false);
                mListener.update();
            }
        });
*/
        return view;
    }

    public void update() {
        Game instance = Game.getInstance();
        List<Player> searchedPlayer = instance.getSearchedPlayer();
        //TODO:Ajit: fix with actual data
        mItems.add(searchedPlayer.get(0).getName());
        mAdapter.notifyDataSetChanged();
    }

    public void update1() {
        Game instance = Game.getInstance();
        TextView searchResultTV = (TextView) getView().findViewById(R.id.search_result_TV);
        Button addButton = (Button) getView().findViewById(R.id.add_player_button);

        if (instance.getSearchedPlayer() != null) {
            addButton.setEnabled(true);
            searchResultTV.setText(instance.getSearchedPlayer().get(0).getName());
            setComponentsVisibility(new View[]{searchResultTV, addButton}, View.VISIBLE);
        } else {
            setComponentsVisibility(new View[]{searchResultTV, addButton}, View.INVISIBLE);
        }


    }

    public void setComponentsVisibility(View[] components, int visibility) {
        for (View v : components) {
            v.setVisibility(visibility);
        }
    }
}
