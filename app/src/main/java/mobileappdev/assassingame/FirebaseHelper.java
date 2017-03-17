package mobileappdev.assassingame;

import java.util.ArrayList;
import java.util.List;

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

    public static void getPlayer(String emailID) {

    }


    public static List<String> getAllPlayerNames() {
        return new ArrayList<>();
    }


}
