package mobileappdev.assassingame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/13/2017.
 */

public class InvitedPlayersFragment extends Fragment {

    private RecyclerView mInvitedPlayersRecyclerView;
    private IPAdapter mIPAdapter;
    private DatabaseHandler mDatabaseHandler;
    private List<Player> mPlayers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invited_players, container, false);
        mInvitedPlayersRecyclerView = (RecyclerView) view.findViewById(R.id.player_recycler_view);
        mInvitedPlayersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDatabaseHandler = new DatabaseHandler(this.getActivity(), "ABC");
        mPlayers = mDatabaseHandler.getAllPlayers();
        updateUI();
        return view;
    }

    private void updateUI() {
//        Game instance = Game.getInstance(getActivity());
//        LinkedList<Player> players = instance.getPlayers();
        mPlayers = mDatabaseHandler.getAllPlayers();
        mIPAdapter = new IPAdapter(mPlayers);
        mInvitedPlayersRecyclerView.setAdapter(mIPAdapter);
    }

    public void update() {
        updateUI();
    }

    private class IPHolder extends RecyclerView.ViewHolder {
        private TextView mNameTextView;
        private Button mRemoveButton;
        IPHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.player_name);
            mRemoveButton = (Button) itemView.findViewById(R.id.checkBox);
            mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePlayer(getAdapterPosition());
                }
            });
        }
    }

    private void removePlayer(int position) {
//        Game instance = Game.getInstance(getActivity());
//        LinkedList<Player> players = instance.getPlayers();
//        Player player = players.get(position);
        mDatabaseHandler.deletePlayer(mPlayers.get(position).getEmailID());
        mPlayers.remove(position);
        updateUI();
    }

    private class IPAdapter extends RecyclerView.Adapter<IPHolder> {
        private List<Player> mPlayers;

        IPAdapter(List<Player> players) {
            mPlayers = players;
        }

        @Override
        public IPHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.fragment_player_selection, parent, false);
            return new IPHolder(view);
        }

        @Override
        public void onBindViewHolder(IPHolder holder, int position) {
            Player player = mPlayers.get(position);
            holder.mNameTextView.setText(player.getName());
            holder.mRemoveButton.setEnabled(true);
        }

        @Override
        public int getItemCount() {
            return mPlayers.size();
        }
}
}
