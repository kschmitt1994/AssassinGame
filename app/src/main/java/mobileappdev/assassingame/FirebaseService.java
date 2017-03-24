package mobileappdev.assassingame;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseService extends Service {

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

    public FirebaseService() {

        // FirebaseDatabase f =

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
