package ga.jundbits.dareme.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.Query;

import ga.jundbits.dareme.Adapters.MainHomeChallengesRecyclerAdapter;
import ga.jundbits.dareme.Callbacks.OnCommentsClick;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout mainHomeSwipeRefreshLayout;
    private RecyclerView mainHomeChallengesRecyclerView;

    private MainHomeChallengesRecyclerAdapter mainHomeChallengesRecyclerAdapter;

    private OnCommentsClick onCommentsClick;

    public HomeFragment() {

    }

    public void setOnCommentsClick(OnCommentsClick onCommentsClick) {
        this.onCommentsClick = onCommentsClick;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initVars(view);
        setupAdapter();
        setOnClicks();

    }

    private void initVars(View view) {

        mainHomeSwipeRefreshLayout = view.findViewById(R.id.main_home_swipe_refresh_layout);
        mainHomeChallengesRecyclerView = view.findViewById(R.id.main_home_challenges_recycler_view);

    }

    private void setupAdapter() {

        Query query = FirebaseHelper.collectionReference("Challenges").orderBy("timestamp", Query.Direction.DESCENDING);

        PagingConfig config = new PagingConfig(3, 5, false, 10);

        FirestorePagingOptions<Challenge> options = new FirestorePagingOptions.Builder<Challenge>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Challenge.class)
                .build();

        mainHomeChallengesRecyclerAdapter = new MainHomeChallengesRecyclerAdapter(options, getContext(), onCommentsClick);

        mainHomeChallengesRecyclerView.setHasFixedSize(true);
        mainHomeChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mainHomeChallengesRecyclerView.setAdapter(mainHomeChallengesRecyclerAdapter);

    }

    private void setOnClicks() {

        mainHomeSwipeRefreshLayout.setOnRefreshListener(() -> {
            mainHomeChallengesRecyclerAdapter.refresh();
            mainHomeSwipeRefreshLayout.setRefreshing(false);
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences challengePreferences = getContext().getSharedPreferences("ChallengePreferences", Context.MODE_PRIVATE);

        int challengePosition = challengePreferences.getInt("position", -1);
        if (challengePosition == -1) return;

        mainHomeChallengesRecyclerAdapter.refresh();
        new Handler().postDelayed(() -> mainHomeChallengesRecyclerView.smoothScrollToPosition(challengePosition), 1000);

        SharedPreferences.Editor editor = challengePreferences.edit();
        editor.clear();
        editor.apply();

    }

}
