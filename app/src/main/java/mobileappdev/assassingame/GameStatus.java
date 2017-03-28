package mobileappdev.assassingame;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/18/2017
 */

public enum GameStatus {

    STARTED, FINISHED;

    public static GameStatus getGameStatusFrom(String string) {
        switch (string) {
            case "STARTED" :
            case "started" :
            case "Started" :
                return STARTED;
            case "FINISHED":
            case "finished":
            case "Finished":
                return FINISHED;
            default:
                throw new RuntimeException("No such Game Status");
        }
    }
}
