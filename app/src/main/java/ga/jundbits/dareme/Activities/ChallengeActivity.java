package ga.jundbits.dareme.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Adapters.ChallengeCommentsBottomSheetRecyclerAdapter;
import ga.jundbits.dareme.Models.ChallengeCommentsBottomSheetModel;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.TimeAgo;

public class ChallengeActivity extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout challengeConstraintLayout;
    private Toolbar challengeToolbar;
    private SwipeRefreshLayout challengeSwipeRefreshLayout;
    private CircleImageView challengeUserImage;
    private TextView challengeUserUsername, challengeChallengesUsername, challengeChallengesTimeAgo;
    private ConstraintLayout challengeTextLayout;
    private TextView challengeText;
    private TextView challengePrizeList;
    private ConstraintLayout challengeStatusCompletedLayout, challengeStatusFailedLayout;
    private ImageButton challengeLikeButton, challengeCommentButton, challengeShareButton;
    private TextView challengeLikeCounter, challengeCommentCounter, challengeShareCounter;
    private ConstraintLayout challengeProofLayout;
    private VideoView challengeProofVideoView;

    private ConstraintLayout challengeCommentsBottomSheetConstraintLayout;
    private BottomSheetBehavior challengeCommentsBottomSheetBehavior;
    private ImageButton challengeCommentsBottomSheetCloseButton;
    private RecyclerView challengeCommentsRecyclerView;
    private EditText challengeCommentsBottomSheetCommentEditText;
    private ImageButton challengeCommentsBottomSheetCommentPostButton;

    private ChallengeCommentsBottomSheetRecyclerAdapter challengeCommentsBottomSheetRecyclerAdapter;

    private String challengeID;

    private TimeAgo timeAgo;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private FirebaseDynamicLinks firebaseDynamicLinks;

    private String currentUserID;

    private DocumentReference currentUserDocument;
    private DocumentReference challengeDocument;
    private StorageReference challengeStorageReference;

    private ProgressDialog progressDialog;

    private Intent returnIntent;

    public static final String APP_IMAGE_URL = "https://i.ibb.co/5Ttz167/Dare-Me.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        initVars();
        setupToolbar();
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
        challengeTextLayout = findViewById(R.id.challenge_text_layout);
        challengeText = findViewById(R.id.challenge_text);
        challengePrizeList = findViewById(R.id.challenge_prize_list);
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

        challengeCommentsBottomSheetConstraintLayout = findViewById(R.id.challenge_comments_bottom_sheet_constraint_layout);
        challengeCommentsBottomSheetBehavior = BottomSheetBehavior.from(challengeCommentsBottomSheetConstraintLayout);
        challengeCommentsBottomSheetCloseButton = findViewById(R.id.challenge_comments_bottom_sheet_close_button);
        challengeCommentsRecyclerView = findViewById(R.id.challenge_comments_bottom_sheet_recycler_view);
        challengeCommentsBottomSheetCommentEditText = findViewById(R.id.challenge_comments_bottom_sheet_comment_edit_text);
        challengeCommentsBottomSheetCommentPostButton = findViewById(R.id.challenge_comments_bottom_sheet_comment_post_button);

        challengeID = getIntent().getStringExtra("challenge_id");

        timeAgo = new TimeAgo();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDynamicLinks = FirebaseDynamicLinks.getInstance();

        currentUserID = firebaseUser.getUid();

        currentUserDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);
        challengeDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").document(challengeID);
        challengeStorageReference = firebaseStorage.getReference().child(getString(R.string.app_name)).child("Challenges").child(challengeID).child(challengeID);

        progressDialog = new ProgressDialog(ChallengeActivity.this);

        returnIntent = new Intent();

    }

    private void setupToolbar() {

        setSupportActionBar(challengeToolbar);
        getSupportActionBar().setTitle(getString(R.string.challenge_capital_c));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void closeKeyboard() {

        View closeKeyboardView = ChallengeActivity.this.getCurrentFocus();
        if (closeKeyboardView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(closeKeyboardView.getWindowToken(), 0);
            challengeCommentsBottomSheetCommentEditText.clearFocus();
        }

    }

    @Override
    public void onBackPressed() {

        if (challengeCommentsBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }

    }

    private void loadChallenge() {

        challengeDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (!documentSnapshot.exists())
                        return;

                    // Retrieving
                    final String watcherUserID = documentSnapshot.getString("user_id");
                    final String playerUserID = documentSnapshot.getString("player_user_id");
                    String image = documentSnapshot.getString("image");
                    final String username = documentSnapshot.getString("username");
                    final String challengesUsername = documentSnapshot.getString("challenges_username");
                    long dateTimeMillis = documentSnapshot.getLong("date_time_millis");
                    String color = documentSnapshot.getString("color");
                    String text = documentSnapshot.getString("text");
                    String prizeList = documentSnapshot.getString("prize");
                    final boolean completed = documentSnapshot.getBoolean("completed");
                    final boolean failed = documentSnapshot.getBoolean("failed");
                    String videoProof = documentSnapshot.getString("video_proof");

                    // Applying
                    if (image.equals("default")) {
                        challengeUserImage.setImageResource(R.mipmap.no_image);
                    } else {
                        Glide.with(ChallengeActivity.this).load(image).into(challengeUserImage);
                    }
                    challengeUserUsername.setText(username);

                    currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {

                            String currentUserUsername = snapshot.getString("username");

                            if (challengesUsername.equals(currentUserUsername)) {
                                challengeChallengesUsername.setText(R.string.challenges_you);
                            } else {
                                challengeChallengesUsername.setText(getString(R.string.challenges) + " " + challengesUsername);
                            }

                        }
                    });

                    challengeChallengesTimeAgo.setText(timeAgo.getTimeAgo(ChallengeActivity.this, dateTimeMillis));
                    challengeTextLayout.setBackgroundColor(Color.parseColor(color));
                    challengeText.setText(text);
                    challengePrizeList.setText(getString(R.string.prize) + "\n" + prizeList);

                    if (completed) {
                        challengeStatusCompletedLayout.setVisibility(View.VISIBLE);
                    } else {
                        challengeStatusCompletedLayout.setVisibility(View.GONE);
                    }

                    if (failed) {
                        challengeStatusFailedLayout.setVisibility(View.VISIBLE);
                    } else {
                        challengeStatusFailedLayout.setVisibility(View.GONE);

                    }

                    checkIfLiked();
                    retrieveCounters();

                    if (!TextUtils.isEmpty(videoProof)) {

                        challengeProofLayout.setVisibility(View.VISIBLE);

                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        final int screenWidth = displayMetrics.widthPixels;

                        challengeProofVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {

                                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

                                ViewGroup.LayoutParams params = challengeProofVideoView.getLayoutParams();
                                params.width = screenWidth;
                                params.height = screenWidth;
                                challengeProofVideoView.setLayoutParams(params);

                            }
                        });

                        final MediaController mediaController = new MediaController(ChallengeActivity.this);
                        mediaController.setAnchorView(challengeProofVideoView);

                        challengeProofVideoView.setMediaController(mediaController);
                        challengeProofVideoView.setVideoURI(Uri.parse(videoProof));
                        challengeProofVideoView.seekTo(100);

                        challengeProofVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mediaController.show();
                            }
                        });

                    }

                    // Set on-clicks
                    challengeUserImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openProfile(watcherUserID);
                        }
                    });

                    challengeUserUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openProfile(watcherUserID);
                        }
                    });

                    challengeChallengesUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("Users").whereEqualTo("username", challengesUsername)
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                            if (!queryDocumentSnapshots.isEmpty()) {

                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                                    firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                            .collection("Users").document(documentSnapshot.getId())
                                                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                                    String userID = documentSnapshot.getString("id");
                                                                    openProfile(userID);

                                                                }
                                                            });

                                                }

                                            }

                                        }
                                    });

                        }
                    });

                    challengeLikeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Add to challenge document
                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("Challenges").document(challengeID)
                                    .collection("Likes").document(currentUserID)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                            if (documentSnapshot.exists()) {

                                                challengeLikeButton.setImageResource(R.drawable.ic_like_grey_32dp);

                                                firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                        .collection("Challenges").document(challengeID)
                                                        .collection("Likes").document(currentUserID)
                                                        .delete();

                                            } else {

                                                challengeLikeButton.setImageResource(R.drawable.ic_like_red_32dp);

                                                Map<String, Object> likeMap = new HashMap<>();
                                                likeMap.put("user_id", currentUserID);
                                                likeMap.put("timestamp", FieldValue.serverTimestamp());

                                                firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                        .collection("Challenges").document(challengeID)
                                                        .collection("Likes").document(currentUserID)
                                                        .set(likeMap);

                                            }

                                        }
                                    });

                            // Add to watcher document
                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("Users").document(watcherUserID)
                                    .collection("Likes").document(currentUserID)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                            if (documentSnapshot.exists()) {

                                                firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                        .collection("Users").document(watcherUserID)
                                                        .collection("Likes").document(currentUserID)
                                                        .delete();

                                            } else {

                                                Map<String, Object> likeMap = new HashMap<>();
                                                likeMap.put("user_id", currentUserID);
                                                likeMap.put("timestamp", FieldValue.serverTimestamp());

                                                firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                        .collection("Users").document(watcherUserID)
                                                        .collection("Likes").document(currentUserID)
                                                        .set(likeMap);

                                            }

                                        }
                                    });

                            // Add to player document
                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("Users").document(playerUserID)
                                    .collection("Likes").document(currentUserID)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                            if (documentSnapshot.exists()) {

                                                firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                        .collection("Users").document(playerUserID)
                                                        .collection("Likes").document(currentUserID)
                                                        .delete();

                                            } else {

                                                Map<String, Object> likeMap = new HashMap<>();
                                                likeMap.put("user_id", currentUserID);
                                                likeMap.put("timestamp", FieldValue.serverTimestamp());

                                                firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                        .collection("Users").document(playerUserID)
                                                        .collection("Likes").document(currentUserID)
                                                        .set(likeMap);

                                            }

                                        }
                                    });

                        }
                    });

                } else {
                    Snackbar.make(challengeConstraintLayout, getString(R.string.couldnt_load_challenge), Snackbar.LENGTH_SHORT).show();
                }

                if (challengeSwipeRefreshLayout.isRefreshing()) {
                    challengeSwipeRefreshLayout.setRefreshing(false);
                }

            }
        });

    }

    private void setOnClicks() {

        challengeSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadChallenge();
            }
        });

        challengeCommentButton.setOnClickListener(this);
        challengeShareButton.setOnClickListener(this);

    }

    private void checkIfLiked() {

        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                .collection("Challenges").document(challengeID)
                .collection("Likes").document(currentUserID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            challengeLikeButton.setImageResource(R.drawable.ic_like_red_32dp);
                        } else {
                            challengeLikeButton.setImageResource(R.drawable.ic_like_grey_32dp);
                        }

                    }
                });

    }

    private void retrieveCounters() {

        challengeDocument.collection("Likes")
                .addSnapshotListener(ChallengeActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        challengeLikeCounter.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

        challengeDocument.collection("Comments")
                .addSnapshotListener(ChallengeActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        challengeCommentCounter.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                .collection("ShareableLinks").whereEqualTo("challenge_id", challengeID)
                .addSnapshotListener(ChallengeActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        challengeShareCounter.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

    }

    private void openProfile(String userID) {

        Intent profileIntent = new Intent(ChallengeActivity.this, ProfileActivity.class);
        profileIntent.putExtra("user_id", userID);
        startActivity(profileIntent);

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
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.challenge_comment_button:
                onButtonClick("comment");
                break;

            case R.id.challenge_share_button:
                onButtonClick("share");
                break;

        }

    }

    private void onButtonClick(String buttonName) {

        switch (buttonName) {

            case "comment":
                commentChallenge();
                break;

            case "share":
                shareChallenge();
                break;

        }

    }

    private void commentChallenge() {

        Query query = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").document(challengeID).collection("Comments").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChallengeCommentsBottomSheetModel> options = new FirestoreRecyclerOptions.Builder<ChallengeCommentsBottomSheetModel>()
                .setLifecycleOwner(this)
                .setQuery(query, ChallengeCommentsBottomSheetModel.class)
                .build();

        challengeCommentsBottomSheetRecyclerAdapter = new ChallengeCommentsBottomSheetRecyclerAdapter(options, this);

        challengeCommentsRecyclerView.setHasFixedSize(true);
        challengeCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        challengeCommentsRecyclerView.setAdapter(challengeCommentsBottomSheetRecyclerAdapter);

        if (challengeProofVideoView.isPlaying()) {
            challengeProofVideoView.pause();
        }

        challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        challengeCommentsBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED) {

                    closeKeyboard();

                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {

                    if (!challengeCommentsBottomSheetCommentEditText.getText().toString().isEmpty()) {
                        challengeCommentsBottomSheetCommentPostButton.setEnabled(true);
                    }

                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        challengeCommentsBottomSheetCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        challengeCommentsBottomSheetCommentPostButton.setEnabled(false);

        challengeCommentsBottomSheetCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {
                    challengeCommentsBottomSheetCommentPostButton.setEnabled(false);
                } else {
                    challengeCommentsBottomSheetCommentPostButton.setEnabled(true);
                }

            }
        });

        challengeCommentsBottomSheetCommentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String comment = challengeCommentsBottomSheetCommentEditText.getText().toString().trim();

                challengeCommentsBottomSheetCommentEditText.getText().clear();

                currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        String currentUserUsername = documentSnapshot.getString("username");
                        String currentUserImage = documentSnapshot.getString("image");

                        Timestamp timestamp = Timestamp.now();

                        long timestampSeconds = timestamp.getSeconds();
                        long timestampNanoSeconds = timestamp.getNanoseconds();
                        long timestampSecondsToMillis = TimeUnit.SECONDS.toMillis(timestampSeconds);
                        long timestampNanoSecondsToMillis = TimeUnit.NANOSECONDS.toMillis(timestampNanoSeconds);
                        long timestampTotalMillis = timestampSecondsToMillis + timestampNanoSecondsToMillis;

                        Map<String, Object> commentMap = new HashMap<>();
                        commentMap.put("comment", comment);
                        commentMap.put("user_id", currentUserID);
                        commentMap.put("username", currentUserUsername);
                        commentMap.put("image", currentUserImage);
                        commentMap.put("timestamp", FieldValue.serverTimestamp());
                        commentMap.put("date_time_millis", timestampTotalMillis);

                        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                .collection("Challenges").document(challengeID)
                                .collection("Comments")
                                .add(commentMap)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        challengeCommentsRecyclerView.smoothScrollToPosition(0);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ChallengeActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });

            }
        });

    }

    private void shareChallenge() {

        challengeDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").document(challengeID);
        challengeStorageReference = firebaseStorage.getReference().child(getString(R.string.app_name)).child("Challenges").child(challengeID).child(challengeID);

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                .collection("ShareableLinks").whereEqualTo("challenge_id", challengeID)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (queryDocumentSnapshots.isEmpty()) {

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] bytes = baos.toByteArray();

                            // Upload To Storage
                            UploadTask uploadTask = challengeStorageReference.putBytes(bytes);

                            // Get Download Url
                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                            if (!task.isSuccessful()) {
                                                throw task.getException();
                                            }

                                            // Continue with the task to get the download URL
                                            return challengeStorageReference.getDownloadUrl();

                                        }
                                    })
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {

                                            if (task.isSuccessful()) {

                                                Uri downloadUri = task.getResult();
                                                createLink(downloadUri, challengeID, challengeUserUsername.getText().toString(), challengeChallengesUsername.getText().toString());

                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(ChallengeActivity.this, getString(R.string.error_sharing_the_challenge), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });

                        } else {

                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                        .collection("ShareableLinks").document(documentSnapshot.getId())
                                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                String storageLink = documentSnapshot.getString("storage_link");
                                                createLink(Uri.parse(storageLink), challengeID, challengeUserUsername.getText().toString(), challengeChallengesUsername.getText().toString());

                                            }
                                        });

                            }

                        }

                    }
                });

    }

    private void createLink(final Uri downloadUri, final String challengeID, String username, String challengesUsername) {

        DynamicLink dynamicLink = firebaseDynamicLinks.createDynamicLink()
                .setLink(downloadUri)
                .setDomainUriPrefix("https://dareme.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .setFallbackUrl(Uri.parse("https://bit.ly/3cu9i1t"))
                                .build()
                )
                .setIosParameters(
                        new DynamicLink.IosParameters.Builder(getPackageName())
                                .setFallbackUrl(Uri.parse("https://bit.ly/3cu9i1t"))
                                .build()
                )
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(username + " has challenged " + challengesUsername)
                                .setDescription("Check it out!")
                                .setImageUrl(Uri.parse(APP_IMAGE_URL))
                                .build()
                )
                .buildDynamicLink();

        final Uri longDynamicLinkUri = dynamicLink.getUri();

        Task<ShortDynamicLink> shortDynamicLinkTask = firebaseDynamicLinks.createDynamicLink()
                .setLongLink(longDynamicLinkUri)
                .buildShortDynamicLink()
                .addOnCompleteListener(ChallengeActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {

                        if (task.isSuccessful()) {

                            final Uri shortLink = task.getResult().getShortLink();

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm:ss a, zz");
                            final String dateTime = dateFormat.format(calendar.getTime());

                            Map<String, Object> shareMap = new HashMap<>();
                            shareMap.put("storage_link", downloadUri.toString());
                            shareMap.put("long_link", longDynamicLinkUri.toString());
                            shareMap.put("short_link", shortLink.toString());
                            shareMap.put("generated_by", currentUserID);
                            shareMap.put("challenge_id", challengeID);
                            shareMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("ShareableLinks").document(dateTime)
                                    .set(shareMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Intent sendIntent = new Intent();
                                            sendIntent.setAction(Intent.ACTION_SEND);
                                            sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                            sendIntent.setType("text/plain");

                                            progressDialog.dismiss();

                                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                                            startActivity(shareIntent);

                                        }
                                    });

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ChallengeActivity.this, getString(R.string.error_sharing_the_challenge), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

}
