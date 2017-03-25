package mobileappdev.assassingame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by kennyschmitt on 3/25/17.
 */

public class PostgameActivity extends AppCompatActivity {

    private TextView mWinnerTextView;
    private TextView mLoserTextView;
    private Button mMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postgame);


        mWinnerTextView = (TextView) findViewById(R.id.winning_team);

        mLoserTextView = (TextView) findViewById(R.id.losing_team);

        mWinnerTextView.setText("");

        mLoserTextView.setText("");

        //Todo: pull game instance data to find winner and loser.

        mMenuButton = (Button) findViewById(R.id.main_menu_button);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostgameActivity.this, MainActivity.class));
            }
        });
    }

}
