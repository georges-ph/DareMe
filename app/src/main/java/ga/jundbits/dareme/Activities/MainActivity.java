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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

import ga.jundbits.dareme.Adapters.ChallengeCommentsBottomSheetRecyclerAdapter;
import ga.jundbits.dareme.Callbacks.OnCommentsClick;
import ga.jundbits.dareme.Fragments.AccountFragment;
import ga.jundbits.dareme.Fragments.HomeFragment;
import ga.jundbits.dareme.Fragments.MyChallengesFragment;
import ga.jundbits.dareme.Models.Comment;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class MainActivity extends AppCompatActivity implements OnCommentsClick {

    private Toolbar mainToolbar;
    private BottomNavigationView mainBottomNavigationView;

    private BottomSheetBehavior<View> challengeCommentsBottomSheetBehavior;
    private ImageButton challengeCommentsBottomSheetCloseButton;
    private RecyclerView challengeCommentsRecyclerView;
    private EditText challengeCommentsBottomSheetCommentEditText;
    private ImageButton challengeCommentsBottomSheetCommentPostButton;

    private final List<Comment> commentsList = new ArrayList<>();

    private ChallengeCommentsBottomSheetRecyclerAdapter challengeCommentsBottomSheetRecyclerAdapter;

    private HomeFragment homeFragment;
    private MyChallengesFragment myChallengesFragment;
    private AccountFragment accountFragment;
    private FragmentManager fragmentManager;
    private Fragment activeFragment;

    private boolean doubleBackToExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();
        setupToolbarAndBackButton();
        setupFragments();
        loadData();
        setOnClicks();

    }

    private void initVars() {

        mainToolbar = findViewById(R.id.main_toolbar);
        mainBottomNavigationView = findViewById(R.id.main_bottom_navigation_view);

        ConstraintLayout challengeCommentsBottomSheetConstraintLayout = findViewById(R.id.challenge_comments_bottom_sheet_constraint_layout);
        challengeCommentsBottomSheetBehavior = BottomSheetBehavior.from(challengeCommentsBottomSheetConstraintLayout);
        challengeCommentsBottomSheetCloseButton = findViewById(R.id.challenge_comments_bottom_sheet_close_button);
        challengeCommentsRecyclerView = findViewById(R.id.challenge_comments_bottom_sheet_recycler_view);
        challengeCommentsBottomSheetCommentEditText = findViewById(R.id.challenge_comments_bottom_sheet_comment_edit_text);
        challengeCommentsBottomSheetCommentPostButton = findViewById(R.id.challenge_comments_bottom_sheet_comment_post_button);

        homeFragment = new HomeFragment();
        myChallengesFragment = new MyChallengesFragment();
        accountFragment = new AccountFragment();
        fragmentManager = getSupportFragmentManager();
        activeFragment = homeFragment;

        homeFragment.setOnCommentsClick(this);

    }

    private void setupToolbarAndBackButton() {

        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (challengeCommentsBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return;
                }

                if (doubleBackToExit) {
                    finish();
                    return;
                }

                doubleBackToExit = true;
                Toast.makeText(MainActivity.this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExit = false, 2000);

            }
        });

    }

    private void setupFragments() {

        fragmentManager.beginTransaction().add(R.id.main_fragment_container, accountFragment, "account").hide(accountFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_fragment_container, myChallengesFragment, "my challenges").hide(myChallengesFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_fragment_container, homeFragment, "home").commit();

    }

    private void loadData() {

        // TODO: 08-Jun-24 fix this when fixing MyFirebaseMessagingService
        if (getIntent().hasExtra("challenge_id")) {
            Intent challengeIntent = new Intent(MainActivity.this, ChallengeActivity.class);
            challengeIntent.putExtra("challenge_id", getIntent().getStringExtra("challenge_id"));
            startActivity(challengeIntent);
        }

    }

    private void setOnClicks() {

        mainBottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.main_bottom_menu_home_fragment) {
                fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit();
                activeFragment = homeFragment;
                getSupportActionBar().setTitle(getString(R.string.app_name));
                return true;
            } else if (itemId == R.id.main_bottom_menu_my_challenges_fragment) {
                fragmentManager.beginTransaction().hide(activeFragment).show(myChallengesFragment).commit();
                activeFragment = myChallengesFragment;
                getSupportActionBar().setTitle(getString(R.string.app_name));
                return true;
            } else if (itemId == R.id.main_bottom_menu_account_fragment) {
                fragmentManager.beginTransaction().hide(activeFragment).show(accountFragment).commit();
                activeFragment = accountFragment;
                getSupportActionBar().setTitle(HelperMethods.getCurrentUser().getUsername());
                return true;
            }
            return false;
        });

        challengeCommentsBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    HelperMethods.closeKeyboard(MainActivity.this);
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

    }

    @Override
    public void onClick(String challengeID) {

        // Load comments on comments button clicked
        FirebaseHelper.documentReference("Challenges/" + challengeID).collection("Comments").get()
                .addOnSuccessListener(this, queryDocumentSnapshots -> {

                    commentsList.clear();
                    commentsList.addAll(queryDocumentSnapshots.toObjects(Comment.class));

                    if (challengeCommentsBottomSheetRecyclerAdapter != null) {
                        challengeCommentsBottomSheetRecyclerAdapter.update(commentsList);
                        return;
                    }

                    challengeCommentsBottomSheetRecyclerAdapter = new ChallengeCommentsBottomSheetRecyclerAdapter(this, commentsList);
                    challengeCommentsRecyclerView.setHasFixedSize(true);
                    challengeCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    challengeCommentsRecyclerView.setAdapter(challengeCommentsBottomSheetRecyclerAdapter);

                });

        challengeCommentsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Set onClick for comment post button
        challengeCommentsBottomSheetCommentPostButton.setOnClickListener(v -> {

            String comment = challengeCommentsBottomSheetCommentEditText.getText().toString().trim();
            challengeCommentsBottomSheetCommentEditText.getText().clear();

            Comment commentModel = new Comment(comment,
                    FirebaseHelper.getCurrentUser().getUid(),
                    HelperMethods.getCurrentUser().getUsername(),
                    HelperMethods.getCurrentUser().getImage());

            commentsList.add(0, commentModel);
            challengeCommentsBottomSheetRecyclerAdapter.update(commentsList);
            challengeCommentsBottomSheetRecyclerAdapter.notifyItemInserted(0);

            FirebaseHelper.documentReference("Challenges/" + challengeID)
                    .collection("Comments")
                    .add(commentModel)
                    .addOnSuccessListener(documentReference -> challengeCommentsRecyclerView.smoothScrollToPosition(0))
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show());

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemID = item.getItemId();
        if (itemID == R.id.main_menu_help) {
            Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(helpIntent);
            return true;
        } else if (itemID == R.id.main_menu_logout) {
            FirebaseHelper.logout();
            Intent splashIntent = new Intent(MainActivity.this, SplashActivity.class);
            splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(splashIntent);
            finish();
            return true;
        }
        return false;

    }

}
