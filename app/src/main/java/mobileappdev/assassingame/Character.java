package mobileappdev.assassingame;

/**
 * Created by Ajit Ku. Sahoo on 3/14/2017.
 */

public enum Character {
    ASSASSIN, CITIZEN, DETECTIVE, DOCTOR;

    public static Character getCharacterFrom(String string) {
        switch (string) {
            case "CITIZEN" :
                return CITIZEN;
            case "ASSASSIN":
                return ASSASSIN;
            case "DETECTIVE":
                return DETECTIVE;
            case "DOCTOR":
                return DOCTOR;
            default:
                throw new RuntimeException("No such Character in the game");
        }
    }
}
