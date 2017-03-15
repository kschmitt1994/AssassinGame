package mobileappdev.assassingame;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Ajit Ku. Sahoo on 3/13/2017.
 */

public class SearchPlayerResultFragment extends Fragment {

    private InvitedPlayerListChangeListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        View view = inflater.inflate(R.layout.fragment_search_player_result, container, false);

        TextView searchResultTV = (TextView) view.findViewById(R.id.search_result_TV);
        final Button addButton = (Button) view.findViewById(R.id.add_player_button);
        setComponentsVisibility(new View[]{searchResultTV, addButton}, View.INVISIBLE);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Game instance = Game.getInstance(getActivity());
                Player searchedPlayer = instance.getSearchedPlayer();
                instance.addPlayer(searchedPlayer);
                addButton.setEnabled(false);
                mListener.playerInvited();
            }
        });

        return view;
    }

    public void update() {
        Game instance = Game.getInstance(getActivity());
        TextView searchResultTV = (TextView) getView().findViewById(R.id.search_result_TV);
        Button addButton = (Button) getView().findViewById(R.id.add_player_button);

        if (instance.getSearchedPlayer() != null) {
            addButton.setEnabled(true);
            searchResultTV.setText(instance.getSearchedPlayer().getName());
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
