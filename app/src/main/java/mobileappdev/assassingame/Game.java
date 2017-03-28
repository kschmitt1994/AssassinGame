package mobileappdev.assassingame;

import android.content.Context;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/17/2017
 */

public class Game {

    private boolean mIsPublic;
    private String mGameName;
    private String gameAdmin;
    private Set<String> players2Invite = new HashSet<>();

    private List<Player> mSearchedPlayer;


    public Set<String> getPlayers2Invite() {
        return players2Invite;
    }

    public void addPlayer2Invite(String player) {
        players2Invite.add(player);
    }


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

    public void setGameAdmin(String gameAdmin) {
        this.gameAdmin = gameAdmin;
    }

    public String getGameAdmin() {

        return gameAdmin;
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

    public void setGameName(String gameName) {
        mGameName = gameName;
    }
}
