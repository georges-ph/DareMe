package ga.jundbits.dareme.Fragments;

import android.app.Activity;
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
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Activities.ChallengeActivity;
import ga.jundbits.dareme.Adapters.AccountProfileChallengesRecyclerAdapter;
import ga.jundbits.dareme.Callbacks.OnChallengeClick;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class AccountFragment extends Fragment implements OnChallengeClick {

    private SwipeRefreshLayout mainAccountSwipeRefreshLayout;

    private CircleImageView mainAccountUserImage;
    private TextView mainAccountChallengesCounter, mainAccountChallengesText, mainAccountFollowersCounter, mainAccountFollowersText, mainAccountLikesCounter, mainAccountLikesText;
    private TextView mainAccountUserName, mainAccountDescription;
    private RecyclerView mainAccountChallengesRecyclerView;
    private TextView mainAccountNoCompletedChallengesText;

    private AccountProfileChallengesRecyclerAdapter accountProfileChallengesRecyclerAdapter;

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

    }

    private void loadAccount() {

        // User info
        if (HelperMethods.getCurrentUser().getImage() == null) {
            mainAccountUserImage.setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(getContext()).load(HelperMethods.getCurrentUser().getImage()).into(mainAccountUserImage);
        }

        mainAccountUserName.setText(HelperMethods.getCurrentUser().getName());

        if (TextUtils.isEmpty(HelperMethods.getCurrentUser().getDescription())) {
            mainAccountDescription.setVisibility(View.GONE);
        } else {
            mainAccountDescription.setVisibility(View.VISIBLE);
            mainAccountDescription.setText(HelperMethods.getCurrentUser().getDescription());
        }

        // Followers count and text
        FirebaseHelper.collectionReference("Followers").count().get(AggregateSource.SERVER).addOnSuccessListener((Activity) getContext(), aggregateQuerySnapshot -> {
            mainAccountFollowersCounter.setText(String.valueOf(aggregateQuerySnapshot.getCount()));
            mainAccountFollowersText.setText(aggregateQuerySnapshot.getCount() == 1 ? getString(R.string.follower) : getString(R.string.followers));
        });

        // Likes count and text
        FirebaseHelper.collectionReference("Likes").count().get(AggregateSource.SERVER).addOnSuccessListener((Activity) getContext(), aggregateQuerySnapshot -> {
            mainAccountLikesCounter.setText(String.valueOf(aggregateQuerySnapshot.getCount()));
            mainAccountLikesText.setText(aggregateQuerySnapshot.getCount() == 1 ? getString(R.string.like) : getString(R.string.likes));
        });

        // Challenges with count and text
        Query query = FirebaseHelper.collectionReference("Challenges")
                .whereEqualTo("player_id", FirebaseHelper.getCurrentUser().getUid())
                .whereEqualTo("completed", true)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        PagingConfig config = new PagingConfig(3, 5, false, 15);

        FirestorePagingOptions<Challenge> options = new FirestorePagingOptions.Builder<Challenge>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Challenge.class)
                .build();

        if (accountProfileChallengesRecyclerAdapter == null) {
            accountProfileChallengesRecyclerAdapter = new AccountProfileChallengesRecyclerAdapter(options, getContext(), this);
            mainAccountChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mainAccountChallengesRecyclerView.setHasFixedSize(true);
            mainAccountChallengesRecyclerView.setAdapter(accountProfileChallengesRecyclerAdapter);
        }

        mainAccountChallengesCounter.setText(String.valueOf(accountProfileChallengesRecyclerAdapter.getItemCount()));

        if (accountProfileChallengesRecyclerAdapter.getItemCount() == 0) {
            mainAccountNoCompletedChallengesText.setVisibility(View.VISIBLE);
            mainAccountChallengesText.setText(getString(R.string.challenges));
        } else if (accountProfileChallengesRecyclerAdapter.getItemCount() == 1) {
            mainAccountNoCompletedChallengesText.setVisibility(View.GONE);
            mainAccountChallengesText.setText(getString(R.string.challenge));
        } else {
            mainAccountNoCompletedChallengesText.setVisibility(View.GONE);
            mainAccountChallengesText.setText(getString(R.string.challenges));
        }

        if (mainAccountSwipeRefreshLayout.isRefreshing()) {
            mainAccountSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void setOnClicks() {
        mainAccountSwipeRefreshLayout.setOnRefreshListener(this::loadAccount);
    }

    @Override
    public void onClick(String challengeID, Challenge challenge) {

        Bundle bundle = new Bundle();
        bundle.putString("challenge", new Gson().toJson(challenge));
        bundle.putString("challenge_id", challengeID);

        Intent challengeIntent = new Intent(getContext(), ChallengeActivity.class);
        challengeIntent.putExtras(bundle);
        getContext().startActivity(challengeIntent);

    }

}
