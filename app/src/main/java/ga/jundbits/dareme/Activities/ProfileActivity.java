package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Adapters.AccountProfileChallengesRecyclerAdapter;
import ga.jundbits.dareme.Callbacks.OnChallengeClick;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.Models.User;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;

public class ProfileActivity extends AppCompatActivity implements OnChallengeClick {

    private Toolbar profileToolbar;
    private SwipeRefreshLayout profileSwipeRefreshLayout;
    private CircleImageView profileUserImage;
    private TextView profileChallengesCounter, profileChallengesText, profileFollowersCounter, profileFollowersText, profileLikesCounter, profileLikesText;
    private TextView profileUserName, profileDescription;
    private RecyclerView profileChallengesRecyclerView;
    private TextView profileNoCompletedChallengesText;

    private AccountProfileChallengesRecyclerAdapter profileChallengesRecyclerAdapter;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initVars();
        setupToolbar();
        loadProfile();
        setOnClicks();

    }

    private void initVars() {

        profileToolbar = findViewById(R.id.profile_toolbar);
        profileSwipeRefreshLayout = findViewById(R.id.profile_swipe_refresh_layout);
        profileUserImage = findViewById(R.id.profile_user_image);
        profileChallengesCounter = findViewById(R.id.profile_challenges_counter);
        profileChallengesText = findViewById(R.id.profile_challenges_text);
        profileFollowersCounter = findViewById(R.id.profile_followers_counter);
        profileFollowersText = findViewById(R.id.profile_followers_text);
        profileLikesCounter = findViewById(R.id.profile_likes_counter);
        profileLikesText = findViewById(R.id.profile_likes_text);
        profileUserName = findViewById(R.id.profile_user_name);
        profileDescription = findViewById(R.id.profile_description);
        profileChallengesRecyclerView = findViewById(R.id.profile_challenges_recycler_view);
        profileNoCompletedChallengesText = findViewById(R.id.profile_no_completed_challenges_text);

        userID = getIntent().getExtras().getString("user_id");

    }

    private void setupToolbar() {
        setSupportActionBar(profileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadProfile() {

        // User info
        FirebaseHelper.documentReference("Users/" + userID).get().addOnSuccessListener(documentSnapshot -> {

            User user = documentSnapshot.toObject(User.class);

            getSupportActionBar().setTitle(user.getUsername());
            profileUserName.setText(user.getName());

            if (user.getImage() == null) {
                profileUserImage.setImageResource(R.mipmap.no_image);
            } else {
                Glide.with(ProfileActivity.this).load(user.getImage()).into(profileUserImage);
            }

            if (TextUtils.isEmpty(user.getDescription())) {
                profileDescription.setVisibility(View.GONE);
            } else {
                profileDescription.setVisibility(View.VISIBLE);
                profileDescription.setText(user.getDescription());
            }

        });

        // Followers count and text
        FirebaseHelper.collectionReference("Followers").count().get(AggregateSource.SERVER).addOnSuccessListener(this, aggregateQuerySnapshot -> {
            profileFollowersCounter.setText(String.valueOf(aggregateQuerySnapshot.getCount()));
            profileFollowersText.setText(aggregateQuerySnapshot.getCount() == 1 ? getString(R.string.follower) : getString(R.string.followers));
        });

        // Likes count and text
        FirebaseHelper.collectionReference("Likes").count().get(AggregateSource.SERVER).addOnSuccessListener(this, aggregateQuerySnapshot -> {
            profileLikesCounter.setText(String.valueOf(aggregateQuerySnapshot.getCount()));
            profileLikesText.setText(aggregateQuerySnapshot.getCount() == 1 ? getString(R.string.like) : getString(R.string.likes));
        });

        // Challenges with count and text
        Query query = FirebaseHelper.collectionReference("Challenges")
                .whereEqualTo("player_id", userID)
                .whereEqualTo("completed", true)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        PagingConfig config = new PagingConfig(3, 5, false, 15);

        FirestorePagingOptions<Challenge> options = new FirestorePagingOptions.Builder<Challenge>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Challenge.class)
                .build();

        if (profileChallengesRecyclerAdapter == null) {
            profileChallengesRecyclerAdapter = new AccountProfileChallengesRecyclerAdapter(options, this, this);
            profileChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            profileChallengesRecyclerView.setHasFixedSize(true);
            profileChallengesRecyclerView.setAdapter(profileChallengesRecyclerAdapter);
        }

        profileChallengesCounter.setText(String.valueOf(profileChallengesRecyclerAdapter.getItemCount()));

        if (profileChallengesRecyclerAdapter.getItemCount() == 0) {
            profileNoCompletedChallengesText.setVisibility(View.VISIBLE);
            profileChallengesText.setText(getString(R.string.challenges));
        } else if (profileChallengesRecyclerAdapter.getItemCount() == 1) {
            profileNoCompletedChallengesText.setVisibility(View.GONE);
            profileChallengesText.setText(getString(R.string.challenge));
        } else {
            profileNoCompletedChallengesText.setVisibility(View.GONE);
            profileChallengesText.setText(getString(R.string.challenges));
        }

        if (profileSwipeRefreshLayout.isRefreshing()) {
            profileSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void setOnClicks() {
        profileSwipeRefreshLayout.setOnRefreshListener(this::loadProfile);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;

    }

    @Override
    public void onClick(String challengeID, Challenge challenge) {

        Bundle bundle = new Bundle();
        bundle.putString("challenge", new Gson().toJson(challenge));
        bundle.putString("challenge_id", challengeID);

        Intent challengeIntent = new Intent(ProfileActivity.this, ChallengeActivity.class);
        challengeIntent.putExtras(bundle);
        startActivity(challengeIntent);

    }

}
