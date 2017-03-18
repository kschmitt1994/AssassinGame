package mobileappdev.assassingame;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/17/2017
 */

public enum InvitationStatus {
    UNDEFINED, ACCEPTED, DECLINED, INVITED;

    public static InvitationStatus getStatusFrom(String string) {
        switch (string) {
            case "UNDEFINED" :
                return UNDEFINED;
            case "ACCEPTED":
                return ACCEPTED;
            case "DECLINED":
                return DECLINED;
            case "INVITED":
                return INVITED;
            default:
                throw new RuntimeException("Invalid Invitation Status. ");
        }
    }
}
