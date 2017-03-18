package mobileappdev.assassingame;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/17/2017
 */

public class FirebaseHelper {

    public static boolean isGamePublic(String gameName) {
        return true;// TODO: 3/17/2017 fetch from firebase
    }

    public static void setGamePublic(String gameName) {
        // TODO: 3/17/2017 update in firebase
    }

    public static List<String> getAllGameNames() {
        //// TODO: 3/17/2017 return all the game names available in database
        return new ArrayList<>();
    }


    public static void sendInvite(Player player) {
        // TODO: 3/17/2017 broadcast the msg
    }

    public static Player getPlayerByEmailID(String emailID) {
        // TODO: 3/17/2017 Sam: replace the below dummy code
        return Player.getDummyPlayer();
    }


    /**
     * It may contain zero or many names containing the name segment
     * @param userName
     * @return
     */
    public static List<Player> getPlayerListContainingUserName(String userName) {
        // TODO: 3/17/2017 Sam: replace the below dummy code
        Player dummyPlayer = Player.getDummyPlayer();
        ArrayList<Player> searchedPlayers = new ArrayList<>();
        searchedPlayers.add(dummyPlayer);
        return searchedPlayers;

    }


    public static List<String> getAllPlayerNames() {

        // TODO: 3/17/2017 return actual list
        return new ArrayList<>();
    }


    public static void sendLocation(Location location) {
        // TODO: 3/18/2017 need to retrieve the game which I am a part of and then send location to all of the players
    }

    public static void sendRejectionResponse(String sender) {
        // TODO: 3/18/2017 need to send out a reject message through firebase to the sender
    }

    public static void sendAcceptResponse(String sender) {
        // TODO: 3/18/2017 add the player to the game in firebase
        // TODO: 3/18/2017 send acceptance response to the admin

    }

    public static void sendGameStartMessage(String gameName) {
        //// TODO: 3/18/2017 1. set status of game as started. use enum GameStatus
        // TODO: 3/18/2017 2. need to send a message to all the players of this game that game is started
    }


    public static GameStatus getGameStatus(String gameName) {
        //// TODO: 3/18/2017 return Game status.
        return GameStatus.STARTED; //dummy value.
    }

    public static boolean isGameStarted(String gameName) {
        return GameStatus.STARTED.equals(getGameStatus(gameName)); //dummy value
    }

    public static boolean isGameFinished(String gameName) {
        return GameStatus.FINISHED.equals(getGameStatus(gameName)); //dummy value
    }
}
