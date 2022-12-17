package ga.jundbits.dareme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    ConstraintLayout startLayout;
    Button startPlayerButton, startWatcherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        startLayout = findViewById(R.id.start_layout);
        startPlayerButton = findViewById(R.id.start_player_button);
        startWatcherButton = findViewById(R.id.start_watcher_button);

        AnimationDrawable animDrawable = (AnimationDrawable) startLayout.getBackground();
        animDrawable.setEnterFadeDuration(10);
        animDrawable.setExitFadeDuration(1000);
        animDrawable.start();

        startPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchRegisterActivity("player");
            }
        });

        startWatcherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchRegisterActivity("watcher");
            }
        });

    }

    private void launchRegisterActivity(String userType) {

        Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
        registerIntent.putExtra("user_type", userType);
        startActivity(registerIntent);
        finish();

    }

}