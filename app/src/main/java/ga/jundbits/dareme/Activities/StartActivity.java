package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import ga.jundbits.dareme.R;

public class StartActivity extends AppCompatActivity {

    private ConstraintLayout startLayout;
    private Button startPlayerButton, startWatcherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initVars();
        startAnimation();
        setOnClicks();

    }

    private void initVars() {

        startLayout = findViewById(R.id.start_layout);
        startPlayerButton = findViewById(R.id.start_player_button);
        startWatcherButton = findViewById(R.id.start_watcher_button);

    }

    private void startAnimation() {

        AnimationDrawable animDrawable = (AnimationDrawable) startLayout.getBackground();
        animDrawable.setEnterFadeDuration(10);
        animDrawable.setExitFadeDuration(1000);
        animDrawable.start();

    }

    private void setOnClicks() {

        startPlayerButton.setOnClickListener(v -> launchRegisterActivity("player"));
        startWatcherButton.setOnClickListener(v -> launchRegisterActivity("watcher"));

    }

    private void launchRegisterActivity(String userType) {

        Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
        registerIntent.putExtra("user_type", userType);
        startActivity(registerIntent);

    }

}
