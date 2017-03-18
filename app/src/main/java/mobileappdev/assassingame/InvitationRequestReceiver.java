package mobileappdev.assassingame;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/17/2017
 */

public class InvitationRequestReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BroadcastHelper.INVITE_REQUEST)){ //TODO: to be broadcasted from within firebase receive msg
            addNotification(context, intent);
        } else if (action.equals(BroadcastHelper.REJECT_ACTION)){
            FirebaseHelper.sendRejectionResponse(intent.getStringExtra("sender"));
        } else if (action.equals(BroadcastHelper.ACCEPT_ACTION)){
            FirebaseHelper.sendAcceptResponse(intent.getStringExtra("sender"));
            context.startActivity(new Intent(context, PlayBoardActivity.class));
        } else if (action.equals(BroadcastHelper.GAME_START)) {

            Intent intent1 = new Intent(context, PlayBoardActivity.class);
            intent1.putExtra("gamestarted", true);
            context.startActivity(intent1);
        }
    }

    public static void addNotification(Context context, Intent intent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        mBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        mBuilder.setContentTitle("Assassin Invitation !");
        mBuilder.setContentText("Please join me. From - " + intent.getStringExtra("sender")); // TODO: 3/18/2017 show sender name
        mBuilder.setAutoCancel(true);

        //Accept intent
        Intent yesReceive = new Intent(context, PlayBoardActivity.class); //TODO: how to send broadcast from here
        yesReceive.putExtra("sender", intent.getStringExtra("sender"));
        yesReceive.setAction(BroadcastHelper.ACCEPT_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.common_google_signin_btn_text_dark, "Accept", pendingIntentYes);

        //Reject intent
        Intent noReceive = new Intent(); //TODO: how to send reject broadcast
        noReceive.putExtra("sender", intent.getStringExtra("sender"));
        noReceive.setAction(BroadcastHelper.REJECT_ACTION);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(context, 12345, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.common_google_signin_btn_text_dark, "Reject", pendingIntentNo);


       /* Intent resultIntent = new Intent(context, PlayBoardActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PlayBoardActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);*/
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }
}
