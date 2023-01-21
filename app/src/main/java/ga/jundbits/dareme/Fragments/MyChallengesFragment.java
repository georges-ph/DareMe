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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import ga.jundbits.dareme.Activities.NewChallengeActivity;
import ga.jundbits.dareme.Adapters.MyChallengesProfileChallengesRecyclerAdapter;
import ga.jundbits.dareme.Models.MyChallengesProfileChallengesModel;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;

public class MyChallengesFragment extends Fragment implements MyChallengesProfileChallengesRecyclerAdapter.ListItemButtonClick {

    private SwipeRefreshLayout myChallengesSwipeRefreshLayout;
    private TextView myChallengesYouChallengedText;
    private RecyclerView myChallengesChallengesRecyclerView;
    private FloatingActionButton myChallengesFAB;

    private MyChallengesProfileChallengesRecyclerAdapter myChallengesProfileChallengesRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String currentUserID;
    private String currentUserType;

    private DocumentReference currentUserDocument;

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

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        currentUserID = firebaseUser.getUid();
        currentUserType = HelperMethods.getCurrentUserModel().getType();

        currentUserDocument = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

    }

    private void loadMyChallenges() {

        if (currentUserType.equals("player")) {
            myChallengesFAB.setVisibility(View.GONE);
        } else if (currentUserType.equals("watcher")) {
            myChallengesFAB.setVisibility(View.VISIBLE);
        }

        Query query = null;

        if (currentUserType.equals("player")) {
            query = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").whereEqualTo("player_user_id", currentUserID);
        } else if (currentUserType.equals("watcher")) {
            query = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").whereEqualTo("user_id", currentUserID);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (queryDocumentSnapshots.isEmpty()) {

                    if (currentUserType.equals("player")) {
                        myChallengesYouChallengedText.setText(getContext().getString(R.string.you_havent_been_challenged_by_anyone));
                    } else if (currentUserType.equals("watcher")) {
                        myChallengesYouChallengedText.setText(getContext().getString(R.string.you_havent_challenged_anyone));
                    }

                } else {

                    if (currentUserType.equals("player")) {
                        myChallengesYouChallengedText.setText(getContext().getString(R.string.you_have_been_challenged_by));
                    } else if (currentUserType.equals("watcher")) {
                        myChallengesYouChallengedText.setText(getContext().getString(R.string.you_have_challenged));
                    }

                }

            }
        });

        PagingConfig config = new PagingConfig(3, 5, false, 15);

        FirestorePagingOptions<MyChallengesProfileChallengesModel> options = new FirestorePagingOptions.Builder<MyChallengesProfileChallengesModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, MyChallengesProfileChallengesModel.class)
                .build();

        myChallengesProfileChallengesRecyclerAdapter = new MyChallengesProfileChallengesRecyclerAdapter(options, getContext(), this);

        myChallengesChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myChallengesChallengesRecyclerView.setHasFixedSize(true);
        myChallengesChallengesRecyclerView.setAdapter(myChallengesProfileChallengesRecyclerAdapter);

        if (myChallengesSwipeRefreshLayout.isRefreshing()) {
            myChallengesSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void setOnClicks() {

        myChallengesSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMyChallenges();
            }
        });

        myChallengesFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent newChallengeIntent = new Intent(getContext(), NewChallengeActivity.class);
                getContext().startActivity(newChallengeIntent);

            }
        });

    }

    @Override
    public void onListItemButtonClick(String buttonName, String username, String challengeID, int challengePosition) {

    }

}
