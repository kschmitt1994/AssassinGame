package mobileappdev.assassingame;

import android.location.Location;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/17/2017
 */

public class FirebaseHelper {

    // This is a bit of a hacky workaround so that Firebase is
    // compatible with our FirebaseHelper API
    private static StringBuffer result = new StringBuffer();

    public static void createGame(Game newGame) {
        String gameReference = "games/" + newGame.getGameName();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference typeRef = database.getReference(gameReference + "/type");
        DatabaseReference creatorRef = database.getReference(gameReference + "/creator");
        DatabaseReference playersRef = database.getReference(gameReference + "/players");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String creatorName = user.getDisplayName();
            creatorRef.setValue(creatorName);
        }

        // Public or private
        if (newGame.isPublic()) {
            typeRef.setValue("public");
        } else {
            typeRef.setValue("private");
        }

        // Adding player names to our newly-created game!
        DatabaseReference newPlayerRef = playersRef.push();
        playersRef.setValue(newGame.getSearchedPlayer());
    }

    public static boolean isGamePublic(String gameName) {

        /*
         * Because the game type at this point is already determined, we don't need to waste
         * resources by listening for changes. The reference method addListenerForSingleValueEvent()
         * handles this for us, and we use a StringBuffer to store the result of that query for use
         * in this function. A similar approach is used in other "get" methods so that I don't have
         * to change the FirebaseHelper class.
         */

        // Establish reference to Firebase based on gameName attribute
        String gameTypeReference = "games/" + gameName + "/type";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(gameTypeReference);

        // Listen for single value then destroy listener
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean value = (Boolean) dataSnapshot.getValue();
                FirebaseHelper.result.append(value);
                Log.d("FIREBASE HELPER", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FIREBASE HELPER", "Failed to read value.", databaseError.toException());
            }
        });

        // Implementing the hacky workaround
        Boolean isPublic = result.toString().equals("public");

        // Resetting string buffer
        result.setLength(0);

        return isPublic;
    }

    // What exactly would we need this for?
    public static void setGamePublic(String gameName) {

        // Establishing reference to Firebase based on gameName attribute
        String gameTypeReference = "games/" + gameName + "/type";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(gameTypeReference);

        // Much simpler than isGamePublic, right?
        ref.setValue("public");
    }

    public static List<String> getAllGameNames() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        Query gameQuery = ref.child("games");
        final List<String> gameNames = new ArrayList<String>();

        gameQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot gameSnapshot: dataSnapshot.getChildren()) {
                    gameNames.add(gameSnapshot.getKey()); // Because game names are used as keys
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("GAMES", "loadGames:onCancelled", databaseError.toException());
                // ...
            }
        });

        return gameNames;
    }


    public static void sendInvite(Player player) {
        // TODO: 3/17/2017 broadcast the msg
    }

    /**
     * Find a player based on their email address. Note: the result must be an exact match in
     * order to protect the privacy of our users. ALSO NOTE: We are doing some silly formatting
     * of the email because Firebase keys cannot contain certain characters (like '.').
     *
     * @param emailID: email address of player we are looking for
     * @return Newly-created Player object referring to queried user
     */
    public static Player getPlayerByEmailID(String emailID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        // Get rid of periods for valid Firebase keys
        String fmtEmail = emailID.replaceAll("\\.", "");
        final String originalEmail = emailID;


        Query gameQuery = ref.child("emails").equalTo(fmtEmail);
        final Player playerQueryResult = Player.getDummyPlayer();

        gameQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    playerQueryResult.setEmailID(originalEmail);
                    playerQueryResult.setName(userSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("GAMES", "loadGames:onCancelled", databaseError.toException());
                // ...
            }
        });

        return playerQueryResult;
    }


    /**
     * Find a player based on their username. Note: unlike email addresses, this does not have
     * to be an exact match. It would be fairly easy to change that though.
     *
     * @param userName: username of player we are looking for
     * @return List<Player>: Newly-created Player list referring to queried users
     */
    public static List<Player> getPlayerListContainingUserName(String userName) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        Query gameQuery = ref.child("users").startAt(userName);
        final ArrayList<Player> playerQueryResult = new ArrayList<>();

        gameQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    Player newPlayer = new Player(
                            userSnapshot.getKey(),
                            userSnapshot.getValue().toString(),
                            null, true, null);
                    playerQueryResult.add(newPlayer);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("USERS", "getPlayerListContainingUserName:onCancelled",
                        databaseError.toException());
            }
        });

        return playerQueryResult;
    }


    public static List<String> getAllPlayerNames() {
        // TODO: 3/17/2017 return actual list
        // TODO: 3/20/2017 What do we need this for?
        return new ArrayList<>();
    }


    public static void sendLocation(Location location, String gameName, String myself) {
        // TODO: 3/18/2017 need to send location to all of the players of the given game
    }

    public static void sendRejectionResponse(String sender) {
        // TODO: 3/18/2017 need to send out a reject message through firebase to the sender
    }

    /**
     * @param fromPlayer: The user that accepted the invitation.
     * @param toAdmin: The message that will be broadcasted to the game admin.
     */
    public static void sendAcceptResponse(String fromPlayer, String toAdmin) {
        // TODO: 3/18/2017 add the player to the game in firebase
        // TODO: 3/18/2017 send acceptance response to the admin

    }

    public static void sendGameStartMessage(String gameName) {
        // TODO: 3/18/2017 1. set status of game as started. use enum GameStatus
        // TODO: 3/18/2017 2. need to send a message to all the players of this game that game is started
    }

    /**
     * Retrieves the game status as a string from the Firebase realtime database. Converts this
     * result to a GameStatus object.
     * @param gameName: game whose status we are checking
     * @return GameStatus object corresponding to queried game
     */
    public static GameStatus getGameStatus(String gameName) {
        String gameTypeReference = "games/" + gameName + "/status";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(gameTypeReference);
        final StringBuffer status = new StringBuffer();

        // Listen for single value then destroy listener
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String queriedGameStatus = (String) dataSnapshot.getValue();
                status.append(queriedGameStatus);
                Log.d("FIREBASE HELPER", "Value is: " + queriedGameStatus);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FIREBASE HELPER", "Failed to read value.", databaseError.toException());
            }
        });

        GameStatus gameStatus = GameStatus.getCharacterFrom(status.toString());
        return gameStatus;
    }

    /**
     * Helper method to determine if the game has started.
     * @param gameName: game in question
     * @return whether or not game has started
     */
    public static boolean isGameStarted(String gameName) {
        return GameStatus.STARTED.equals(getGameStatus(gameName)); //dummy value
    }

    /**
     * Helper method to determine if the game has finished.
     * @param gameName: game in question
     * @return whether or not game has finished
     */
    public static boolean isGameFinished(String gameName) {
        return GameStatus.FINISHED.equals(getGameStatus(gameName)); //dummy value
    }

    public static Map<String, Player> getAllPlayers(String gameName) {
        //TODO:Sam: return all the players (playerName --> Player object) from firebase database for the given GameName
        return null;
    }

    public static void updatePlayerStatus(String gameName, String playerName, PlayerStatus status, boolean shouldUpdateCiviliansCounter) {
        //TODO:Sam: update the player to be dead/alive/left
        //TODO:Sam: decrease alive civlians counter only if the boolean flag is true,
        // which represents no of civilians left (excluding detective). when it becomes zero, Assassin wins the game.
    }

    public static int getNoOfAliveCivilians(String gameName) {
        //TODO:Sam: number of civilians alive (not dead/left) for the game
        return 1;
    }

    public static void initializeNoOfAliveCivilians(String gameName) {
        //TODO:Sam: number of civilians alive (not dead/left) for the game
    }

    public static void increaseNoOfAliveCiviliansBy1(String gameName) {
        //TODO:Sam: increase the counter for number of civilians alive (not dead/left) for the game
    }


    public static void updateGameStatus(String gameName, boolean assassinWon, String description) {
        //TODO:Sam: update the game status

    }

    public static void newPlayerAddedUp(String userName, String gameName) {
        //TODO:Sam: send this message to everyone in the game
    }

    public static void sendPlayerNotLoggedInResponse(String fromPlayer, String toAdmin) {
        //TODO:Sam: send this message to admin with info about fromPlayer
    }
}
