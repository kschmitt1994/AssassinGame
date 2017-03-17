package mobileappdev.assassingame;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ajit Ku. Sahoo on 3/14/2017.
 */

public class Game {

    private boolean mIsPublic;
    private String mGameName;
    private List<Player> mSearchedPlayer;

    private static Game sGame;

    private Game() {

    }

    public static Game getInstance() {
        if (sGame == null) {
            sGame = new Game();
        }
        return sGame;
    }

    public String getGameName() {
        return mGameName;
    }

    public boolean isPublic() {
        return mIsPublic;
    }

    public void setPublic(boolean value) {
        mIsPublic = value;
    }

    public List<Player> getSearchedPlayer() {
        return mSearchedPlayer;
    }

    public void setSearchedPlayer(List<Player> searchedPlayer) {
        mSearchedPlayer = searchedPlayer;
    }
}
