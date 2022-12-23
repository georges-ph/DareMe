package ga.jundbits.dareme.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Activities.ChallengeActivity;
import ga.jundbits.dareme.Adapters.AccountProfileChallengesRecyclerAdapter;
import ga.jundbits.dareme.Models.AccountProfileChallengesModel;
import ga.jundbits.dareme.R;

public class AccountFragment extends Fragment implements AccountProfileChallengesRecyclerAdapter.OnListItemClick {

    private SwipeRefreshLayout mainAccountSwipeRefreshLayout;

    private CircleImageView mainAccountUserImage;

    private TextView mainAccountChallengesCounter, mainAccountChallengesText, mainAccountFollowersCounter, mainAccountFollowersText, mainAccountLikesCounter, mainAccountLikesText;

    private TextView mainAccountUserName, mainAccountDescription;

    private RecyclerView mainAccountChallengesRecyclerView;
    private TextView mainAccountNoCompletedChallengesText;

    private AccountProfileChallengesRecyclerAdapter accountProfileChallengesRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String currentUserID;

    private DocumentReference currentUserDocument;

    public AccountFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initVars(view);
        loadAccount();
        setOnClicks();

    }

    private void initVars(View view) {

        mainAccountSwipeRefreshLayout = view.findViewById(R.id.main_account_swipe_refresh_layout);

        mainAccountUserImage = view.findViewById(R.id.main_account_user_image);

        mainAccountChallengesCounter = view.findViewById(R.id.main_account_challenges_counter);
        mainAccountChallengesText = view.findViewById(R.id.main_account_challenges_text);
        mainAccountFollowersCounter = view.findViewById(R.id.main_account_followers_counter);
        mainAccountFollowersText = view.findViewById(R.id.main_account_followers_text);
        mainAccountLikesCounter = view.findViewById(R.id.main_account_likes_counter);
        mainAccountLikesText = view.findViewById(R.id.main_account_likes_text);

        mainAccountUserName = view.findViewById(R.id.main_account_user_name);
        mainAccountDescription = view.findViewById(R.id.main_account_description);

        mainAccountChallengesRecyclerView = view.findViewById(R.id.main_account_challenges_recycler_view);
        mainAccountNoCompletedChallengesText = view.findViewById(R.id.main_account_no_completed_challenges_text);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        currentUserID = firebaseUser.getUid();

        currentUserDocument = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

    }

    private void loadAccount() {

        currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String image = documentSnapshot.getString("image");
                String name = documentSnapshot.getString("name");
                String description = documentSnapshot.getString("description");

                // Image
                if (image.equals("default")) {
                    mainAccountUserImage.setImageResource(R.mipmap.no_image);
                } else {
                    Glide.with(getContext()).load(image).into(mainAccountUserImage);
                }

                // Name
                mainAccountUserName.setText(name);

                // Description
                if (TextUtils.isEmpty(description)) {
                    mainAccountDescription.setVisibility(View.GONE);
                } else {
                    mainAccountDescription.setVisibility(View.VISIBLE);
                    mainAccountDescription.setText(description);
                }


            }
        });

        // Challenges count and text
        currentUserDocument.collection("CompletedChallenges")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        mainAccountChallengesCounter.setText(String.valueOf(queryDocumentSnapshots.size()));

                        if (queryDocumentSnapshots.size() == 1) {
                            mainAccountChallengesText.setText(getString(R.string.challenge));
                        } else {

                            mainAccountChallengesText.setText(getString(R.string.challenges));

                            if (queryDocumentSnapshots.isEmpty()) {
                                mainAccountNoCompletedChallengesText.setVisibility(View.VISIBLE);
                            }

                        }

                    }
                });

        // Followers count and text
        currentUserDocument.collection("Followers")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        mainAccountFollowersCounter.setText(String.valueOf(queryDocumentSnapshots.size()));

                        if (queryDocumentSnapshots.size() == 1) {
                            mainAccountFollowersText.setText(getString(R.string.follower));
                        } else {
                            mainAccountFollowersText.setText(getString(R.string.followers));
                        }

                    }
                });

        // Likes count and text
        currentUserDocument.collection("Likes")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        mainAccountLikesCounter.setText(String.valueOf(queryDocumentSnapshots.size()));

                        if (queryDocumentSnapshots.size() == 1) {
                            mainAccountLikesText.setText(getString(R.string.like));
                        } else {
                            mainAccountLikesText.setText(getString(R.string.likes));
                        }

                    }
                });

        // Challenges
        Query query = currentUserDocument.collection("CompletedChallenges").orderBy("timestamp", Query.Direction.DESCENDING);

        PagingConfig config = new PagingConfig(3, 5, false, 15);

        FirestorePagingOptions<AccountProfileChallengesModel> options = new FirestorePagingOptions.Builder<AccountProfileChallengesModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, AccountProfileChallengesModel.class)
                .build();

        accountProfileChallengesRecyclerAdapter = new AccountProfileChallengesRecyclerAdapter(options, this, getContext());

        mainAccountChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mainAccountChallengesRecyclerView.setHasFixedSize(true);
        mainAccountChallengesRecyclerView.setAdapter(accountProfileChallengesRecyclerAdapter);

        if (mainAccountSwipeRefreshLayout.isRefreshing()) {
            mainAccountSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void setOnClicks() {

        mainAccountSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAccount();
            }
        });

    }

    @Override
    public void onItemClick(String challengeID) {

        Intent challengeIntent = new Intent(getContext(), ChallengeActivity.class);
        challengeIntent.putExtra("challenge_id", challengeID);
        getContext().startActivity(challengeIntent);

    }

}
