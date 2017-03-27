package mobileappdev.assassingame;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/17/2017
 */

public class InvitationRequestReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //if not logged in
        //return;
        String action = intent.getAction();
        if (action.equals(BroadcastHelper.INVITE_REQUEST)){ //TODO: to be broadcasted from within firebase receive msg
            addNotification(context, intent);

        /*} else if (action.equals(BroadcastHelper.REJECT_ACTION)){
            FirebaseHelper.sendRejectionResponse(intent.getStringExtra("sender"));

        } else if (action.equals(BroadcastHelper.ACCEPT_ACTION)){
            FirebaseHelper.sendAcceptResponse(intent.getStringExtra(BroadcastHelper.SENDER));
            Intent intent1 = new Intent(context, LogInActivity.class);
            intent1.putExtra(BroadcastHelper.ON_GAME_REQUEST, true);
            intent1.putExtra(BroadcastHelper.GAME_NAME, intent.getStringExtra(BroadcastHelper.GAME_NAME));
            context.startActivity(intent1);*/

        } else if (action.equals(BroadcastHelper.GAME_START)) {
            //if user is not logged in, then he will not receive any notification
            SharedPreferences sharedPreferences = context.getSharedPreferences(LogInActivity.MY_PREFERENCES, Context.MODE_PRIVATE);
            if (!sharedPreferences.getBoolean(LogInActivity.IS_USER_LOGGED_IN, false)) {
                String player = intent.getStringExtra(BroadcastHelper.SENDER);
                String admin = intent.getStringExtra(BroadcastHelper.ADMIN);
                FirebaseHelper.sendPlayerNotLoggedInResponse(player, admin);
                return;
            }

            Intent intent1 = new Intent(context, PlayBoardActivity.class);
            intent1.putExtra(BroadcastHelper.GAME_STARTED, true);
            intent1.putExtra(BroadcastHelper.GAME_NAME, intent.getStringExtra(BroadcastHelper.GAME_NAME));
            context.startActivity(intent1);
        }
    }

    public static void addNotification(Context context, Intent intent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        mBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        mBuilder.setContentTitle("Join my game of Assassin!");
        mBuilder.setContentText(intent.getStringExtra(BroadcastHelper.ADMIN) + " wants you to join their game, " + intent.getStringExtra(BroadcastHelper.GAME_NAME));
        mBuilder.setAutoCancel(true);

        //Accept intent
        Intent yesReceive = new Intent(context, PlayBoardActivity.class);
        yesReceive.putExtra(BroadcastHelper.ON_GAME_REQUEST, true);
        yesReceive.putExtra(BroadcastHelper.INVITATION_RESPONSE, InvitationStatus.ACCEPTED);
        yesReceive.putExtra(BroadcastHelper.ADMIN, intent.getStringExtra(BroadcastHelper.ADMIN));
        yesReceive.putExtra(BroadcastHelper.PLAYER_NAME, intent.getStringExtra(BroadcastHelper.PLAYER_NAME));
        yesReceive.putExtra(BroadcastHelper.GAME_NAME, intent.getStringExtra(BroadcastHelper.GAME_NAME));
        PendingIntent pendingIntentYes = PendingIntent.getActivity(context, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.common_google_signin_btn_text_dark, "Accept", pendingIntentYes);

        //Reject intent
        Intent noReceive = new Intent(context, PlayBoardActivity.class);
        noReceive.putExtra(BroadcastHelper.ON_GAME_REQUEST, true);
        noReceive.putExtra(BroadcastHelper.INVITATION_RESPONSE, InvitationStatus.DECLINED);
        noReceive.putExtra(BroadcastHelper.ADMIN, intent.getStringExtra(BroadcastHelper.ADMIN));
        noReceive.putExtra(BroadcastHelper.PLAYER_NAME, intent.getStringExtra(BroadcastHelper.PLAYER_NAME));
        noReceive.putExtra(BroadcastHelper.GAME_NAME, intent.getStringExtra(BroadcastHelper.GAME_NAME));

        // NOTE: I had to change the number literal; this has to be unique for every pending activity.
        PendingIntent pendingIntentNo = PendingIntent.getActivity(context, 12346, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.common_google_signin_btn_text_dark, "Decline", pendingIntentNo);


       /* Intent resultIntent = new Intent(context, PlayBoardActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PlayBoardActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);*/

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
