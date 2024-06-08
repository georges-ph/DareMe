package ga.jundbits.dareme.Fragments;

import android.content.Intent;
import android.os.Bundle;
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

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;

import ga.jundbits.dareme.Activities.NewChallengeActivity;
import ga.jundbits.dareme.Adapters.MainMyChallengesRecyclerAdapter;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class MyChallengesFragment extends Fragment {

    private SwipeRefreshLayout myChallengesSwipeRefreshLayout;
    private TextView myChallengesYouChallengedText;
    private RecyclerView myChallengesChallengesRecyclerView;
    private FloatingActionButton myChallengesFAB;

    private MainMyChallengesRecyclerAdapter challengesRecyclerAdapter;

    public MyChallengesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_challenges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initVars(view);
        loadMyChallenges();
        setOnClicks();

    }

    private void initVars(View view) {

        myChallengesSwipeRefreshLayout = view.findViewById(R.id.main_my_challenges_swipe_refresh_layout);
        myChallengesYouChallengedText = view.findViewById(R.id.main_my_challenges_you_challenged_text);
        myChallengesChallengesRecyclerView = view.findViewById(R.id.main_my_challenges_recycler_view);
        myChallengesFAB = view.findViewById(R.id.main_my_challenges_new_challenge_fab);

    }

    private void loadMyChallenges() {

        Query query = null;

        if (HelperMethods.getCurrentUser().getType().equals("player")) {
            myChallengesFAB.setVisibility(View.GONE);
            query = FirebaseHelper.collectionReference("Challenges").whereEqualTo("player_id", FirebaseHelper.getCurrentUser().getUid());
        } else if (HelperMethods.getCurrentUser().getType().equals("watcher")) {
            myChallengesFAB.setVisibility(View.VISIBLE);
            query = FirebaseHelper.collectionReference("Challenges").whereEqualTo("watcher_id", FirebaseHelper.getCurrentUser().getUid());
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (queryDocumentSnapshots.isEmpty()) {
                myChallengesYouChallengedText.setText(HelperMethods.getCurrentUser().getType().equals("player") ? getContext().getString(R.string.you_havent_been_challenged_by_anyone) : getContext().getString(R.string.you_havent_challenged_anyone));
            } else {
                myChallengesYouChallengedText.setText(HelperMethods.getCurrentUser().getType().equals("player") ? getContext().getString(R.string.you_have_been_challenged_by) : getContext().getString(R.string.you_have_challenged));
            }

        });

        PagingConfig config = new PagingConfig(3, 5, false, 15);

        FirestorePagingOptions<Challenge> options = new FirestorePagingOptions.Builder<Challenge>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Challenge.class)
                .build();

        if (challengesRecyclerAdapter == null) {
            challengesRecyclerAdapter = new MainMyChallengesRecyclerAdapter(options, getContext());
            myChallengesChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            myChallengesChallengesRecyclerView.setHasFixedSize(true);
            myChallengesChallengesRecyclerView.setAdapter(challengesRecyclerAdapter);
        }

        if (myChallengesSwipeRefreshLayout.isRefreshing()) {
            myChallengesSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void setOnClicks() {

        myChallengesSwipeRefreshLayout.setOnRefreshListener(() -> {
            challengesRecyclerAdapter.refresh();
            myChallengesSwipeRefreshLayout.setRefreshing(false);
        });

        myChallengesFAB.setOnClickListener(v -> {
            Intent newChallengeIntent = new Intent(getContext(), NewChallengeActivity.class);
            getContext().startActivity(newChallengeIntent);
        });

    }

}
