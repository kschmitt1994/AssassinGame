package mobileappdev.assassingame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Assassin";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Create method is invoked.");
        setContentView(R.layout.activity_main);

        Button createGameButton = (Button)findViewById(R.id.create_game);
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGame();
            }
        });

    }

    private void createGame() {
        Log.i(TAG, "Inside Create Game screen.");
        startActivity(new Intent(MainActivity.this, CreateGameActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Start method is invoked.");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Stop method is invoked.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy method is invoked.");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Pause method is invoked.");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Resume method is invoked.");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "Restart method is invoked.");

    }


}
