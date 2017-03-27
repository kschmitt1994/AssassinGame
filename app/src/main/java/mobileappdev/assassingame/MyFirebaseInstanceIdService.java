package mobileappdev.assassingame;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by kennyschmitt on 3/23/17.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private static final String CHAT_ENGAGE_TOPIC = "chat_engage";

    /**
     * The Application's current Instance ID token is no longer valid and thus a new one must be requested.
     */
    public void onTokenRefresh() {
        // If you need to handle the generation of a token, initially or
        // after a refresh this is where you should do that.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FCM Token: " + token);

        // Once a token is generated, we subscribe to topic.
    FirebaseMessaging.getInstance()
                .subscribeToTopic(CHAT_ENGAGE_TOPIC);
    }
}
