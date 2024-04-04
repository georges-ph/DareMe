package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import ga.jundbits.dareme.Adapters.ChallengeCommentsBottomSheetRecyclerAdapter;
import ga.jundbits.dareme.Fragments.AccountFragment;
import ga.jundbits.dareme.Fragments.HomeFragment;
import ga.jundbits.dareme.Fragments.MyChallengesFragment;
import ga.jundbits.dareme.Models.ChallengeCommentsBottomSheetModel;
import ga.jundbits.dareme.Models.CommentModel;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;

public class MainActivity extends AppCompatActivity implements HomeFragment.OpenCommentsBox {

    private Toolbar mainToolbar;
    private BottomNavigationView mainBottomNavigationView;

    private ConstraintLayout commentsBottomSheetConstraintLayout;
    private BottomSheetBehavior commentsBottomSheetBehavior;
    private ImageButton commentsBottomSheetCloseButton;
    private RecyclerView commentsRecyclerView;
    private EditText commentsBottomSheetCommentEditText;
    private ImageButton commentsBottomSheetCommentPostButton;

    private ChallengeCommentsBottomSheetRecyclerAdapter challengeCommentsBottomSheetRecyclerAdapter;

    private boolean scrollToBottom = false;

    private HomeFragment homeFragment;
    private MyChallengesFragment myChallengesFragment;
    private AccountFragment accountFragment;
    private FragmentManager fragmentManager;
    private Fragment activeFragment;

    private DocumentReference currentUserDocument;

    private boolean doubleBackToExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();
        setupToolbar();
        setupFragments();
        loadData();
        setOnClicks();

    }

    private void initVars() {

        mainToolbar = findViewById(R.id.main_toolbar);
        mainBottomNavigationView = findViewById(R.id.main_bottom_navigation_view);

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

        currentUserDocument = HelperMethods.userDocumentRef(this, HelperMethods.getCurrentUserID());

    }

    private void setupToolbar() {

        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

    }

    private void setupFragments() {

        fragmentManager.beginTransaction().add(R.id.main_fragment_container, accountFragment, "account").hide(accountFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_fragment_container, myChallengesFragment, "my challenges").hide(myChallengesFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_fragment_container, homeFragment, "home").commit();

    }

    private void loadData() {

        if (getIntent().hasExtra("challenge_id")) {

            Intent challengeIntent = new Intent(MainActivity.this, ChallengeActivity.class);
            challengeIntent.putExtra("challenge_id", getIntent().getStringExtra("challenge_id"));
            startActivity(challengeIntent);

        }

    }

    private void setOnClicks() {

        mainBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

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
                        getSupportActionBar().setTitle(HelperMethods.getCurrentUserModel().getUsername());
                        return true;

                    default:
                        return false;

                }
            }
        });

        commentsBottomSheetCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        commentsBottomSheetCommentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String comment = commentsBottomSheetCommentEditText.getText().toString().trim();

                commentsBottomSheetCommentEditText.getText().clear();

                CommentModel commentModel = new CommentModel(comment,
                        HelperMethods.getCurrentUserID(),
                        HelperMethods.getCurrentUserModel().getUsername(),
                        HelperMethods.getCurrentUserModel().getImage(),
                        HelperMethods.getCurrentTimestamp());

                HelperMethods.challengesCollectionRef(MainActivity.this).document(commentsBottomSheetCommentPostButton.getTag().toString())
                        .collection("Comments")
                        .add(commentModel)
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

    }

    @Override
    public void onCommentClicked(final String challengeID) {

        Query query = HelperMethods.challengesCollectionRef(this).document(challengeID).collection("Comments").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChallengeCommentsBottomSheetModel> options = new FirestoreRecyclerOptions.Builder<ChallengeCommentsBottomSheetModel>()
                .setLifecycleOwner(this)
                .setQuery(query, ChallengeCommentsBottomSheetModel.class)
                .build();

        challengeCommentsBottomSheetRecyclerAdapter = new ChallengeCommentsBottomSheetRecyclerAdapter(options, this);
// TODO: erja3 red l comments mtl abel bas aaml shaghle eno badel ma a3ml convert lal timestamp
//  when retrieving a3mela when posting aw abel b shwe w hek
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setItemAnimator(null);
        commentsRecyclerView.setAdapter(challengeCommentsBottomSheetRecyclerAdapter);

        commentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        commentsBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED) {

                    HelperMethods.closeKeyboard(MainActivity.this);

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

        commentsBottomSheetCommentPostButton.setEnabled(false);
        commentsBottomSheetCommentPostButton.setTag(challengeID);

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

    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {

        if (fragment instanceof HomeFragment) {
            HomeFragment homeFragment = (HomeFragment) fragment;
            homeFragment.setOnCommentsOpened(this);
        }

        if (fragment instanceof MyChallengesFragment) {
            MyChallengesFragment myChallengesFragment = (MyChallengesFragment) fragment;
        }

        if (fragment instanceof AccountFragment) {
            AccountFragment accountFragment = (AccountFragment) fragment;
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
