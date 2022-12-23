package ga.jundbits.dareme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Activities.ProfileActivity;
import ga.jundbits.dareme.Models.MainHomeChallengesModel;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.TimeAgo;

public class MainHomeChallengesRecyclerAdapter extends FirestorePagingAdapter<MainHomeChallengesModel, MainHomeChallengesRecyclerAdapter.MainHomeChallengesViewHolder> {

    private Context context;
    private ListItemButtonClick listItemButtonClick;

    private TimeAgo timeAgo;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String currentUserID;
    private DocumentReference currentUserDocument;

    public MainHomeChallengesRecyclerAdapter(@NonNull FirestorePagingOptions<MainHomeChallengesModel> options, Context context, ListItemButtonClick listItemButtonClick) {
        super(options);
        this.context = context;
        this.listItemButtonClick = listItemButtonClick;
    }

    @Override
    protected void onBindViewHolder(@NonNull final MainHomeChallengesViewHolder holder, int position, @NonNull MainHomeChallengesModel model) {

        // Retrieving everything
        final String watcherUserID = model.getUser_id();
        final String playerUserID = model.getPlayer_user_id();
        final String image = model.getImage();
        final String username = model.getUsername();
        final String challengesUsername = model.getChallenges_username();
        long dateTimeMillis = model.getDate_time_millis();
        String color = model.getColor();
        String challengeText = model.getText();
        String prize = model.getPrize();
        final boolean accepted = model.isAccepted();
        final boolean completed = model.isCompleted();
        final boolean failed = model.isFailed();

        // Applying
        if (image.equals("default")) {
            holder.userImage.setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(context).load(image).into(holder.userImage);
        }

        holder.userUsername.setText(username);

        currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                String currentUserUsername = snapshot.getString("username");

                // Player
                if (challengesUsername.equals(currentUserUsername)) {

                    holder.challengesUsername.setText(R.string.challenges_you);

                    if (accepted || completed || failed) {
                        holder.challengeAcceptRejectButtonsLayout.setVisibility(View.GONE);
                    } else {
                        holder.challengeAcceptRejectButtonsLayout.setVisibility(View.VISIBLE);
                    }

                } else {

                    holder.challengesUsername.setText(context.getString(R.string.challenges) + " " + challengesUsername);
                    holder.challengeAcceptRejectButtonsLayout.setVisibility(View.GONE);

                }

                // Watcher
                if (username.equals(currentUserUsername)) {

                    if (accepted) {

                        if (completed || failed) {
                            holder.challengeCompletedFailedButtonsLayout.setVisibility(View.GONE);
                        } else {
                            holder.challengeCompletedFailedButtonsLayout.setVisibility(View.VISIBLE);
                        }

                    } else {
                        holder.challengeCompletedFailedButtonsLayout.setVisibility(View.GONE);
                    }

                }

            }
        });

        holder.challengesTimeAgo.setText(timeAgo.getTimeAgo(context, dateTimeMillis));

        holder.challengeTextCardView.setCardBackgroundColor(Color.parseColor(color));

        holder.challengeText.setText(challengeText);

        holder.prizeList.setText(context.getString(R.string.prize) + "\n" + prize);

        if (completed) {
            holder.challengeStatusCompletedLayout.setVisibility(View.VISIBLE);
        } else {
            holder.challengeStatusCompletedLayout.setVisibility(View.GONE);
        }

        if (failed) {
            holder.challengeStatusFailedLayout.setVisibility(View.VISIBLE);
        } else {
            holder.challengeStatusFailedLayout.setVisibility(View.GONE);
        }

        // Check if liked
        firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                .collection("Challenges").document(getItem(holder.getAdapterPosition()).getId())
                .collection("Likes").document(currentUserID)
                .addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (documentSnapshot.exists()) {
                            holder.likeButton.setImageResource(R.drawable.ic_like_red_32dp);
                        } else {
                            holder.likeButton.setImageResource(R.drawable.ic_like_grey_32dp);
                        }

                    }
                });

        // Like
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                        .collection("Challenges").document(getItem(holder.getAdapterPosition()).getId())
                        .collection("Likes").document(currentUserID)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                if (documentSnapshot.exists()) {

                                    holder.likeButton.setImageResource(R.drawable.ic_like_grey_32dp);

                                    firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                                            .collection("Challenges").document(getItem(holder.getAdapterPosition()).getId())
                                            .collection("Likes").document(currentUserID)
                                            .delete();

                                } else {

                                    holder.likeButton.setImageResource(R.drawable.ic_like_red_32dp);

                                    Map<String, Object> likeMap = new HashMap<>();
                                    likeMap.put("user_id", currentUserID);
                                    likeMap.put("timestamp", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                                            .collection("Challenges").document(getItem(holder.getAdapterPosition()).getId())
                                            .collection("Likes").document(currentUserID)
                                            .set(likeMap);

                                }

                            }
                        });

                // Add to watcher document
                firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                        .collection("Users").document(watcherUserID)
                        .collection("Likes").document(currentUserID)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                if (documentSnapshot.exists()) {

                                    firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                                            .collection("Users").document(watcherUserID)
                                            .collection("Likes").document(currentUserID)
                                            .delete();

                                } else {

                                    Map<String, Object> likeMap = new HashMap<>();
                                    likeMap.put("user_id", currentUserID);
                                    likeMap.put("timestamp", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                                            .collection("Users").document(watcherUserID)
                                            .collection("Likes").document(currentUserID)
                                            .set(likeMap);

                                }

                            }
                        });

                // Add to player document
                firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                        .collection("Users").document(playerUserID)
                        .collection("Likes").document(currentUserID)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                if (documentSnapshot.exists()) {

                                    firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                                            .collection("Users").document(playerUserID)
                                            .collection("Likes").document(currentUserID)
                                            .delete();

                                } else {

                                    Map<String, Object> likeMap = new HashMap<>();
                                    likeMap.put("user_id", currentUserID);
                                    likeMap.put("timestamp", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                                            .collection("Users").document(playerUserID)
                                            .collection("Likes").document(currentUserID)
                                            .set(likeMap);

                                }

                            }
                        });

            }
        });

        holder.numberOfLikes(holder.likeCounter, getItem(holder.getAdapterPosition()).getId());
        holder.numberOfComments(holder.commentCounter, getItem(holder.getAdapterPosition()).getId());
        holder.numberOfShares(holder.shareCounter, getItem(holder.getAdapterPosition()).getId());

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openProfile(watcherUserID);
            }
        });

        holder.userUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openProfile(watcherUserID);
            }
        });

        holder.challengesUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                        .collection("Users").whereEqualTo("username", challengesUsername)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                if (!queryDocumentSnapshots.isEmpty()) {

                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                        firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                .collection("Users").document(documentSnapshot.getId())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        String userID = documentSnapshot.getString("id");
                                                        holder.openProfile(userID);

                                                    }
                                                });

                                    }

                                }

                            }
                        });

            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listItemButtonClick.onListItemButtonClick("share", username, challengesUsername, getItem(holder.getAdapterPosition()).getId(), -1);

            }
        });

    }

    @NonNull
    @Override
    public MainHomeChallengesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_home_challenges_list_item, parent, false);
        context = parent.getContext();

        timeAgo = new TimeAgo();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserID = firebaseUser.getUid();
        currentUserDocument = firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        return new MainHomeChallengesViewHolder(view);

    }

    public class MainHomeChallengesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        CircleImageView userImage;
        TextView userUsername, challengesUsername, challengesTimeAgo;
        CardView challengeTextCardView;
        TextView challengeText;
        TextView prizeList;
        ConstraintLayout challengeStatusCompletedLayout, challengeStatusFailedLayout, challengeAcceptRejectButtonsLayout, challengeCompletedFailedButtonsLayout;
        Button acceptButton, rejectButton, completedButton, failedButton;
        ImageButton likeButton, commentButton, shareButton;
        TextView likeCounter, commentCounter, shareCounter;

        public MainHomeChallengesViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.main_home_challenge_list_item_card_view);
            userImage = itemView.findViewById(R.id.main_home_challenge_list_item_user_image);
            userUsername = itemView.findViewById(R.id.main_home_challenge_list_item_user_username);
            challengesUsername = itemView.findViewById(R.id.main_home_challenge_list_item_challenges_username);
            challengesTimeAgo = itemView.findViewById(R.id.main_home_challenge_list_item_challenges_time_ago);
            challengeTextCardView = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_text_card_view);
            challengeText = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_text);
            prizeList = itemView.findViewById(R.id.main_home_challenge_list_item_prize_list);
            challengeStatusCompletedLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_status_completed_layout);
            challengeStatusFailedLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_status_failed_layout);
            challengeAcceptRejectButtonsLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_accept_reject_buttons_layout);
            challengeCompletedFailedButtonsLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_completed_failed_buttons_layout);
            acceptButton = itemView.findViewById(R.id.main_home_challenge_list_item_accept_button);
            rejectButton = itemView.findViewById(R.id.main_home_challenge_list_item_reject_button);
            completedButton = itemView.findViewById(R.id.main_home_challenge_list_item_completed_button);
            failedButton = itemView.findViewById(R.id.main_home_challenge_list_item_failed_button);
            likeButton = itemView.findViewById(R.id.main_home_challenge_list_item_like_button);
            commentButton = itemView.findViewById(R.id.main_home_challenge_list_item_comment_button);
            shareButton = itemView.findViewById(R.id.main_home_challenge_list_item_share_button);
            likeCounter = itemView.findViewById(R.id.main_home_challenge_list_item_like_counter);
            commentCounter = itemView.findViewById(R.id.main_home_challenge_list_item_comment_counter);
            shareCounter = itemView.findViewById(R.id.main_home_challenge_list_item_share_counter);

            cardView.setOnClickListener(this);
            acceptButton.setOnClickListener(this);
            rejectButton.setOnClickListener(this);
            completedButton.setOnClickListener(this);
            failedButton.setOnClickListener(this);
            commentButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.main_home_challenge_list_item_card_view:
                    listItemButtonClick.onListItemButtonClick("card view", null, null, getItem(getAdapterPosition()).getId(), getAdapterPosition());
                    break;

                case R.id.main_home_challenge_list_item_accept_button:
                    listItemButtonClick.onListItemButtonClick("accept", null, null, getItem(getAdapterPosition()).getId(), getAdapterPosition());
                    break;

                case R.id.main_home_challenge_list_item_reject_button:
                    listItemButtonClick.onListItemButtonClick("reject", null, null, getItem(getAdapterPosition()).getId(), getAdapterPosition());
                    break;

                case R.id.main_home_challenge_list_item_completed_button:
                    listItemButtonClick.onListItemButtonClick("completed", null, null, getItem(getAdapterPosition()).getId(), getAdapterPosition());
                    break;

                case R.id.main_home_challenge_list_item_failed_button:
                    listItemButtonClick.onListItemButtonClick("failed", null, null, getItem(getAdapterPosition()).getId(), getAdapterPosition());
                    break;

                case R.id.main_home_challenge_list_item_comment_button:
                    listItemButtonClick.onListItemButtonClick("comment", null, null, getItem(getAdapterPosition()).getId(), -1);
                    break;

            }

        }

        private void numberOfLikes(final TextView likeCounter, String challengeID) {

            firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                    .collection("Challenges").document(challengeID)
                    .collection("Likes")
                    .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            likeCounter.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    });

        }

        public void numberOfComments(final TextView commentCounter, String challengeID) {

            firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                    .collection("Challenges").document(challengeID)
                    .collection("Comments")
                    .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            commentCounter.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    });

        }

        public void numberOfShares(final TextView shareCounter, String challengeID) {

            firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections")
                    .collection("Shareable Links").whereEqualTo("challenge_id", challengeID)
                    .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            shareCounter.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    });

        }

        public void openProfile(String userID) {

            Intent profileIntent = new Intent(context, ProfileActivity.class);
            profileIntent.putExtra("user_id", userID);
            context.startActivity(profileIntent);

        }

    }

    public interface ListItemButtonClick {
        void onListItemButtonClick(String buttonName, String username, String challengesUsername, String challengeID, int challengePosition);
    }

}
