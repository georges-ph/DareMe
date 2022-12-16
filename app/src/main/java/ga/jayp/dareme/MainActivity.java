package ga.jayp.dareme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;

public class MainActivity extends AppCompatActivity implements HomeFragment.OpenCommentsBox, HomeFragment.NoConnection, MyChallengesFragment.NoConnection, AccountFragment.NoConnection {

    String userType;
    String challengeID;

    ConstraintLayout noConnectionLayout;

    Toolbar mainToolbar;
    BottomNavigationView mainBottomNavigationView;

    ConstraintLayout mainBannerLayout;
    TextView mainBannerMessage;
    ImageButton mainBannerCloseButton;

    ConstraintLayout commentsBottomSheetConstraintLayout;
    BottomSheetBehavior commentsBottomSheetBehavior;
    ImageButton commentsBottomSheetCloseButton;
    RecyclerView commentsRecyclerView;
    EditText commentsBottomSheetCommentEditText;
    ImageButton commentsBottomSheetCommentPostButton;

    ChallengeCommentsBottomSheetRecyclerAdapter challengeCommentsBottomSheetRecyclerAdapter;

    boolean scrollToBottom = false;

    HomeFragment homeFragment;
    MyChallengesFragment myChallengesFragment;
    AccountFragment accountFragment;
    FragmentManager fragmentManager;
    Fragment activeFragment;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDynamicLinks firebaseDynamicLinks;

    String currentUserID;
    String currentUserUsername;

    DocumentReference currentUserDocument;

    EasyNetworkMod easyNetworkMod;

    boolean doubleBackToExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userType = getIntent().getExtras().getString("user_type");
        challengeID = getIntent().getExtras().getString("challenge_id");

        noConnectionLayout = findViewById(R.id.no_connection_layout);

        mainToolbar = findViewById(R.id.main_toolbar);
        mainBottomNavigationView = findViewById(R.id.main_bottom_navigation_view);

        mainBannerLayout = findViewById(R.id.main_banner_layout);
        mainBannerMessage = findViewById(R.id.main_banner_message);
        mainBannerCloseButton = findViewById(R.id.main_banner_close_button);

        commentsBottomSheetConstraintLayout = findViewById(R.id.challenge_comments_bottom_sheet_constraint_layout);
        commentsBottomSheetBehavior = BottomSheetBehavior.from(commentsBottomSheetConstraintLayout);
        commentsBottomSheetCloseButton = findViewById(R.id.challenge_comments_bottom_sheet_close_button);
        commentsRecyclerView = findViewById(R.id.challenge_comments_bottom_sheet_recycler_view);
        commentsBottomSheetCommentEditText = findViewById(R.id.challenge_comments_bottom_sheet_comment_edit_text);
        commentsBottomSheetCommentPostButton = findViewById(R.id.challenge_comments_bottom_sheet_comment_post_button);

        homeFragment = new HomeFragment();
        myChallengesFragment = new MyChallengesFragment();
        accountFragment = new AccountFragment();
        fragmentManager = getSupportFragmentManager();
        activeFragment = homeFragment;

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDynamicLinks = FirebaseDynamicLinks.getInstance();

        currentUserID = firebaseUser.getUid();
        currentUserDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        easyNetworkMod = new EasyNetworkMod(this);

        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        getUserUsername(currentUserDocument);

        fragmentManager.beginTransaction().add(R.id.main_fragment_container, accountFragment, "account").hide(accountFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_fragment_container, myChallengesFragment, "my challenges").hide(myChallengesFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_fragment_container, homeFragment, "home").commit();

        currentUserDocument.addSnapshotListener(MainActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                String message = documentSnapshot.getString("message");
                boolean messageClosed = documentSnapshot.getBoolean("message_closed");

                Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in);
                Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out);

                if (messageClosed) {
                    mainBannerLayout.startAnimation(fadeOut);
                    mainBannerLayout.setVisibility(View.GONE);
                } else {
                    mainBannerMessage.setText(message);
                    mainBannerLayout.setVisibility(View.VISIBLE);
                    mainBannerLayout.startAnimation(fadeIn);
                }

            }
        });

        mainBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    default:
                        return false;

                    case R.id.main_bottom_menu_home_fragment:
                        fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit();
                        activeFragment = homeFragment;
                        getSupportActionBar().setTitle(getString(R.string.app_name));
                        return true;

                    case R.id.main_bottom_menu_my_challenges_fragment:
                        fragmentManager.beginTransaction().hide(activeFragment).show(myChallengesFragment).commit();
                        activeFragment = myChallengesFragment;
                        getSupportActionBar().setTitle(getString(R.string.app_name));
                        return true;

                    case R.id.main_bottom_menu_account_fragment:
                        fragmentManager.beginTransaction().hide(activeFragment).show(accountFragment).commit();
                        activeFragment = accountFragment;
                        getSupportActionBar().setTitle(currentUserUsername);
                        return true;

                }

            }
        });

        if (!challengeID.equals("null")) {

            Intent challengeIntent = new Intent(MainActivity.this, ChallengeActivity.class);
            challengeIntent.putExtra("challenge_id", challengeID);
            startActivity(challengeIntent);

        }

    }

    @Override
    public void onCommentClicked(final String challengeID) {

        Query query = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").document(challengeID).collection("Comments").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChallengeCommentsBottomSheetModel> options = new FirestoreRecyclerOptions.Builder<ChallengeCommentsBottomSheetModel>()
                .setLifecycleOwner(this)
                .setQuery(query, ChallengeCommentsBottomSheetModel.class)
                .build();

        challengeCommentsBottomSheetRecyclerAdapter = new ChallengeCommentsBottomSheetRecyclerAdapter(options, this);
// TODO: erja3 red l comments mtl abel bas aaml shaghle eno badel ma a3ml convert lal timestamp
//  when retrieving a3mela when posting aw abel b shwe w hek
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(challengeCommentsBottomSheetRecyclerAdapter);

        commentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        commentsBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED) {

                    closeKeyboard();

                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {

                    if (!commentsBottomSheetCommentEditText.getText().toString().isEmpty()) {
                        commentsBottomSheetCommentPostButton.setEnabled(true);
                    }

                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        commentsBottomSheetCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        commentsBottomSheetCommentPostButton.setEnabled(false);

        commentsBottomSheetCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {
                    commentsBottomSheetCommentPostButton.setEnabled(false);
                } else {
                    commentsBottomSheetCommentPostButton.setEnabled(true);
                }

            }
        });

        commentsBottomSheetCommentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (easyNetworkMod.isNetworkAvailable()) {

                    final String comment = commentsBottomSheetCommentEditText.getText().toString().trim();

                    commentsBottomSheetCommentEditText.getText().clear();

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
                                            commentsRecyclerView.smoothScrollToPosition(0);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    });

                } else {
                    noConnection();
                }

            }
        });

    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {

        if (fragment instanceof HomeFragment) {
            HomeFragment homeFragment = (HomeFragment) fragment;
            homeFragment.setOnCommentsOpened(this);
            homeFragment.setOnNoConnection(this);
        }

        if (fragment instanceof MyChallengesFragment) {
            MyChallengesFragment myChallengesFragment = (MyChallengesFragment) fragment;
            myChallengesFragment.setOnNoConnection(this);
        }

        if (fragment instanceof AccountFragment) {
            AccountFragment accountFragment = (AccountFragment) fragment;
            accountFragment.setOnNoConnection(this);
        }

    }

    private void closeKeyboard() {

        View closeKeyboardView = MainActivity.this.getCurrentFocus();
        if (closeKeyboardView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(closeKeyboardView.getWindowToken(), 0);
            commentsBottomSheetCommentEditText.clearFocus();
        }

    }

    @Override
    public void onBackPressed() {

        if (commentsBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            commentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {

            if (doubleBackToExit) {
                super.onBackPressed();
            }

            doubleBackToExit = true;
            Toast.makeText(MainActivity.this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExit = false;
                }
            }, 2000);

        }

    }

    @Override
    public void noConnection() {

        noConnectionLayout.setVisibility(View.VISIBLE);

        noConnectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, getString(R.string.connecting), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (easyNetworkMod.isNetworkAvailable()) {
                            noConnectionLayout.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
                        }

                    }
                }, 2000);

            }
        });

    }

    private String getUserUsername(DocumentReference currentUserDocument) {

        currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                currentUserUsername = documentSnapshot.getString("username");

            }
        });

        return currentUserUsername;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            default:
                return false;

            case R.id.main_menu_help:

                Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(helpIntent);

                return true;

        }

    }

}
