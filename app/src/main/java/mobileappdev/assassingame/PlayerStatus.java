package mobileappdev.assassingame;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/18/2017
 */

public enum PlayerStatus {
    ALIVE, DEAD, LEFT; //LEFT = left game

    public static PlayerStatus getPlayerStatus(String status) {
        switch (status) {
            case "ALIVE":
            case "Alive":
            case "alive":
                return ALIVE;
            case "DEAD":
            case "dead":
            case "Dead":
                return DEAD;
            case "LEFT":
            case "Left":
            case "left":
                return LEFT;
            default:
                throw new RuntimeException("No such player status defined");

        }
    }
}
