package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ga.jundbits.dareme.Adapters.PlayersRecyclerAdapter;
import ga.jundbits.dareme.Callbacks.OnPlayerClick;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.Models.User;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class NewChallengeActivity extends AppCompatActivity implements OnPlayerClick {

    private ConstraintLayout newChallengeConstraintLayout;
    private ProgressBar newChallengeProgressBar;
    private Toolbar newChallengeToolbar;
    private EditText newChallengePlayerUsername, newChallengeDescription, newChallengeReward;
    private RecyclerView newChallengePlayersRecyclerView;
    private Button newChallengeAddChallengeButton;

    private final List<User> playersList = new ArrayList<>();
    private PlayersRecyclerAdapter newChallengePlayersRecyclerAdapter;

    private User selectedPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);

        initVars();
        setupToolbar();
        setupAdapter();
        loadData();
        setOnClicks();

    }

    private void initVars() {

        newChallengeConstraintLayout = findViewById(R.id.new_challenge_constraint_layout);
        newChallengeProgressBar = findViewById(R.id.new_challenge_progress_bar);
        newChallengeToolbar = findViewById(R.id.new_challenge_toolbar);
        newChallengePlayerUsername = findViewById(R.id.new_challenge_player_username);
        newChallengeDescription = findViewById(R.id.new_challenge_description);
        newChallengeReward = findViewById(R.id.new_challenge_reward);
        newChallengePlayersRecyclerView = findViewById(R.id.new_challenge_players_recycler_view);
        newChallengeAddChallengeButton = findViewById(R.id.new_challenge_add_challenge_button);

    }

    private void setupToolbar() {

        setSupportActionBar(newChallengeToolbar);
        getSupportActionBar().setTitle(getString(R.string.new_challenge));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setupAdapter() {

        newChallengePlayersRecyclerAdapter = new PlayersRecyclerAdapter(this, playersList, this);

        newChallengePlayersRecyclerView.setHasFixedSize(true);
        newChallengePlayersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newChallengePlayersRecyclerView.setAdapter(newChallengePlayersRecyclerAdapter);

    }

    private void loadData() {

        FirebaseHelper.collectionReference("Users")
                .whereEqualTo("type", "player")
                .get().addOnSuccessListener(this, queryDocumentSnapshots -> playersList.addAll(queryDocumentSnapshots.toObjects(User.class)));

    }

    private void setOnClicks() {

        newChallengePlayerUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                newChallengePlayersRecyclerView.setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
                filter(s.toString().toLowerCase().trim());

            }
        });

        newChallengeAddChallengeButton.setOnClickListener(v -> {

            HelperMethods.closeKeyboard(NewChallengeActivity.this);

            String playerUsername = newChallengePlayerUsername.getText().toString().trim();
            String description = newChallengeDescription.getText().toString().trim();
            String reward = newChallengeReward.getText().toString().trim();

            if (TextUtils.isEmpty(playerUsername) || TextUtils.isEmpty(description) || TextUtils.isEmpty(reward)) {
                if (TextUtils.isEmpty(playerUsername)) {
                    HelperMethods.showError(newChallengeConstraintLayout, getString(R.string.player_s_username_cannot_be_empty));
                } else if (TextUtils.isEmpty(description)) {
                    HelperMethods.showError(newChallengeConstraintLayout, getString(R.string.challenge_description_cannot_be_empty));
                } else if (TextUtils.isEmpty(reward)) {
                    HelperMethods.showError(newChallengeConstraintLayout, getString(R.string.challenge_prize_cannot_be_empty));
                }
                return;
            }

            showLoading(true);

            Challenge challenge = new Challenge(FirebaseHelper.getCurrentUser().getUid(),
                    HelperMethods.getCurrentUser().getUsername(),
                    HelperMethods.getCurrentUser().getImage(),
                    selectedPlayer.getId(),
                    selectedPlayer.getUsername(),
                    selectedPlayer.getImage(),
                    HelperMethods.randomColor(getApplicationContext()),
                    description, reward, null,
                    false, false, new ArrayList<>(), 0);

            FirebaseHelper.collectionReference("Challenges")
                    .add(challenge)
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        HelperMethods.showError(newChallengeConstraintLayout, e.getMessage());
                    })
                    .addOnSuccessListener(documentReference -> {

                        showLoading(false);

                        Toast.makeText(NewChallengeActivity.this, "Challenge added successfully", Toast.LENGTH_SHORT).show();

                        Bundle bundle = new Bundle();
                        bundle.putString("challenge", new Gson().toJson(challenge));
                        bundle.putString("challenge_id", documentReference.getId());

                        Intent challengeIntent = new Intent(NewChallengeActivity.this, ChallengeActivity.class);
                        challengeIntent.putExtras(bundle);
                        startActivity(challengeIntent);
                        finish();

                    });

        });

    }

    private void showLoading(boolean loading) {

        if (loading) {
            newChallengeProgressBar.setVisibility(View.VISIBLE);
            newChallengeAddChallengeButton.setEnabled(false);
        } else {
            newChallengeProgressBar.setVisibility(View.GONE);
            newChallengeAddChallengeButton.setEnabled(true);
        }

    }

    private void filter(String username) {

        List<User> filteredList = new ArrayList<>();

        for (User player : playersList)
            if (player.getUsername().contains(username))
                filteredList.add(player);

        newChallengePlayersRecyclerAdapter.update(filteredList);

    }

    @Override
    public void onClick(User player) {

        selectedPlayer = player;
        newChallengePlayerUsername.setText(player.getUsername());
        newChallengePlayersRecyclerView.setVisibility(View.GONE);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;

    }

}
