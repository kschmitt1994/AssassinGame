package mobileappdev.assassingame;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseService extends FirebaseInstanceIdService {

    /*
     * This service will primarily be tasked with delivering messages and notifications to users
     * as they use our app. These include notices such as:
     *
     *     - Chat messages (when you're alive)
     *     - Game start notifications
     *     - Game end notifications
     *     - Invitations to play
     *
     * Each of these will be wired up to our Firebase backend. For more information, a lot of the
     * logic is described in the FirebaseHelper class.
     */

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FirebaseService", "Refreshed token: " + refreshedToken);

        // Here we are pushing the token to Firebase.

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deviceRef = database.getReference("users/" +
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "/devices");
        DatabaseReference newDeviceRef = deviceRef.push();

        newDeviceRef.setValue(refreshedToken);
    }

    public FirebaseService() {

    }
}
