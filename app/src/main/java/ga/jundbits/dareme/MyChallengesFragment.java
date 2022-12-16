package ga.jundbits.dareme;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;

public class MyChallengesFragment extends Fragment implements MyChallengesProfileChallengesRecyclerAdapter.ListItemButtonClick {

    NoConnection noConnection;

    SwipeRefreshLayout myChallengesSwipeRefreshLayout;
    TextView myChallengesYouChallengedText;
    RecyclerView myChallengesChallengesRecyclerView;
    FloatingActionButton myChallengesFAB;

    MyChallengesProfileChallengesRecyclerAdapter myChallengesProfileChallengesRecyclerAdapter;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    String currentUserID;
    String currentUserType;

    DocumentReference currentUserDocument;

    EasyNetworkMod easyNetworkMod;

    public MyChallengesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_challenges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myChallengesSwipeRefreshLayout = view.findViewById(R.id.main_my_challenges_swipe_refresh_layout);
        myChallengesYouChallengedText = view.findViewById(R.id.main_my_challenges_you_challenged_text);
        myChallengesChallengesRecyclerView = view.findViewById(R.id.main_my_challenges_recycler_view);
        myChallengesFAB = view.findViewById(R.id.main_my_challenges_new_challenge_fab);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        currentUserID = firebaseUser.getUid();
        currentUserType = getActivity().getIntent().getExtras().getString("user_type");

        currentUserDocument = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        easyNetworkMod = new EasyNetworkMod(getContext());

        loadMyChallenges();

        if (currentUserType.equals("player")) {
            myChallengesFAB.setVisibility(View.GONE);
        } else if (currentUserType.equals("watcher")) {
            myChallengesFAB.setVisibility(View.VISIBLE);
        }

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

    private void loadMyChallenges() {

        Query query = null;

        if (easyNetworkMod.isNetworkAvailable()) {

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

            PagedList.Config config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(15)
                    .setPageSize(3)
                    .build();

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

        } else {

            if (myChallengesSwipeRefreshLayout.isRefreshing()) {
                myChallengesSwipeRefreshLayout.setRefreshing(false);
            }

            noConnection.noConnection();

        }

    }

    public void setOnNoConnection(NoConnection noConnection) {
        this.noConnection = noConnection;
    }

    @Override
    public void onListItemButtonClick(String buttonName, String username, String challengeID, int challengePosition) {

    }

    public interface NoConnection {
        void noConnection();
    }

}
