package ga.jundbits.dareme.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Adapters.ChallengeCommentsBottomSheetRecyclerAdapter;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.Models.Comment;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class ChallengeActivity extends AppCompatActivity {

    private ConstraintLayout challengeConstraintLayout;
    private Toolbar challengeToolbar;
    private SwipeRefreshLayout challengeSwipeRefreshLayout;
    private CircleImageView challengeUserImage;
    private TextView challengeUserUsername, challengeChallengesUsername, challengeChallengesTimeAgo;
    private TextView challengeText;
    private TextView challengeRewards;
    private ConstraintLayout challengeStatusCompletedLayout, challengeStatusFailedLayout;
    private ImageButton challengeLikeButton, challengeCommentButton, challengeShareButton;
    private TextView challengeLikeCounter, challengeCommentCounter, challengeShareCounter;
    private ConstraintLayout challengeProofLayout;
    private VideoView challengeProofVideoView;

    private BottomSheetBehavior<View> challengeCommentsBottomSheetBehavior;
    private ImageButton challengeCommentsBottomSheetCloseButton;
    private RecyclerView challengeCommentsRecyclerView;
    private EditText challengeCommentsBottomSheetCommentEditText;
    private ImageButton challengeCommentsBottomSheetCommentPostButton;

    private final List<Comment> commentsList = new ArrayList<>();
    private ChallengeCommentsBottomSheetRecyclerAdapter challengeCommentsBottomSheetRecyclerAdapter;
    private Challenge challenge;

    private DocumentReference challengeDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        initVars();
        setupToolbarAndBackButton();
        loadChallenge();
        setOnClicks();

    }

    private void initVars() {

        challengeConstraintLayout = findViewById(R.id.challenge_constraint_layout);
        challengeToolbar = findViewById(R.id.challenge_toolbar);
        challengeSwipeRefreshLayout = findViewById(R.id.challenge_swipe_refresh_layout);
        challengeUserImage = findViewById(R.id.challenge_user_image);
        challengeUserUsername = findViewById(R.id.challenge_user_username);
        challengeChallengesUsername = findViewById(R.id.challenge_challenges_username);
        challengeChallengesTimeAgo = findViewById(R.id.challenge_challenges_time_ago);
        challengeText = findViewById(R.id.challenge_text);
        challengeRewards = findViewById(R.id.challenge_rewards);
        challengeStatusCompletedLayout = findViewById(R.id.challenge_status_completed_layout);
        challengeStatusFailedLayout = findViewById(R.id.challenge_status_failed_layout);
        challengeLikeButton = findViewById(R.id.challenge_like_button);
        challengeCommentButton = findViewById(R.id.challenge_comment_button);
        challengeShareButton = findViewById(R.id.challenge_share_button);
        challengeLikeCounter = findViewById(R.id.challenge_like_counter);
        challengeCommentCounter = findViewById(R.id.challenge_comment_counter);
        challengeShareCounter = findViewById(R.id.challenge_share_counter);
        challengeProofLayout = findViewById(R.id.challenge_proof_layout);
        challengeProofVideoView = findViewById(R.id.challenge_proof_video_view);

        ConstraintLayout challengeCommentsBottomSheetConstraintLayout = findViewById(R.id.challenge_comments_bottom_sheet_constraint_layout);
        challengeCommentsBottomSheetBehavior = BottomSheetBehavior.from(challengeCommentsBottomSheetConstraintLayout);
        challengeCommentsBottomSheetCloseButton = findViewById(R.id.challenge_comments_bottom_sheet_close_button);
        challengeCommentsRecyclerView = findViewById(R.id.challenge_comments_bottom_sheet_recycler_view);
        challengeCommentsBottomSheetCommentEditText = findViewById(R.id.challenge_comments_bottom_sheet_comment_edit_text);
        challengeCommentsBottomSheetCommentPostButton = findViewById(R.id.challenge_comments_bottom_sheet_comment_post_button);

        challengeCommentsBottomSheetCommentPostButton.setEnabled(false);

        String challengeID = getIntent().getExtras().getString("challenge_id");
        challenge = new Gson().fromJson(getIntent().getExtras().getString("challenge"), Challenge.class);
        challengeDocument = FirebaseHelper.documentReference("Challenges/" + challengeID);

    }

    private void setupToolbarAndBackButton() {

        setSupportActionBar(challengeToolbar);
        getSupportActionBar().setTitle(getString(R.string.challenge_capital_c));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (challengeCommentsBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    finish();
                }

            }
        });

    }

    private void loadChallenge() {

        if (challenge.getWatcher_image() == null) {
            challengeUserImage.setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(ChallengeActivity.this).load(challenge.getWatcher_image()).into(challengeUserImage);
        }

        challengeUserUsername.setText(challenge.getWatcher_username());

        if (challenge.getPlayer_username().equals(HelperMethods.getCurrentUser().getUsername())) {
            challengeChallengesUsername.setText(R.string.challenges_you);
        } else {
            challengeChallengesUsername.setText(getString(R.string.challenges) + " " + challenge.getPlayer_username());
        }

        if (challenge.getTimestamp() != null)
            challengeChallengesTimeAgo.setText(TimeAgo.using(challenge.getTimestamp().getTime()));
        challengeText.setBackgroundColor(Color.parseColor(challenge.getColor()));
        challengeText.setText(challenge.getDescription());
        challengeRewards.setText(getString(R.string.rewards) + "\n" + challenge.getRewards());

        challengeStatusCompletedLayout.setVisibility(challenge.isCompleted() ? View.VISIBLE : View.GONE);
        challengeStatusFailedLayout.setVisibility(challenge.isFailed() ? View.VISIBLE : View.GONE);

        if (challenge.getLikes().contains(FirebaseHelper.getCurrentUser().getUid())) {
            challengeLikeButton.setImageResource(R.drawable.ic_like_red_32dp);
        } else {
            challengeLikeButton.setImageResource(R.drawable.ic_like_grey_32dp);
        }

        challengeLikeCounter.setText(String.valueOf(challenge.getLikes().size()));
        challengeShareCounter.setText(String.valueOf(challenge.getShare_count()));

        challengeDocument.collection("Comments").get()
                .addOnSuccessListener(this, queryDocumentSnapshots -> {
                    commentsList.clear();
                    commentsList.addAll(queryDocumentSnapshots.toObjects(Comment.class));
                    if (challengeCommentsBottomSheetRecyclerAdapter != null)
                        challengeCommentsBottomSheetRecyclerAdapter.notifyItemRangeChanged(0, commentsList.size());
                    challengeCommentCounter.setText(String.valueOf(queryDocumentSnapshots.size()));
                });

        if (!TextUtils.isEmpty(challenge.getVideo_url())) {

            challengeProofLayout.setVisibility(View.VISIBLE);

            challengeProofVideoView.setOnPreparedListener(mp -> {

                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

                ViewGroup.LayoutParams params = challengeProofVideoView.getLayoutParams();
                params.width = HelperMethods.getScreenWidth(this);
                params.height = HelperMethods.getScreenWidth(this);
                challengeProofVideoView.setLayoutParams(params);

            });

            MediaController mediaController = new MediaController(ChallengeActivity.this);
            mediaController.setAnchorView(challengeProofVideoView);

            challengeProofVideoView.setMediaController(mediaController);
            challengeProofVideoView.setVideoURI(Uri.parse(challenge.getVideo_url()));
            challengeProofVideoView.seekTo(100);

            challengeProofVideoView.setOnCompletionListener(mp -> mediaController.show());

        }

        if (challengeSwipeRefreshLayout.isRefreshing()) {
            challengeSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void refreshChallenge() {

        challengeDocument.get()
                .addOnSuccessListener(this, documentSnapshot -> {

                    challenge = documentSnapshot.toObject(Challenge.class);
                    loadChallenge();

                })
                .addOnFailureListener(this, e -> {

                    Snackbar.make(challengeConstraintLayout, getString(R.string.couldnt_load_challenge), Snackbar.LENGTH_SHORT).show();

                    if (challengeSwipeRefreshLayout.isRefreshing()) {
                        challengeSwipeRefreshLayout.setRefreshing(false);
                    }

                });

    }

    private void setOnClicks() {

        challengeSwipeRefreshLayout.setOnRefreshListener(this::refreshChallenge);

        challengeUserImage.setOnClickListener(v -> openProfile(challenge.getWatcher_id()));
        challengeUserUsername.setOnClickListener(v -> openProfile(challenge.getWatcher_id()));
        challengeChallengesUsername.setOnClickListener(v -> openProfile(challenge.getPlayer_id()));

        challengeLikeButton.setOnClickListener(v -> {

            if (challenge.getLikes().contains(FirebaseHelper.getCurrentUser().getUid())) {
                challengeLikeButton.setImageResource(R.drawable.ic_like_grey_32dp);
                challenge.getLikes().remove(FirebaseHelper.getCurrentUser().getUid());
                challengeDocument.update("likes", FieldValue.arrayRemove(FirebaseHelper.getCurrentUser().getUid()));
                FirebaseHelper.documentReference("Users/" + challenge.getPlayer_id()).update("likes", FieldValue.arrayRemove(FirebaseHelper.getCurrentUser().getUid()));
            } else {
                challengeLikeButton.setImageResource(R.drawable.ic_like_red_32dp);
                challenge.getLikes().add(FirebaseHelper.getCurrentUser().getUid());
                challengeDocument.update("likes", FieldValue.arrayUnion(FirebaseHelper.getCurrentUser().getUid()));
                FirebaseHelper.documentReference("Users/" + challenge.getPlayer_id()).update("likes", FieldValue.arrayUnion(FirebaseHelper.getCurrentUser().getUid()));
            }

            challengeLikeCounter.setText(String.valueOf(challenge.getLikes().size()));

        });

        challengeCommentButton.setOnClickListener(view -> openComments());
        challengeShareButton.setOnClickListener(view -> {

            FirebaseHelper.createShareUrl(challengeDocument.getId(), challenge)
                    .addOnFailureListener((Activity) getApplicationContext(), e -> Toast.makeText(getApplicationContext(), getString(R.string.error_sharing_the_challenge), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener((Activity) getApplicationContext(), shortDynamicLink -> {

                        challengeDocument.update("share_count", FieldValue.increment(1));

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, shortDynamicLink.getShortLink().toString());
                        sendIntent.setType("text/plain");

                        Intent shareIntent = Intent.createChooser(sendIntent, "Share challenge");
                        startActivity(shareIntent);

                    });

        });

        challengeCommentsBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    HelperMethods.closeKeyboard(ChallengeActivity.this);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        challengeCommentsBottomSheetCloseButton.setOnClickListener(v -> challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        challengeCommentsBottomSheetCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                challengeCommentsBottomSheetCommentPostButton.setEnabled(!s.toString().isEmpty());
            }
        });

        challengeCommentsBottomSheetCommentPostButton.setOnClickListener(v -> {

            String comment = challengeCommentsBottomSheetCommentEditText.getText().toString().trim();
            challengeCommentsBottomSheetCommentEditText.getText().clear();

            Comment commentModel = new Comment(comment,
                    FirebaseHelper.getCurrentUser().getUid(),
                    HelperMethods.getCurrentUser().getUsername(),
                    HelperMethods.getCurrentUser().getImage());

            commentsList.add(0, commentModel);
            challengeCommentCounter.setText(String.valueOf(commentsList.size()));
            challengeCommentsBottomSheetRecyclerAdapter.update(commentsList);
            challengeCommentsBottomSheetRecyclerAdapter.notifyItemInserted(0);

            challengeDocument
                    .collection("Comments")
                    .add(commentModel)
                    .addOnSuccessListener(documentReference -> challengeCommentsRecyclerView.smoothScrollToPosition(0))
                    .addOnFailureListener(e -> Toast.makeText(ChallengeActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show());

        });

    }

    private void openProfile(String userID) {

        Intent profileIntent = new Intent(ChallengeActivity.this, ProfileActivity.class);
        profileIntent.putExtra("user_id", userID);
        startActivity(profileIntent);

    }

    private void openComments() {

        if (challengeCommentsBottomSheetRecyclerAdapter != null) {
            challengeCommentsBottomSheetRecyclerAdapter.update(commentsList);
        } else {
            challengeCommentsBottomSheetRecyclerAdapter = new ChallengeCommentsBottomSheetRecyclerAdapter(this, commentsList);

            challengeCommentsRecyclerView.setHasFixedSize(true);
            challengeCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            challengeCommentsRecyclerView.setAdapter(challengeCommentsBottomSheetRecyclerAdapter);
        }

        if (challengeProofVideoView.isPlaying()) challengeProofVideoView.pause();

        challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

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
