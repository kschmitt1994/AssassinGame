package mobileappdev.assassingame;

import android.util.Log;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/14/2017
 */

public enum GameCharacter {
    ASSASSIN, CITIZEN, DETECTIVE, DOCTOR, UNDEFINED;

    public static GameCharacter getCharacterFrom(String character) {
        switch (character) {
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
            case "UNDEFINED":
            case "Undefined":
            case "undefined":
                return UNDEFINED;
            default: {
                Log.w("Ajit11", "character found: " + character);
                throw new RuntimeException(character + "No such GameCharacter in the game ");

            }
        }
    }
}
