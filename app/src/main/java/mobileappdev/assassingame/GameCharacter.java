package mobileappdev.assassingame;

/**
 * Created by Ajit Ku. Sahoo on 3/14/2017.
 */

public enum GameCharacter {
    ASSASSIN, CITIZEN, DETECTIVE, DOCTOR;

    public static GameCharacter getCharacterFrom(String string) {
        switch (string) {
            case "CITIZEN" :
            case "Citizen" :
            case "citizen" :
                return CITIZEN;
            case "ASSASSIN":
            case "assassin":
            case "Assassin":
                return ASSASSIN;
            case "DETECTIVE":
            case "detective":
            case "Detective":
                return DETECTIVE;
            case "DOCTOR":
            case "doctor":
            case "Doctor":
                return DOCTOR;
            default:
                throw new RuntimeException("No such GameCharacter in the game");
        }
    }
}
