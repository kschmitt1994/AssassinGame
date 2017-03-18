package mobileappdev.assassingame;

import android.location.Location;

/**
 * Created by Ajit Ku. Sahoo on 3/14/2017.
 */

public class Player {
    private String mName;
    private String mEmailID;
    private boolean mAlive;
    private GameCharacter mGameCharacterType;
    private Location mLocation;

    private InvitationStatus mInvitationStatus;

    public Player(String name, String emailID, GameCharacter gameCharacter, boolean alive) {
        this(name, emailID, gameCharacter, alive, InvitationStatus.UNDEFINED);
    }
    public Player(String name, String emailID, GameCharacter gameCharacter, boolean alive, InvitationStatus status) {
        mName = name;
        mEmailID = emailID;
        mAlive = alive;
        mGameCharacterType = gameCharacter;
        mLocation = null;
        mInvitationStatus = status;
    }

    public InvitationStatus getInvitationStatus() {
        return mInvitationStatus;
    }

    public void setInvitationStatus(InvitationStatus invitationStatus) {
        mInvitationStatus = invitationStatus;
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

    public GameCharacter getGameCharacterType() {
        return mGameCharacterType;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setAlive(boolean alive) {
        mAlive = alive;
    }

    public void setGameCharacterType(GameCharacter gameCharacterType) {
        mGameCharacterType = gameCharacterType;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    private static int counter = 0;

    public static Player getDummyPlayer() {
        ++counter;
        InvitationStatus status = InvitationStatus.UNDEFINED;
        if (counter%4 == 0)
            status = InvitationStatus.ACCEPTED;
        else if (counter%4 == 1)
            status = InvitationStatus.DECLINED;
        else if (counter%4 == 2)
            status = InvitationStatus.UNDEFINED;
        else if (counter%4 == 3)
            status = InvitationStatus.INVITED;

        return new Player("Ajit " + counter, "abc" + counter + "@xyz.com", GameCharacter.CITIZEN, true, status);
    }
}
