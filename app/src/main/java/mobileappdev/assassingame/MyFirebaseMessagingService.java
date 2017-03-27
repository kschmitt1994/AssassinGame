package mobileappdev.assassingame;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by kennyschmitt on 3/23/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseService";

    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "FCM Notification Message: " + remoteMessage.getNotification());
        Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());

        Map<String, String> payload = remoteMessage.getData();

        if (payload != null) {
            String payloadType = payload.get("type");

            switch (payloadType) {
                case "invitation":
                    Intent inviteIntent = new Intent();
                    inviteIntent.setAction(BroadcastHelper.INVITE_REQUEST);
                    inviteIntent.putExtra(BroadcastHelper.ADMIN, payload.get("sender"));
                    inviteIntent.putExtra(BroadcastHelper.GAME_NAME, payload.get("game"));
                    inviteIntent.putExtra(BroadcastHelper.PLAYER_NAME, payload.get("receiver"));
                    sendBroadcast(inviteIntent);
                    break;
                case "game_start":
                    Intent gameStartIntent = new Intent();
                    gameStartIntent.setAction(BroadcastHelper.GAME_START);
                    gameStartIntent.putExtra(BroadcastHelper.ADMIN, payload.get("admin"));
                    gameStartIntent.putExtra(BroadcastHelper.GAME_NAME, payload.get("game"));
                    gameStartIntent.putExtra(BroadcastHelper.SENDER, payload.get("sender"));
                default:
                    Log.d(TAG, "Intent error");
            }
        }

    }
}

