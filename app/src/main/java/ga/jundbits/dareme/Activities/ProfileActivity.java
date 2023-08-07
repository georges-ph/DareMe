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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Adapters.AccountProfileChallengesRecyclerAdapter;
import ga.jundbits.dareme.Models.AccountProfileChallengesModel;
import ga.jundbits.dareme.R;

public class ProfileActivity extends AppCompatActivity implements AccountProfileChallengesRecyclerAdapter.OnListItemClick {

    private Toolbar profileToolbar;
    private SwipeRefreshLayout profileSwipeRefreshLayout;

    private CircleImageView profileUserImage;

    private TextView profileChallengesCounter, profileChallengesText, profileFollowersCounter, profileFollowersText, profileLikesCounter, profileLikesText;

    private TextView profileUserName, profileDescription;

    private RecyclerView profileChallengesRecyclerView;
    private TextView profileNoCompletedChallengesText;

    private AccountProfileChallengesRecyclerAdapter profileChallengesRecyclerAdapter;

    private String userID;

    private FirebaseFirestore firebaseFirestore;
    private DocumentReference userDocument;

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

        firebaseFirestore = FirebaseFirestore.getInstance();
        userDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(userID);

    }

    private void setupToolbar() {

        setSupportActionBar(profileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadProfile() {

        userDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String username = documentSnapshot.getString("username");
                String image = documentSnapshot.getString("image");
                String name = documentSnapshot.getString("name");
                String description = documentSnapshot.getString("description");

                // Username
                getSupportActionBar().setTitle(username);

                // Image
                if (image == null) {
                    profileUserImage.setImageResource(R.mipmap.no_image);
                } else {
                    Glide.with(ProfileActivity.this).load(image).into(profileUserImage);
                }

                // Name
                profileUserName.setText(name);

                // Description
                if (TextUtils.isEmpty(description)) {
                    profileDescription.setVisibility(View.GONE);
                } else {
                    profileDescription.setVisibility(View.VISIBLE);
                    profileDescription.setText(description);
                }

            }
        });

        // Challenges count and text
        userDocument.collection("CompletedChallenges")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        profileChallengesCounter.setText(String.valueOf(queryDocumentSnapshots.size()));

                        if (queryDocumentSnapshots.size() == 1) {
                            profileChallengesText.setText(getString(R.string.challenge));
                        } else {

                            profileChallengesText.setText(getString(R.string.challenges));

                            if (queryDocumentSnapshots.isEmpty()) {
                                profileNoCompletedChallengesText.setVisibility(View.VISIBLE);
                            }

                        }

                    }
                });

        // Followers count and text
        userDocument.collection("Followers")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        profileFollowersCounter.setText(String.valueOf(queryDocumentSnapshots.size()));

                        if (queryDocumentSnapshots.size() == 1) {
                            profileFollowersText.setText(getString(R.string.follower));
                        } else {
                            profileFollowersText.setText(getString(R.string.followers));
                        }

                    }
                });

        // Likes count and text
        userDocument.collection("Likes")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        profileLikesCounter.setText(String.valueOf(queryDocumentSnapshots.size()));

                        if (queryDocumentSnapshots.size() == 1) {
                            profileLikesText.setText(getString(R.string.like));
                        } else {
                            profileLikesText.setText(getString(R.string.likes));
                        }

                    }
                });

        // Challenges
        Query query = userDocument.collection("CompletedChallenges").orderBy("timestamp", Query.Direction.DESCENDING);

        PagingConfig config = new PagingConfig(3, 5, false, 15);

        FirestorePagingOptions<AccountProfileChallengesModel> options = new FirestorePagingOptions.Builder<AccountProfileChallengesModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, AccountProfileChallengesModel.class)
                .build();

        profileChallengesRecyclerAdapter = new AccountProfileChallengesRecyclerAdapter(options, this, this);

        profileChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        profileChallengesRecyclerView.setHasFixedSize(true);
        profileChallengesRecyclerView.setAdapter(profileChallengesRecyclerAdapter);

        if (profileSwipeRefreshLayout.isRefreshing()) {
            profileSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void setOnClicks() {

        profileSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadProfile();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            default:
                return false;

            case android.R.id.home:
                finish();
                return true;

        }

    }

    @Override
    public void onItemClick(String challengeID) {

        Intent challengeIntent = new Intent(ProfileActivity.this, ChallengeActivity.class);
        challengeIntent.putExtra("challenge_id", challengeID);
        startActivity(challengeIntent);

    }

}
