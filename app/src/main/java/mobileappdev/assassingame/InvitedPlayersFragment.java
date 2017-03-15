package mobileappdev.assassingame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/13/2017.
 */

public class InvitedPlayersFragment extends Fragment {

    private RecyclerView mInvitedPlayersRecyclerView;
    private IPAdapter mIPAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invited_players, container, false);
        mInvitedPlayersRecyclerView = (RecyclerView) view.findViewById(R.id.player_recycler_view);
        mInvitedPlayersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    private void updateUI() {
        Game instance = Game.getInstance(getActivity());
        LinkedList<Player> players = instance.getPlayers();
        mIPAdapter = new IPAdapter(players);
        mInvitedPlayersRecyclerView.setAdapter(mIPAdapter);
    }

    public void update() {
        updateUI();
    }

    private class IPHolder extends RecyclerView.ViewHolder {
        private TextView mNameTextView;
        private Button mCheckBox;
        private int mPosition;
        IPHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.player_name);
            mCheckBox = (Button) itemView.findViewById(R.id.checkBox);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePlayer(mPosition);
                }
            });
        }
    }

    private void removePlayer(int position) {
        Game instance = Game.getInstance(getActivity());
        LinkedList<Player> players = instance.getPlayers();
        players.remove(position);
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
            holder.mCheckBox.setEnabled(true);
            holder.mPosition = position;
        }

        @Override
        public int getItemCount() {
            return mPlayers.size();
        }
}
}
