package mobileappdev.assassingame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/17/2017
 */

public class Game {

    private boolean mIsPublic;
    private String mGameName;
    private String gameAdmin;
    private Set<String> players2Invite;
    private List<Player> mSearchedPlayer;
    private Map<String, Player> allPlayers;


    private static Game sGame;

    private Game() {
        players2Invite = new HashSet<>();
        allPlayers = new HashMap<>();
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

    public List<Player> getAllPlayers() {
        return new ArrayList<>(allPlayers.values());
    }

    public List<String> getAllPlayerNames() {
        return new ArrayList<>(allPlayers.keySet());
    }

    public Map<String, Player> getName2PlayerMap() {
        return allPlayers;
    }

    public void setName2PlayerMap(Map<String, Player> map) {
        allPlayers = map;
    }

    public void addPlayer(Player player) {
        allPlayers.put(player.getName(), player);
    }

    public void removePlayer(String player) {
        allPlayers.remove(player);
    }

    public void removeAllPlayers() {
        allPlayers = new HashMap<>();
    }

    public Set<String> getPlayers2Invite() {
        return players2Invite;
    }

    public void addPlayer2Invite(String player) {
        players2Invite.add(player);
    }

    public void resetGameData() {
        players2Invite = new HashSet<>();
        mSearchedPlayer = new ArrayList<>();
        allPlayers = new HashMap<>();
    }

}
