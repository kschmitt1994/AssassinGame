package mobileappdev.assassingame;

import android.content.Context;

import java.util.LinkedList;

/**
 * Created by Ajit Ku. Sahoo on 3/14/2017.
 */

public class Game {

    private String mGameName;
    private boolean mIsPublic;
    private Player mSearchedPlayer;
    private LinkedList<Player> mPlayers;
    //private GoogleMap mGoogleMap;

    private static Game sGame;

    private Game(Context context) {
        mPlayers = new LinkedList<>();
    }

    public static Game getInstance(Context context) {
        if (sGame == null) {
            sGame = new Game(context);
        }
        return sGame;
    }

    public String getGameName() {
        return mGameName;
    }

    public LinkedList<Player> getPlayers() {
        return mPlayers;
    }

    public boolean isPublic() {
        return mIsPublic;
    }

    public void setGameName(String gameName) {
        mGameName = gameName;
    }

    public void addPlayers(Player player) {
        mPlayers.add(player);
    }

    public void setPublic(boolean value) {
        mIsPublic = value;
    }

    public void addPlayer(Player player) {
        mPlayers.add(player);
    }

    public Player getSearchedPlayer() {
        return mSearchedPlayer;
    }

    public void setSearchedPlayer(Player searchedPlayer) {
        mSearchedPlayer = searchedPlayer;
    }
}
