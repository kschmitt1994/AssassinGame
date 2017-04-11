package mobileappdev.assassingame;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Map;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 4/5/2017
 */

public class AsyncTaskCancelGame extends AsyncTask<Context, Void, Void> {
    @Override
    protected Void doInBackground(Context... params) {
        Game gameInstance = Game.getInstance();
        String gameName = gameInstance.getGameName();

        FirebaseHelper.deleteGame(gameName);

        Map<String, Player> name2PlayerMap = gameInstance.getName2PlayerMap();
        DatabaseHandler databaseHandler = new DatabaseHandler(params[0], gameName);
        for (String playerName : name2PlayerMap.keySet()) {
            databaseHandler.deletePlayer(playerName);
        }

        return null;
    }
}
