package mobileappdev.assassingame;

import android.location.Location;

/**
 * Created by Ajit Ku. Sahoo on 3/14/2017.
 */

public class Player {
    private String mName;
    private String mEmailID;
    private boolean mAlive;
    private Character mCharacterType;
    private Location mLocation;

    public Player(String name, String emailID, boolean alive) {
        mName = name;
        mEmailID = emailID;
        mAlive = alive;
        mCharacterType = Character.CITIZEN;
        mLocation = null;
    }

    public String getName() {
        return mName;
    }

    public String getEmailID() {
        return mEmailID;
    }

    public boolean isAlive() {
        return mAlive;
    }

    public Character getCharacterType() {
        return mCharacterType;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setAlive(boolean alive) {
        mAlive = alive;
    }

    public void setCharacterType(Character characterType) {
        mCharacterType = characterType;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    private static int counter = 0;
    public static Player getDummyPlayer() {
        ++counter;
        return new Player("Ajit " + counter, "abc" + counter + "@xyz.com", true);
    }
}
