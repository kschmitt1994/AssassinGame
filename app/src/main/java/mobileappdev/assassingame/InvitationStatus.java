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
            case "undefined" :
            case "Undefined" :
                return UNDEFINED;
            case "ACCEPTED":
            case "accepted":
            case "Accepted":
                return ACCEPTED;
            case "DECLINED":
            case "declined":
            case "Declined":
                return DECLINED;
            case "INVITED":
            case "invited":
            case "Invited":
                return INVITED;
            default:
                throw new RuntimeException("Invalid Invitation Status. ");
        }
    }
}
